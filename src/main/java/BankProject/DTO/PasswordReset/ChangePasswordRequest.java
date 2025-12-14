package BankProject.DTO.PasswordReset;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// 4. Login Olmuş İstifadəçi Parolunu Dəyişsin
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordRequest {

    @NotBlank(message = "FIN boş ola bilməz")
    private String fin;

    @NotBlank(message = "Köhnə parol boş ola bilməz")
    private String oldPassword;

    @NotBlank(message = "Yeni parol boş ola bilməz")
    @Size(min = 6, max = 100, message = "Parol minimum 6 simvoldan ibarət olmalıdır")
    private String newPassword;

    @NotBlank(message = "Parol təkrarı boş ola bilməz")
    private String confirmPassword;
}