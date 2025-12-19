package BankProject.DTO.LoanDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanListResponse {
    private Long loanId;
    private String loanNumber;
    private String loanType;
    private BigDecimal principalAmount;
    private BigDecimal remainingAmount;
    private BigDecimal monthlyPayment;
    private Integer remainingMonths;
    private String status;
    private LocalDate nextPaymentDate;
}