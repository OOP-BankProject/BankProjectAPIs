package BankProject.DTO.CardDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Kart Blokla Response
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CardBlockResponse {
    private String message;
    private Long cardId;
    private String cardNumber;
}
