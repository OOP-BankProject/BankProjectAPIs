package BankProject.DTO.TransferDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BalanceAddResponse {
    private String message;
    private BigDecimal newBalance;
}
