package BankProject.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "loans")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id", nullable = false)
    private Card card;

    @Column(nullable = false, unique = true, length = 50)
    private String loanNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private LoanType loanType;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal principalAmount; // Əsas məbləğ

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal interestRate; // Faiz dərəcəsi (%)

    @Column(nullable = false)
    private Integer termMonths; // Müddət (ay)

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal monthlyPayment;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal remainingAmount;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalPaid = BigDecimal.ZERO;

    @Column(nullable = false)
    private Integer remainingMonths;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LoanStatus status;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate nextPaymentDate;

    @Column
    private LocalDate endDate;

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

    public enum LoanType {
        CONSUMER,      // İstehlak krediti
        MORTGAGE,      // İpotek
        AUTO,          // Avto kredit
        BUSINESS,      // Biznes kredit
        EDUCATION      // Təhsil krediti
    }

    public enum LoanStatus {
        PENDING,       // Gözləmədə
        APPROVED,      // Təsdiqlənib
        ACTIVE,        // Aktiv
        PAID,          // Ödənilib
        OVERDUE,       // Gecikmiş
        REJECTED       // Rədd edilib
    }
}