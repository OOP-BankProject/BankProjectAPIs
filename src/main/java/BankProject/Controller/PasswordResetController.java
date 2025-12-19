package BankProject.Controller;

import BankProject.DTO.ApiResponse;
import BankProject.DTO.PasswordResetDTO.*;
import BankProject.Service.PasswordResetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/password")
@Tag(name = "Parol İdarəetməsi", description = "Parol sıfırlama və dəyişdirmə API-ları")
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    public PasswordResetController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/forgot/step1")
    @Operation(summary = "Parol Unutdum - Addım 1",
            description = "FIN və telefon nömrəsi daxil edilir, OTP göndərilir")
    public ResponseEntity<ApiResponse<ForgotPasswordStep1Response>> initiateForgotPassword(
            @Valid @RequestBody ForgotPasswordStep1Request request) {

        ForgotPasswordStep1Response response = passwordResetService.initiateForgotPassword(request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("OTP kodu göndərildi", response));
    }

    @PostMapping("/forgot/verify-otp")
    @Operation(summary = "Parol Sıfırlama OTP Yoxlanışı",
            description = "Göndərilən OTP kodunu təsdiqləyir")
    public ResponseEntity<ApiResponse<ForgotPasswordOtpResponse>> verifyForgotPasswordOtp(
            @Valid @RequestBody ForgotPasswordOtpRequest request) {

        ForgotPasswordOtpResponse response = passwordResetService.verifyForgotPasswordOtp(request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("OTP kodu təsdiqləndi", response));
    }

    @PostMapping("/reset")
    @Operation(summary = "Yeni Parol Təyin Et",
            description = "OTP təsdiqlənəndən sonra yeni parol təyin edilir")
    public ResponseEntity<ApiResponse<ResetPasswordResponse>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {

        ResetPasswordResponse response = passwordResetService.resetPassword(request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("Parol uğurla yeniləndi", response));
    }

    @PostMapping("/change")
    @Operation(summary = "Parol Dəyişdir",
            description = "Login olmuş istifadəçi köhnə parol ilə yeni parol təyin edir")
    public ResponseEntity<ApiResponse<ChangePasswordResponse>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request) {

        ChangePasswordResponse response = passwordResetService.changePassword(request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("Parol dəyişdirildi", response));
    }
}
