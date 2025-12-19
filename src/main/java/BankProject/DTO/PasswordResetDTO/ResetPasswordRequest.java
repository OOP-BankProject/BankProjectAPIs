package BankProject.DTO.PasswordResetDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// 3. Yeni Parol Təyin Et
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordRequest {

    @NotBlank(message = "Password reset token boş ola bilməz")
    private String passwordResetToken;

    @NotBlank(message = "Yeni parol boş ola bilməz")
    @Size(min = 6, max = 100, message = "Parol minimum 6 simvoldan ibarət olmalıdır")
    private String newPassword;

    @NotBlank(message = "Parol təkrarı boş ola bilməz")
    private String confirmPassword;
}