package BankProject.DTO.LoginDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @NotBlank(message = "FIN boş ola bilməz")
    @Pattern(regexp = "^[A-Z0-9]{7}$", message = "FIN 7 simvollu olmalıdır")
    private String fin;

    @NotBlank(message = "Parol boş ola bilməz")
    private String password;
}
