package BankProject.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions", indexes = {
        @Index(name = "idx_from_card", columnList = "from_card_id"),
        @Index(name = "idx_to_card", columnList = "to_card_id"),
        @Index(name = "idx_created_at", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String transactionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_card_id")
    private Card fromCard;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_card_id")
    private Card toCard;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionStatus status;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(precision = 15, scale = 2)
    private BigDecimal fee = BigDecimal.ZERO;

    @Column(length = 500)
    private String description;

    @Column(length = 100)
    private String recipientAccount; // Bill ödənişləri üçün

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private BillType billType; // Bill ödənişləri üçün

    @Column(length = 200)
    private String failureReason;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum TransactionType {
        TRANSFER,           // Kart transferi
        BILL_MOBILE,        // Mobil operator
        BILL_UTILITY_WATER, // Su
        BILL_UTILITY_GAS,   // Qaz
        BILL_UTILITY_ELECTRICITY, // İşıq
        BILL_INTERNET,      // İnternet
        DEPOSIT,            // Depozit qoyuluşu
        WITHDRAWAL,         // Pul çıxarma
        LOAN_PAYMENT,       // Kredit ödənişi
        CARD_TO_CARD        // Kart-kart transfer
    }

    public enum TransactionStatus {
        PENDING,
        COMPLETED,
        FAILED,
        CANCELLED
    }

    public enum BillType {
        AZERCELL,
        BAKCELL,
        NAR,
        AZERSU,
        AZERIQAZ,
        AZERENERJI,
        AZERTELECOM,
        INTERNET_OTHER
    }
}
