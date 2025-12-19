package BankProject.Controller;

import BankProject.DTO.ApiResponse;
import BankProject.DTO.LoanDTO.*;
import BankProject.Service.LoanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loans")
@Tag(name = "Kredit İdarəetməsi", description = "Kredit müraciəti və ödəniş API-ları")
public class LoanController {

    private final LoanService loanService;

    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }

    @PostMapping("/apply")
    @Operation(summary = "Kredit Müraciəti",
            description = "Yeni kredit üçün müraciət edin")
    public ResponseEntity<ApiResponse<LoanApplicationResponse>> applyForLoan(
            @Valid @RequestBody LoanApplicationRequest request) {

        LoanApplicationResponse response = loanService.applyForLoan(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Kredit müraciəti qəbul edildi", response));
    }

    @PostMapping("/approve")
    @Operation(summary = "Krediti Təsdiqlə (Admin)",
            description = "Kredit müraciətini təsdiqləyin və ya rədd edin")
    public ResponseEntity<ApiResponse<LoanApprovalResponse>> approveLoan(
            @Valid @RequestBody LoanApprovalRequest request) {

        LoanApprovalResponse response = loanService.approveLoan(request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(
                        request.getApproved() ? "Kredit təsdiqləndi" : "Kredit rədd edildi",
                        response));
    }

    @PostMapping("/pay")
    @Operation(summary = "Kredit Ödənişi",
            description = "Aylıq kredit ödənişi edin")
    public ResponseEntity<ApiResponse<LoanPaymentResponse>> makePayment(
            @Valid @RequestBody LoanPaymentRequest request) {

        LoanPaymentResponse response = loanService.makePayment(request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("Kredit ödənişi tamamlandı", response));
    }

    @GetMapping("/my-loans")
    @Operation(summary = "Kreditlərimi Göstər",
            description = "İstifadəçinin bütün kreditlərini siyahılayır")
    public ResponseEntity<ApiResponse<List<LoanListResponse>>> getUserLoans(
            @RequestParam String fin) {

        List<LoanListResponse> response = loanService.getUserLoans(fin);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("Kreditlər yükləndi", response));
    }
}
