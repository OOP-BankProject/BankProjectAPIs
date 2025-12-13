package BankProject.DTO;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OtpVerificationRequest {

    @NotBlank(message = "Session token boş ola bilməz")
    private String sessionToken;

    @NotBlank(message = "OTP kodu boş ola bilməz")
    @Pattern(regexp = "^[0-9]{6}$", message = "OTP kodu 6 rəqəmli olmalıdır")
    private String otpCode;
}
