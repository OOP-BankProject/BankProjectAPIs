package BankProject.DTO.PasswordReset;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordResponse {
    private String message;
    private String fin;
}