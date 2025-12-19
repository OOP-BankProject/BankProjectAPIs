package BankProject.DTO.CardDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// PIN Setup Response
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PinSetupResponse {
    private String message;
    private Long cardId;
    private String cardNumber;
}
