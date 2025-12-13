package BankProject.DTO;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationStep1Request {

    @NotBlank(message = "FIN boş ola bilməz")
    @Pattern(regexp = "^[A-Z0-9]{7}$", message = "FIN 7 simvollu olmalıdır")
    private String fin;

    @NotBlank(message = "Telefon nömrəsi boş ola bilməz")
    @Pattern(regexp = "^\\+994(50|51|55|70|77|99)[0-9]{7}$",
            message = "Telefon nömrəsi düzgün formatda olmalıdır (+994XXXXXXXXX)")
    private String phoneNumber;
}
