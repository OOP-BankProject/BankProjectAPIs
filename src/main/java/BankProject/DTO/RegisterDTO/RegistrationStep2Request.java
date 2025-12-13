package BankProject.DTO;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class RegistrationStep2Request {

    @NotBlank(message = "Verification token boş ola bilməz")
    private String verificationToken;

    @NotBlank(message = "Ad boş ola bilməz")
    @Size(min = 2, max = 50, message = "Ad 2-50 simvol arasında olmalıdır")
    private String firstName;

    @NotBlank(message = "Soyad boş ola bilməz")
    @Size(min = 2, max = 50, message = "Soyad 2-50 simvol arasında olmalıdır")
    private String lastName;

    @NotNull(message = "Doğum tarixi boş ola bilməz")
    @Past(message = "Doğum tarixi keçmiş tarix olmalıdır")
    private LocalDate dateOfBirth;

    @NotBlank(message = "Email boş ola bilməz")
    @Email(message = "Email düzgün formatda olmalıdır")
    private String email;

    @NotBlank(message = "Parol boş ola bilməz")
    @Size(min = 6, max = 100, message = "Parol minimum 6 simvoldan ibarət olmalıdır")
    private String password;

    @NotBlank(message = "Parol təkrarı boş ola bilməz")
    private String confirmPassword;
}
