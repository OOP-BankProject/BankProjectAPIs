package BankProject.DTO.LoanDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanApplicationResponse {
    private String message;
    private Long loanId;
    private String loanNumber;
    private String status;
    private BigDecimal monthlyPayment;
}