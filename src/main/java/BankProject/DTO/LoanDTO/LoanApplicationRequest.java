package BankProject.DTO.LoanDTO;

import BankProject.Entity.Loan;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanApplicationRequest {
    @NotBlank(message = "FIN boş ola bilməz")
    private String fin;

    @NotNull(message = "Card ID boş ola bilməz")
    private Long cardId;

    @NotNull(message = "Kredit növü seçilməlidir")
    private Loan.LoanType loanType;

    @NotNull(message = "Məbləğ boş ola bilməz")
    @DecimalMin(value = "500.00", message = "Minimum kredit məbləği 500 AZN-dir")
    @DecimalMax(value = "50000.00", message = "Maksimum kredit məbləği 50,000 AZN-dir")
    private BigDecimal amount;

    @NotNull(message = "Müddət seçilməlidir")
    @Min(value = 6, message = "Minimum müddət 6 aydır")
    @Max(value = 60, message = "Maksimum müddət 60 aydır")
    private Integer termMonths;
}
