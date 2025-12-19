
package BankProject.Controller;

import BankProject.DTO.ApiResponse;
import BankProject.DTO.TransferDTO.*;
import BankProject.Service.TransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transfer")
@Tag(name = "Transfer Əməliyyatları", description = "Kart-kart transfer API-ları")
public class TransferController {

    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @PostMapping("/card-to-card")
    @Operation(summary = "Kart-Kart Transfer",
            description = "Bir kartdan digərinə pul köçürün")
    public ResponseEntity<ApiResponse<TransferResponse>> cardToCardTransfer(
            @Valid @RequestBody CardToCardTransferRequest request) {

        TransferResponse response = transferService.cardToCardTransfer(request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("Transfer uğurla tamamlandı", response));
    }

    @PostMapping("/add-balance")
    @Operation(summary = "Balans Artır (Test)",
            description = "Test məqsədilə karta balans əlavə edin")
    public ResponseEntity<ApiResponse<BalanceAddResponse>> addBalance(
            @Valid @RequestBody BalanceAddRequest request) {

        BalanceAddResponse response = transferService.addBalance(request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("Balans artırıldı", response));
    }
}
