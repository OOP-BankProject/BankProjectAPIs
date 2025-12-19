package BankProject.DTO.CardDTO;

import BankProject.Entity.Card;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

// Kart Sifariş Request
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CardOrderRequest {
    @NotBlank(message = "FIN boş ola bilməz")
    private String fin;

    @NotNull(message = "Kart növü seçilməlidir")
    private Card.CardType cardType;

    private Boolean isVirtual;

    private BigDecimal creditLimit; // Yalnız kredit kartı üçün
}