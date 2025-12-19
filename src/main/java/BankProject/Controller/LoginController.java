package BankProject.Controller;

import BankProject.DTO.ApiResponse;
import BankProject.DTO.LoginDTO.LoginRequest;
import BankProject.DTO.LoginDTO.LoginResponse;
import BankProject.DTO.LoginDTO.RefreshTokenRequest;
import BankProject.DTO.LoginDTO.RefreshTokenResponse;
import BankProject.Service.LoginService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api")
@Tag(name = "Autentifikasiya", description = "Login və token yenilənməsi API-ları")
public class LoginController {

    private final LoginService loginService;

    public LoginController(LoginService loginService) {
        this.loginService = loginService;
    }

    @PostMapping("/login")
    @Operation(summary = "Daxil Ol",
            description = "FIN və parol ilə sistemə daxil olun")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        LoginResponse response = loginService.login(request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("Uğurla daxil oldunuz", response));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Token Yenilə",
            description = "Refresh token ilə yeni access token alın")
    public ResponseEntity<ApiResponse<RefreshTokenResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request) {

        RefreshTokenResponse response = loginService.refreshToken(request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("Token yeniləndi", response));
    }
}
