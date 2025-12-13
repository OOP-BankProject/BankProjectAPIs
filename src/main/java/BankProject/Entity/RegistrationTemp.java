package BankProject.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "otp_session")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationTemp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 7)
    private String fin;

    @Column(nullable = false, length = 13)
    private String phoneNumber;

    @Column(nullable = false, length = 100)
    private String otpCode;

    @Column(nullable = false)
    private LocalDateTime otpExpiryTime;

    @Column(nullable = false)
    private Integer otpAttempts = 0;

    @Column(nullable = false)
    private Boolean isOtpVerified = false;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        otpExpiryTime = LocalDateTime.now().plusMinutes(5);
    }
}
