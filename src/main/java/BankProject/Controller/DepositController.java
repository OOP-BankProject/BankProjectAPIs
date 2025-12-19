package BankProject.Controller;

import BankProject.DTO.ApiResponse;
import BankProject.DTO.DepositDTO.*;
import BankProject.Service.DepositService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/deposits")
@Tag(name = "Depozit İdarəetməsi", description = "Depozit açma və bağlama API-ları")
public class DepositController {

    private final DepositService depositService;

    public DepositController(DepositService depositService) {
        this.depositService = depositService;
    }

    @PostMapping("/open")
    @Operation(summary = "Depozit Aç",
            description = "Yeni depozit hesabı açın")
    public ResponseEntity<ApiResponse<DepositOpenResponse>> openDeposit(
            @Valid @RequestBody DepositOpenRequest request) {

        DepositOpenResponse response = depositService.openDeposit(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Depozit uğurla açıldı", response));
    }

    @PostMapping("/close")
    @Operation(summary = "Depozit Bağla",
            description = "Depozit hesabını bağlayın və pulu karta köçürün")
    public ResponseEntity<ApiResponse<DepositCloseResponse>> closeDeposit(
            @Valid @RequestBody DepositCloseRequest request) {

        DepositCloseResponse response = depositService.closeDeposit(request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("Depozit bağlandı", response));
    }

    @GetMapping("/my-deposits")
    @Operation(summary = "Depozitlərimi Göstər",
            description = "İstifadəçinin bütün depozitlərini siyahılayır")
    public ResponseEntity<ApiResponse<List<DepositListResponse>>> getUserDeposits(
            @RequestParam String fin) {

        List<DepositListResponse> response = depositService.getUserDeposits(fin);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("Depozitlər yükləndi", response));
    }
}