package BankProject.DTO.LoginDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserExistsResponse {
    private String message;
    private String redirectTo;
    private String fin;
}
