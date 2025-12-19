package BankProject.DTO.LoanDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanApprovalResponse {
    private String message;
    private Long loanId;
    private String loanNumber;
    private String status;
}
