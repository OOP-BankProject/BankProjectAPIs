package BankProject.DTO.PasswordResetDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ForgotPasswordStep1Response {
    private String message;
    private String resetToken;
    private Integer otpExpirySeconds;
}