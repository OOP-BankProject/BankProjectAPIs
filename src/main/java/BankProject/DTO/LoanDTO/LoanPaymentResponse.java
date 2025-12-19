package BankProject.DTO.LoanDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanPaymentResponse {
    private String message;
    private String transactionId;
    private BigDecimal paidAmount;
    private BigDecimal remainingAmount;
    private Integer remainingMonths;
    private BigDecimal cardBalance;
}
