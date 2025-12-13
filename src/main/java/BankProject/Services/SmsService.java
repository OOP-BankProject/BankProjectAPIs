package BankProject.Services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SmsService {
    public void sendOtp(String phoneNumber,String otpCode){
        log.info("SMS: {} - OTP:{}",phoneNumber,otpCode);
    }
}
