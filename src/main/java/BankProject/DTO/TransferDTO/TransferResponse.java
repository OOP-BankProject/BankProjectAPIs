package BankProject.DTO.TransferDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// Transfer Response
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransferResponse {
    private String message;
    private String transactionId;
    private BigDecimal amount;
    private BigDecimal fee;
    private BigDecimal remainingBalance;
    private LocalDateTime timestamp;
}
