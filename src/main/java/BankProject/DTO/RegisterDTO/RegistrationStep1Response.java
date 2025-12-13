package BankProject.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationStep1Response {
    private String message;
    private String sessionToken;
    private Integer otpExpirySeconds;
}