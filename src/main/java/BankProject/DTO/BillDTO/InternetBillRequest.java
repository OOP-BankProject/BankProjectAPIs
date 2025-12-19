package BankProject.DTO.BillDTO;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

// İnternet Ödəniş Request
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InternetBillRequest {
    @NotBlank(message = "Kart nömrəsi boş ola bilməz")
    private String cardNumber;

    @NotBlank(message = "PIN boş ola bilməz")
    private String pin;

    @NotBlank(message = "Provayder seçilməlidir")
    private String provider;

    @NotBlank(message = "Hesab ID-si boş ola bilməz")
    private String accountId;

    @NotNull(message = "Məbləğ boş ola bilməz")
    @DecimalMin(value = "1.00", message = "Minimum ödəniş 1 AZN-dir")
    @DecimalMax(value = "500.00", message = "Maksimum ödəniş 500 AZN-dir")
    private BigDecimal amount;
}
