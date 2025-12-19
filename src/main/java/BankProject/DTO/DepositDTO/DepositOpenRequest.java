package BankProject.DTO.DepositDTO;

import BankProject.Entity.Deposit;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepositOpenRequest {
    @NotBlank(message = "FIN boş ola bilməz")
    private String fin;

    @NotNull(message = "Card ID boş ola bilməz")
    private Long cardId;

    @NotBlank(message = "PIN boş ola bilməz")
    private String pin;

    @NotNull(message = "Depozit növü seçilməlidir")
    private Deposit.DepositType depositType;

    @NotNull(message = "Məbləğ boş ola bilməz")
    @DecimalMin(value = "100.00", message = "Minimum depozit məbləği 100 AZN-dir")
    private BigDecimal amount;

    @NotNull(message = "Müddət seçilməlidir")
    @Min(value = 3, message = "Minimum müddət 3 aydır")
    @Max(value = 36, message = "Maksimum müddət 36 aydır")
    private Integer termMonths;

    private Boolean autoRenewal;
}
