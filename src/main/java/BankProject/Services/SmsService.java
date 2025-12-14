package BankProject.Services;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SmsService {

    private static final Logger log = LoggerFactory.getLogger(SmsService.class);

    public void sendOtp(String phoneNumber, String otpCode) {
        log.info("SMS göndərilir - Telefon: {}, OTP: {}", phoneNumber, otpCode);
        String message = String.format("Bank qeydiyyatı üçün OTP kodunuz: %s. 5 dəqiqə ərzində etibarlıdır.", otpCode);
        // TODO: Real SMS gateway ilə inteqrasiya
        log.info("SMS uğurla göndərildi");
    }

    public void sendPasswordResetOtp(String phoneNumber, String otpCode) {
        log.info("Parol sıfırlama SMS göndərilir - Telefon: {}, OTP: {}", phoneNumber, otpCode);
        String message = String.format("Parol sıfırlama üçün OTP kodunuz: %s. 5 dəqiqə ərzində etibarlıdır.", otpCode);
        // TODO: Real SMS gateway ilə inteqrasiya
        log.info("Parol sıfırlama SMS uğurla göndərildi");
    }
}
