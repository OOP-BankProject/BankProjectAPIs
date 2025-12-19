package BankProject.DTO.BillDTO;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

// Mobil Operator Ödəniş Request
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MobileBillRequest {
    @NotBlank(message = "Kart nömrəsi boş ola bilməz")
    private String cardNumber;

    @NotBlank(message = "PIN boş ola bilməz")
    private String pin;

    @NotBlank(message = "Operator seçilməlidir")
    @Pattern(regexp = "^(AZERCELL|BAKCELL|NAR)$", message = "Keçərli operator: AZERCELL, BAKCELL, NAR")
    private String operator;

    @NotBlank(message = "Telefon nömrəsi boş ola bilməz")
    @Pattern(regexp = "^\\+994(50|51|55|70|77|99)[0-9]{7}$",
            message = "Telefon nömrəsi düzgün formatda olmalıdır")
    private String phoneNumber;

    @NotNull(message = "Məbləğ boş ola bilməz")
    @DecimalMin(value = "1.00", message = "Minimum ödəniş 1 AZN-dir")
    @DecimalMax(value = "100.00", message = "Maksimum ödəniş 100 AZN-dir")
    private BigDecimal amount;
}
