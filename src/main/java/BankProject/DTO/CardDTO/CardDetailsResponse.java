package BankProject.DTO.CardDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

// Kart Detalları Response
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CardDetailsResponse {
    private Long cardId;
    private String cardNumber;
    private String cardHolderName;
    private LocalDate expiryDate;
    private String cardType;
    private String status;
    private BigDecimal balance;
    private BigDecimal creditLimit;
    private BigDecimal usedCredit;
}
