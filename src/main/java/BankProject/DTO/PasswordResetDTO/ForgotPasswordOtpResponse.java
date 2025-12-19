package BankProject.DTO.PasswordResetDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ForgotPasswordOtpResponse {
    private String message;
    private Boolean verified;
    private String passwordResetToken;
}