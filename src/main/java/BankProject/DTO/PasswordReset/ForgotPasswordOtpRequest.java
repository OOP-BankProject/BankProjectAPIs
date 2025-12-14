package BankProject.DTO.PasswordReset;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ForgotPasswordOtpRequest {

    @NotBlank(message = "Reset token boş ola bilməz")
    private String resetToken;

    @NotBlank(message = "OTP kodu boş ola bilməz")
    @Pattern(regexp = "^[0-9]{6}$", message = "OTP kodu 6 rəqəmli olmalıdır")
    private String otpCode;
}
