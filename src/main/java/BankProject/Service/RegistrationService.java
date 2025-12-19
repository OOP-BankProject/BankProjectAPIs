package BankProject.Service;

import BankProject.DTO.OtpVerificationRequest;
import BankProject.DTO.OtpVerificationResponse;
import BankProject.DTO.RegistrationStep1Request;
import BankProject.DTO.RegistrationStep1Response;
import BankProject.DTO.RegistrationStep2Request;
import BankProject.DTO.RegistrationStep2Response;
import BankProject.Entity.User;
import BankProject.Exceptions.*;
import BankProject.Repository.UserRepository;
import BankProject.Repository.RegistrationTempRepository;

import BankProject.Entity.RegistrationTemp;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Random;


@Service
public class RegistrationService {

    private static final Logger log = LoggerFactory.getLogger(RegistrationService.class);

    private final UserRepository userRepository;
    private final RegistrationTempRepository registrationTempRepository;
    private final PasswordEncoder passwordEncoder;
    private final SmsService smsService;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration.session:300000}") // 5 dəqiqə
    private Long sessionTokenExpiration;

    @Value("${jwt.expiration.verification:600000}") // 10 dəqiqə
    private Long verificationTokenExpiration;

    private static final Integer MAX_OTP_ATTEMPTS = 3;
    private static final Integer OTP_EXPIRY_MINUTES = 5;

    // Constructor
    public RegistrationService(UserRepository userRepository,
                               RegistrationTempRepository registrationTempRepository,
                               PasswordEncoder passwordEncoder,
                               SmsService smsService) {
        this.userRepository = userRepository;
        this.registrationTempRepository = registrationTempRepository;
        this.passwordEncoder = passwordEncoder;
        this.smsService = smsService;
    }

    // Addım 1: FIN və Telefon - OTP göndərilməsi
    @Transactional
    public RegistrationStep1Response initiateRegistration(RegistrationStep1Request request) {
        log.info("Qeydiyyat başladı - FIN: {}", request.getFin());

        // FIN yoxlanışı - Əgər mövcuddursa logine yönləndir
        if (userRepository.existsByFin(request.getFin())) {
            log.warn("FIN artıq mövcuddur, login səhifəsinə yönləndirilir - FIN: {}", request.getFin());
            throw new FinAlreadyExistsException("Bu FIN artıq qeydiyyatdan keçib. Zəhmət olmasa login səhifəsinə keçin");
        }

        // Telefon yoxlanışı
        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new PhoneNumberAlreadyExistsException("Bu telefon nömrəsi artıq qeydiyyatdan keçib");
        }

        // Əvvəlki müvəqqəti qeydləri sil
        registrationTempRepository.findByFin(request.getFin())
                .ifPresent(temp -> registrationTempRepository.deleteByFin(request.getFin()));

        // OTP kodu yarat
        String otpCode = generateOtpCode();

        // Müvəqqəti qeyd yarat
        RegistrationTemp temp = new RegistrationTemp();
        temp.setFin(request.getFin());
        temp.setPhoneNumber(request.getPhoneNumber());
        temp.setOtpCode(passwordEncoder.encode(otpCode));
        temp.setOtpExpiryTime(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES));
        temp.setOtpAttempts(0);
        temp.setIsOtpVerified(false);

        registrationTempRepository.save(temp);

        // SMS göndər
        smsService.sendOtp(request.getPhoneNumber(), otpCode);
        log.info("OTP göndərildi - Telefon: {}", request.getPhoneNumber());

        // Session token yarat
        String sessionToken = generateSessionToken(request.getFin());

        return new RegistrationStep1Response(
                "OTP kodu telefon nömrənizə göndərildi",
                sessionToken,
                OTP_EXPIRY_MINUTES * 60
        );
    }

    // OTP Yoxlanış
    @Transactional
    public OtpVerificationResponse verifyOtp(OtpVerificationRequest request) {
        String fin = extractFinFromToken(request.getSessionToken());

        RegistrationTemp temp = registrationTempRepository.findByFin(fin)
                .orElseThrow(() -> new InvalidSessionTokenException("Keçərsiz session"));

        // OTP cəhdlərini yoxla
        if (temp.getOtpAttempts() >= MAX_OTP_ATTEMPTS) {
            registrationTempRepository.deleteByFin(fin);
            throw new OtpAttemptsExceededException("OTP cəhdləri limitini keçdiniz. Yenidən başlayın");
        }

        // OTP vaxtını yoxla
        if (LocalDateTime.now().isAfter(temp.getOtpExpiryTime())) {
            registrationTempRepository.deleteByFin(fin);
            throw new OtpExpiredException("OTP kodunun vaxtı bitib. Yenidən başlayın");
        }

        // OTP kodunu yoxla
        if (!passwordEncoder.matches(request.getOtpCode(), temp.getOtpCode())) {
            temp.setOtpAttempts(temp.getOtpAttempts() + 1);
            registrationTempRepository.save(temp);
            throw new InvalidOtpException("OTP kodu yanlışdır. Qalan cəhd: " +
                    (MAX_OTP_ATTEMPTS - temp.getOtpAttempts()));
        }

        // OTP təsdiqləndi
        temp.setIsOtpVerified(true);
        registrationTempRepository.save(temp);

        // Verification token yarat
        String verificationToken = generateVerificationToken(fin);

        log.info("OTP təsdiqləndi - FIN: {}", fin);

        return new OtpVerificationResponse(
                "OTP kodu təsdiqləndi. İndi qeydiyyatı tamamlaya bilərsiniz",
                true,
                verificationToken
        );
    }

    // OTP Təkrar Göndərmə
    @Transactional
    public RegistrationStep1Response resendOtp(BankProject.DTO.ResendOtpRequest request) {
        String fin = extractFinFromToken(request.getSessionToken());

        RegistrationTemp temp = registrationTempRepository.findByFin(fin)
                .orElseThrow(() -> new InvalidSessionTokenException("Keçərsiz session"));

        // Yeni OTP yarat
        String otpCode = generateOtpCode();
        temp.setOtpCode(passwordEncoder.encode(otpCode));
        temp.setOtpExpiryTime(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES));
        temp.setOtpAttempts(0);

        registrationTempRepository.save(temp);

        // SMS göndər
        smsService.sendOtp(temp.getPhoneNumber(), otpCode);

        log.info("OTP yenidən göndərildi - FIN: {}", fin);

        return new RegistrationStep1Response(
                "Yeni OTP kodu göndərildi",
                request.getSessionToken(),
                OTP_EXPIRY_MINUTES * 60
        );
    }

    // Addım 2: Qeydiyyatı tamamla
    @Transactional
    public RegistrationStep2Response completeRegistration(RegistrationStep2Request request) {
        String fin = extractFinFromToken(request.getVerificationToken());

        RegistrationTemp temp = registrationTempRepository.findByFin(fin)
                .orElseThrow(() -> new InvalidVerificationTokenException("Keçərsiz verification token"));

        // OTP təsdiqini yoxla
        if (!temp.getIsOtpVerified()) {
            throw new OtpNotVerifiedException("OTP kodu təsdiqlənməyib");
        }

        // Yaş yoxlanışı - 18 yaşdan böyük olmalıdır
        LocalDate today = LocalDate.now();
        LocalDate eighteenYearsAgo = today.minusYears(18);

        if (request.getDateOfBirth().isAfter(eighteenYearsAgo)) {
            throw new RegistrationException("Qeydiyyat üçün minimum 18 yaş tələb olunur");
        }

        // Parol uyğunluğunu yoxla
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new PasswordMismatchException("Parollar uyğun gəlmir");
        }

        // Email yoxlanışı
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Bu email artıq istifadə olunub");
        }

        // İstifadəçi yarat
        User user = new User();
        user.setFin(temp.getFin());
        user.setPhoneNumber(temp.getPhoneNumber());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setDateOfBirth(request.getDateOfBirth());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setIsVerified(true);
        user.setIsActive(true);

        User savedUser = userRepository.save(user);

        // Müvəqqəti qeydi sil
        registrationTempRepository.deleteByFin(fin);

        log.info("Qeydiyyat tamamlandı - User ID: {}, Email: {}, Yaş: {}",
                savedUser.getId(), savedUser.getEmail(),
                java.time.Period.between(savedUser.getDateOfBirth(), LocalDate.now()).getYears());

        return new RegistrationStep2Response(
                "Qeydiyyat uğurla tamamlandı",
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getPhoneNumber()
        );
    }

    // Helper metodlar
    private String generateOtpCode() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(1000000));
    }

    private String generateSessionToken(String fin) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        return Jwts.builder()
                .setSubject(fin)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + sessionTokenExpiration))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    private String generateVerificationToken(String fin) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        return Jwts.builder()
                .setSubject(fin)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + verificationTokenExpiration))
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
        registrationTempRepository.deleteExpiredRecords(expiryTime);
        log.info("Vaxtı keçmiş qeydlər silindi");
    }
}
