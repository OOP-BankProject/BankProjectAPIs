package BankProject.DTO.DepositDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepositListResponse {
    private Long depositId;
    private String depositNumber;
    private String depositType;
    private BigDecimal principalAmount;
    private BigDecimal interestRate;
    private BigDecimal totalAmount;
    private String status;
    private LocalDate startDate;
    private LocalDate maturityDate;
}
