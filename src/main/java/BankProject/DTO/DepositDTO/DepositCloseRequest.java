package BankProject.DTO.DepositDTO;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepositCloseRequest {
    @NotNull(message = "Deposit ID boş ola bilməz")
    private Long depositId;

    @NotBlank(message = "PIN boş ola bilməz")
    private String pin;
}
