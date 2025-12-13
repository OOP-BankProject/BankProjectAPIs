package BankProject.Services;

import BankProject.DTO.LoginDTO.*;
import BankProject.Entity.User;
import BankProject.Exceptions.*;
import BankProject.Repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import BankProject.Repository.UserRepository;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class LoginService {

    private static final Logger log = LoggerFactory.getLogger(LoginService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration.access:3600000}") // 1 saat
    private Long accessTokenExpiration;

    @Value("${jwt.expiration.refresh:604800000}") // 7 gün
    private Long refreshTokenExpiration;

    // Constructor
    public LoginService(UserRepository userRepository,
                        PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Login
    public LoginResponse login(LoginRequest request) {
        log.info("Login cəhdi - FIN: {}", request.getFin());

        // İstifadəçini tap
        User user = userRepository.findByFin(request.getFin())
                .orElseThrow(() -> new UserNotFoundException("İstifadəçi tapılmadı"));

        // Hesab aktiv yoxla
        if (!user.getIsActive()) {
            throw new InvalidCredentialsException("Hesab deaktiv edilib");
        }

        // Parol yoxla
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Yanlış parol cəhdi - FIN: {}", request.getFin());
            throw new InvalidCredentialsException("FIN və ya parol yanlışdır");
        }

        // Token-lər yarat
        String accessToken = generateAccessToken(user);
        String refreshToken = generateRefreshToken(user);

        log.info("Uğurlu login - User ID: {}, Email: {}", user.getId(), user.getEmail());

        return new LoginResponse(
                "Uğurla daxil oldunuz",
                accessToken,
                refreshToken,
                user.getId(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getFirstName(),
                user.getLastName()
        );
    }

    // Refresh Token
    public RefreshTokenResponse refreshToken(RefreshTokenRequest request) {
        try {
            // Refresh token-i validate et
            String fin = extractFinFromToken(request.getRefreshToken());

            // İstifadəçini tap
            User user = userRepository.findByFin(fin)
                    .orElseThrow(() -> new UserNotFoundException("İstifadəçi tapılmadı"));

            // Yeni token-lər yarat
            String newAccessToken = generateAccessToken(user);
            String newRefreshToken = generateRefreshToken(user);

            log.info("Token yeniləndi - User ID: {}", user.getId());

            return new RefreshTokenResponse(newAccessToken, newRefreshToken);

        } catch (Exception e) {
            throw new InvalidCredentialsException("Keçərsiz və ya vaxtı keçmiş refresh token");
        }
    }

    // Access Token yarat
    private String generateAccessToken(User user) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        return Jwts.builder()
                .setSubject(user.getFin())
                .claim("userId", user.getId())
                .claim("email", user.getEmail())
                .claim("type", "access")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // Refresh Token yarat
    private String generateRefreshToken(User user) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        return Jwts.builder()
                .setSubject(user.getFin())
                .claim("type", "refresh")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshTokenExpiration))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // Token-dən FIN-i çıxar
    private String extractFinFromToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        Claims claims = Jwts.parser()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }
}