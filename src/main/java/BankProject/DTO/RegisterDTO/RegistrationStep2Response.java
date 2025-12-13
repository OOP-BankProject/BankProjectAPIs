package BankProject.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegistrationStep2Response {
    private String message;
    private Long userId;
    private String email;
    private String phoneNumber;
}
