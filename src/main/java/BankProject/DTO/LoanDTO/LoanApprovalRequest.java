package BankProject.DTO.LoanDTO;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanApprovalRequest {
    @NotNull(message = "Loan ID boş ola bilməz")
    private Long loanId;

    @NotNull(message = "Təsdiq statusu göstərilməlidir")
    private Boolean approved;

    private String rejectionReason;
}
