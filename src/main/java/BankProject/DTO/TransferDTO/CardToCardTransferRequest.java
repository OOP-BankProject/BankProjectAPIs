package BankProject.DTO.TransferDTO;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

// Kart-Kart Transfer Request
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CardToCardTransferRequest {
    @NotBlank(message = "Göndərən kart nömrəsi boş ola bilməz")
    @Pattern(regexp = "^[0-9]{16}$", message = "Kart nömrəsi 16 rəqəmli olmalıdır")
    private String fromCardNumber;

    @NotBlank(message = "Alan kart nömrəsi boş ola bilməz")
    @Pattern(regexp = "^[0-9]{16}$", message = "Kart nömrəsi 16 rəqəmli olmalıdır")
    private String toCardNumber;

    @NotNull(message = "Məbləğ boş ola bilməz")
    @DecimalMin(value = "0.01", message = "Məbləğ minimum 0.01 AZN olmalıdır")
    private BigDecimal amount;

    @NotBlank(message = "PIN boş ola bilməz")
    private String pin;

    private String description;
}
