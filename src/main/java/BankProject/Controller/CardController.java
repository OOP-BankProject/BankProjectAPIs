package BankProject.Controller;

import BankProject.DTO.ApiResponse;
import BankProject.DTO.CardDTO.*;
import BankProject.Service.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cards")
@Tag(name = "Kart İdarəetməsi", description = "Kart sifariş, PIN təyin etmə və idarəetmə API-ları")
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @PostMapping("/order")
    @Operation(summary = "Kart Sifariş Et",
            description = "Yeni debit və ya kredit kartı sifariş edin")
    public ResponseEntity<ApiResponse<CardOrderResponse>> orderCard(
            @Valid @RequestBody CardOrderRequest request) {

        CardOrderResponse response = cardService.orderCard(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Kart uğurla sifariş edildi", response));
    }

    @PostMapping("/setup-pin")
    @Operation(summary = "PIN Təyin Et",
            description = "Yeni kart üçün 4 rəqəmli PIN kodu təyin edin")
    public ResponseEntity<ApiResponse<PinSetupResponse>> setupPin(
            @Valid @RequestBody PinSetupRequest request) {

        PinSetupResponse response = cardService.setupPin(request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("PIN uğurla təyin edildi", response));
    }

    @GetMapping("/my-cards")
    @Operation(summary = "Kartlarımı Göstər",
            description = "İstifadəçinin bütün kartlarını siyahılayır")
    public ResponseEntity<ApiResponse<List<CardListResponse>>> getUserCards(
            @RequestParam String fin) {

        List<CardListResponse> response = cardService.getUserCards(fin);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("Kartlar uğurla yükləndi", response));
    }

    @GetMapping("/details")
    @Operation(summary = "Kart Detalları",
            description = "Kartın tam məlumatlarını göstərir (PIN tələb olunur)")
    public ResponseEntity<ApiResponse<CardDetailsResponse>> getCardDetails(
            @RequestParam Long cardId,
            @RequestParam String pin) {

        CardDetailsResponse response = cardService.getCardDetails(cardId, pin);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("Kart detalları yükləndi", response));
    }

    @PostMapping("/block")
    @Operation(summary = "Kartı Blokla",
            description = "Kartı blokla (itki və ya oğurluq halında)")
    public ResponseEntity<ApiResponse<CardBlockResponse>> blockCard(
            @Valid @RequestBody CardBlockRequest request) {

        CardBlockResponse response = cardService.blockCard(request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("Kart bloklandı", response));
    }
}