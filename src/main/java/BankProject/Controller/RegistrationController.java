package BankProject.Controller;

import BankProject.DTO.*;
import BankProject.DTO.ApiResponse;
import BankProject.DTO.OtpVerificationRequest;
import BankProject.DTO.OtpVerificationResponse;
import BankProject.DTO.RegistrationStep1Request;
import BankProject.DTO.RegistrationStep1Response;
import BankProject.DTO.RegistrationStep2Request;
import BankProject.DTO.RegistrationStep2Response;
import BankProject.DTO.ResendOtpRequest;
import BankProject.Services.RegistrationService;
import io.swagger.v3.oas.annotations.Operation;

import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/register")
@Tag(name = "Qeydiyyat", description = "İstifadəçi qeydiyyatı API-ları")
public class RegistrationController {

    private final RegistrationService registrationService;

    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @PostMapping("/step1")
    @Operation(summary = "Qeydiyyat Addım 1",
            description = "FIN və telefon nömrəsi daxil edilir, OTP göndərilir")
    public ResponseEntity<ApiResponse<RegistrationStep1Response>> initiateRegistration(
            @Valid @RequestBody RegistrationStep1Request request) {

        RegistrationStep1Response response = registrationService.initiateRegistration(request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("OTP kodu göndərildi", response));
    }

    @PostMapping("/verify-otp")
    @Operation(summary = "OTP Yoxlanışı",
            description = "Göndərilən OTP kodunu təsdiqləyir")
    public ResponseEntity<ApiResponse<OtpVerificationResponse>> verifyOtp(
            @Valid @RequestBody OtpVerificationRequest request) {

        OtpVerificationResponse response = registrationService.verifyOtp(request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("OTP kodu təsdiqləndi", response));
    }

    @PostMapping("/resend-otp")
    @Operation(summary = "OTP Təkrar Göndər",
            description = "Yeni OTP kodu göndərir")
    public ResponseEntity<ApiResponse<RegistrationStep1Response>> resendOtp(
            @Valid @RequestBody ResendOtpRequest request) {

        RegistrationStep1Response response = registrationService.resendOtp(request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("Yeni OTP kodu göndərildi", response));
    }

    @PostMapping("/step2")
    @Operation(summary = "Qeydiyyat Addım 2",
            description = "Şəxsi məlumatlar daxil edilir və qeydiyyat tamamlanır")
    public ResponseEntity<ApiResponse<RegistrationStep2Response>> completeRegistration(
            @Valid @RequestBody RegistrationStep2Request request) {

        RegistrationStep2Response response = registrationService.completeRegistration(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Qeydiyyat uğurla tamamlandı", response));
    }

    @GetMapping("/health")
    @Operation(summary = "Health Check", description = "API-nin işlək olub-olmadığını yoxlayır")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("API işləyir", "OK"));
    }
}