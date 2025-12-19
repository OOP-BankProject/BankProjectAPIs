package BankProject.DTO.TransferDTO;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotBlank;

import java.math.BigDecimal;

// Balans Artırma Request
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BalanceAddRequest {
    @NotBlank(message = "Kart nömrəsi boş ola bilməz")
    private String cardNumber;

    @NotBlank(message = "PIN boş ola bilməz")
    private String pin;

    @NotNull(message = "Məbləğ boş ola bilməz")
    @DecimalMin(value = "1.00", message = "Minimum məbləğ 1 AZN-dir")
    private BigDecimal amount;
}
