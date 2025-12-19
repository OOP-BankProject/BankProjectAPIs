package BankProject.DTO.DepositDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepositCloseResponse {
    private String message;
    private String depositNumber;
    private BigDecimal returnedAmount;
    private BigDecimal accruedInterest;
    private Boolean isEarlyClose;
    private BigDecimal cardBalance;
}
