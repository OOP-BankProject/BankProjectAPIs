package BankProject.DTO.CardDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Kart Sifariş Response
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CardOrderResponse {
    private String message;
    private Long cardId;
    private String cardNumber;
    private String cardType;
    private String status;
}
