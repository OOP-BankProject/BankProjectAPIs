package BankProject.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "cards")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, unique = true, length = 16)
    private String cardNumber;

    @Column(nullable = false, length = 100)
    private String cvv; // Şifrələnmiş

    @Column(nullable = false)
    private LocalDate expiryDate;

    @Column(nullable = false, length = 100)
    private String pin; // Şifrələnmiş

    @Column(nullable = false, length = 50)
    private String cardHolderName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CardType cardType; // DEBIT, CREDIT

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CardStatus status; // ACTIVE, BLOCKED, EXPIRED

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(precision = 15, scale = 2)
    private BigDecimal creditLimit; // Kredit kartı üçün

    @Column(precision = 15, scale = 2)
    private BigDecimal usedCredit = BigDecimal.ZERO;

    @Column(nullable = false)
    private Boolean isVirtual = false;

    @Column(nullable = false)
    private Boolean isPinSet = false;

    @Column(nullable = false)
    private Integer failedPinAttempts = 0;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum CardType {
        DEBIT, CREDIT
    }

    public enum CardStatus {
        ACTIVE, BLOCKED, EXPIRED, PENDING
    }
}
