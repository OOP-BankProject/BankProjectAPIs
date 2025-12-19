package BankProject.DTO.LoanDTO;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanPaymentRequest {
    @NotNull(message = "Loan ID boş ola bilməz")
    private Long loanId;

    @NotNull(message = "Card ID boş ola bilməz")
    private Long cardId;

    @NotBlank(message = "PIN boş ola bilməz")
    private String pin;

    private BigDecimal amount; // Null olarsa, aylıq ödəniş məbləği götürülür
}
