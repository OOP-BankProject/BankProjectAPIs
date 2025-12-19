package BankProject.DTO.DepositDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepositOpenResponse {
    private String message;
    private Long depositId;
    private String depositNumber;
    private BigDecimal principalAmount;
    private BigDecimal interestRate;
    private BigDecimal totalAmount;
    private LocalDate maturityDate;
    private BigDecimal remainingCardBalance;
}
