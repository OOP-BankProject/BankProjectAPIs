package BankProject.DTO.BillDTO;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UtilityBillRequest {
    @NotBlank(message = "Kart nömrəsi boş ola bilməz")
    private String cardNumber;

    @NotBlank(message = "PIN boş ola bilməz")
    private String pin;

    @NotBlank(message = "Kommunal növü seçilməlidir")
    @Pattern(regexp = "^(WATER|GAS|ELECTRICITY)$",
            message = "Keçərli növ: WATER, GAS, ELECTRICITY")
    private String utilityType;

    @NotBlank(message = "Abonent nömrəsi boş ola bilməz")
    @Size(min = 5, max = 20, message = "Abonent nömrəsi 5-20 simvol olmalıdır")
    private String subscriberNumber;

    @NotNull(message = "Məbləğ boş ola bilməz")
    @DecimalMin(value = "1.00", message = "Minimum ödəniş 1 AZN-dir")
    @DecimalMax(value = "1000.00", message = "Maksimum ödəniş 1000 AZN-dir")
    private BigDecimal amount;
}
