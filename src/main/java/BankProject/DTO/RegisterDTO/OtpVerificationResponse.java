package BankProject.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OtpVerificationResponse {
    private String message;
    private Boolean verified;
    private String verificationToken;
}
