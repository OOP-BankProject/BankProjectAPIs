package BankProject.DTO.CardDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

// Kart Siyahısı Response
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CardListResponse {
    private Long cardId;
    private String cardNumber;
    private String cardHolderName;
    private String cardType;
    private String status;
    private BigDecimal balance;
    private LocalDate expiryDate;
    private Boolean isVirtual;
    private BigDecimal creditLimit;
    private BigDecimal usedCredit;
}
