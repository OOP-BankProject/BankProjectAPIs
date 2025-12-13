package BankProject.DTO.LoginDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {
    private String message;
    private String accessToken;
    private String refreshToken;
    private Long userId;
    private String email;
    private String phoneNumber;
    private String firstName;
    private String lastName;
}
