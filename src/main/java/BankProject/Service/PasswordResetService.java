package BankProject.Service;

import BankProject.DTO.PasswordResetDTO.*;
import BankProject.Entity.PasswordResetTemp;
import BankProject.Entity.User;
import BankProject.Exceptions.*;
import BankProject.Repository.PasswordResetTempRepository;
import BankProject.Repository.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Random;

@Service
public class PasswordResetService {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetService.class);

    private final UserRepository userRepository;
    private final PasswordResetTempRepository passwordResetTempRepository;
    private final PasswordEncoder passwordEncoder;
    private final SmsService smsService;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration.password-reset:600000}") // 10 dəqiqə
    private Long passwordResetTokenExpiration;

    private static final Integer MAX_OTP_ATTEMPTS = 3;
    private static final Integer OTP_EXPIRY_MINUTES = 5;

    public PasswordResetService(UserRepository userRepository,
                                PasswordResetTempRepository passwordResetTempRepository,
                                PasswordEncoder passwordEncoder,
                                SmsService smsService) {
        this.userRepository = userRepository;
        this.passwordResetTempRepository = passwordResetTempRepository;
        this.passwordEncoder = passwordEncoder;
        this.smsService = smsService;
    }

    // 1. Parol Unutdum - Addım 1
    @Transactional
    public ForgotPasswordStep1Response initiateForgotPassword(ForgotPasswordStep1Request request) {
        log.info("Parol sıfırlama başladı - FIN: {}", request.getFin());

        // İstifadəçini tap
        User user = userRepository.findByFin(request.getFin())
                .orElseThrow(() -> new UserNotFoundException("İstifadəçi tapılmadı"));

        // Telefon nömrəsini yoxla
        if (!user.getPhoneNumber().equals(request.getPhoneNumber())) {
            throw new InvalidPhoneNumberException("Telefon nömrəsi uyğun gəlmir");
        }

        // Əvvəlki müvəqqəti qeydləri sil
        passwordResetTempRepository.findByFin(request.getFin())
                .ifPresent(temp -> passwordResetTempRepository.deleteByFin(request.getFin()));

        // OTP kodu yarat
        String otpCode = generateOtpCode();

        // Müvəqqəti qeyd yarat
        PasswordResetTemp temp = new PasswordResetTemp();
        temp.setFin(request.getFin());
        temp.setPhoneNumber(request.getPhoneNumber());
        temp.setOtpCode(passwordEncoder.encode(otpCode));
        temp.setOtpExpiryTime(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES));
        temp.setOtpAttempts(0);
        temp.setIsOtpVerified(false);

        passwordResetTempRepository.save(temp);

        // SMS göndər
        smsService.sendPasswordResetOtp(request.getPhoneNumber(), otpCode);
        log.info("Parol sıfırlama OTP göndərildi - FIN: {}", request.getFin());

        // Reset token yarat
        String resetToken = generateResetToken(request.getFin());

        return new ForgotPasswordStep1Response(
                "Parol sıfırlama kodu telefon nömrənizə göndərildi",
                resetToken,
                OTP_EXPIRY_MINUTES * 60
        );
    }

    // 2. OTP Yoxla
    @Transactional
    public ForgotPasswordOtpResponse verifyForgotPasswordOtp(ForgotPasswordOtpRequest request) {
        String fin = extractFinFromToken(request.getResetToken());

        PasswordResetTemp temp = passwordResetTempRepository.findByFin(fin)
                .orElseThrow(() -> new InvalidSessionTokenException("Keçərsiz reset token"));

        // OTP cəhdlərini yoxla
        if (temp.getOtpAttempts() >= MAX_OTP_ATTEMPTS) {
            passwordResetTempRepository.deleteByFin(fin);
            throw new OtpAttemptsExceededException("OTP cəhdləri limitini keçdiniz");
        }

        // OTP vaxtını yoxla
        if (LocalDateTime.now().isAfter(temp.getOtpExpiryTime())) {
            passwordResetTempRepository.deleteByFin(fin);
            throw new OtpExpiredException("OTP kodunun vaxtı bitib");
        }

        // OTP kodunu yoxla
        if (!passwordEncoder.matches(request.getOtpCode(), temp.getOtpCode())) {
            temp.setOtpAttempts(temp.getOtpAttempts() + 1);
            passwordResetTempRepository.save(temp);
            throw new InvalidOtpException("OTP kodu yanlışdır. Qalan cəhd: " +
                    (MAX_OTP_ATTEMPTS - temp.getOtpAttempts()));
        }

        // OTP təsdiqləndi
        temp.setIsOtpVerified(true);
        passwordResetTempRepository.save(temp);

        // Password reset token yarat
        String passwordResetToken = generatePasswordResetToken(fin);

        log.info("Parol sıfırlama OTP təsdiqləndi - FIN: {}", fin);

        return new ForgotPasswordOtpResponse(
                "OTP kodu təsdiqləndi. İndi yeni parol təyin edə bilərsiniz",
                true,
                passwordResetToken
        );
    }

    // 3. Yeni Parol Təyin Et
    @Transactional
    public ResetPasswordResponse resetPassword(ResetPasswordRequest request) {
        String fin = extractFinFromToken(request.getPasswordResetToken());

        PasswordResetTemp temp = passwordResetTempRepository.findByFin(fin)
                .orElseThrow(() -> new InvalidVerificationTokenException("Keçərsiz password reset token"));

        // OTP təsdiqini yoxla
        if (!temp.getIsOtpVerified()) {
            throw new OtpNotVerifiedException("OTP kodu təsdiqlənməyib");
        }

        // Parol uyğunluğunu yoxla
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new PasswordMismatchException("Parollar uyğun gəlmir");
        }

        // İstifadəçini tap və parolu yenilə
        User user = userRepository.findByFin(fin)
                .orElseThrow(() -> new UserNotFoundException("İstifadəçi tapılmadı"));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Müvəqqəti qeydi sil
        passwordResetTempRepository.deleteByFin(fin);

        log.info("Parol uğurla yeniləndi - FIN: {}", fin);

        return new ResetPasswordResponse(
                "Parolunuz uğurla yeniləndi",
                fin
        );
    }

    // 4. Login Olmuş İstifadəçi Parolunu Dəyişsin
    @Transactional
    public ChangePasswordResponse changePassword(ChangePasswordRequest request) {
        log.info("Parol dəyişdirilməsi tələbi - FIN: {}", request.getFin());

        // İstifadəçini tap
        User user = userRepository.findByFin(request.getFin())
                .orElseThrow(() -> new UserNotFoundException("İstifadəçi tapılmadı"));

        // Köhnə parolu yoxla
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Köhnə parol yanlışdır");
        }

        // Yeni parol uyğunluğunu yoxla
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new PasswordMismatchException("Yeni parollar uyğun gəlmir");
        }

        // Yeni və köhnə parol eyni olmasın
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new RegistrationException("Yeni parol köhnə paroldan fərqli olmalıdır");
        }

        // Parolu yenilə
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        log.info("Parol uğurla dəyişdirildi - FIN: {}", request.getFin());

        return new ChangePasswordResponse("Parolunuz uğurla dəyişdirildi");
    }

    // Helper metodlar
    private String generateOtpCode() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(1000000));
    }

    private String generateResetToken(String fin) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        return Jwts.builder()
                .setSubject(fin)
                .claim("type", "reset")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 300000)) // 5 min
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    private String generatePasswordResetToken(String fin) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        return Jwts.builder()
                .setSubject(fin)
                .claim("type", "password-reset")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + passwordResetTokenExpiration))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    private String extractFinFromToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
            return Jwts.parser()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (Exception e) {
            throw new InvalidSessionTokenException("Keçərsiz və ya vaxtı keçmiş token");
        }
    }

    // Hər gün saat 02:00-da vaxtı keçmiş qeydləri sil
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void cleanupExpiredRecords() {
        LocalDateTime expiryTime = LocalDateTime.now().minusHours(24);
        passwordResetTempRepository.deleteExpiredRecords(expiryTime);
        log.info("Vaxtı keçmiş parol sıfırlama qeydləri silindi");
    }
}
