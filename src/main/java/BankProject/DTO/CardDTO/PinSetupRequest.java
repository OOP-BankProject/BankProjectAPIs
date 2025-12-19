package BankProject.DTO.CardDTO;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// PIN Setup Request
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PinSetupRequest {
    @NotNull(message = "Card ID boş ola bilməz")
    private Long cardId;

    @NotBlank(message = "PIN boş ola bilməz")
    @Pattern(regexp = "^[0-9]{4}$", message = "PIN 4 rəqəmli olmalıdır")
    private String pin;

    @NotBlank(message = "PIN təkrarı boş ola bilməz")
    private String confirmPin;
}
