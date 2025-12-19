package BankProject.DTO.CardDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Kart Blokla Request
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CardBlockRequest {
    @NotNull(message = "Card ID boş ola bilməz")
    private Long cardId;

    @NotBlank(message = "PIN boş ola bilməz")
    private String pin;
}
