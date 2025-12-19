package BankProject.DTO.BillDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BillPaymentResponse {
    private String message;
    private String transactionId;
    private BigDecimal amount;
    private String recipientAccount;
    private BigDecimal remainingBalance;
    private LocalDateTime timestamp;
}