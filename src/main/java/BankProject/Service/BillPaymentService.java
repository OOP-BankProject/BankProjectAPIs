package BankProject.Service;

import BankProject.DTO.BillDTO.*;
import BankProject.Entity.Card;
import BankProject.Entity.Transaction;
import BankProject.Exceptions.*;
import BankProject.Repository.CardRepository;
import BankProject.Repository.TransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@Slf4j
public class BillPaymentService {

    private final CardRepository cardRepository;
    private final TransactionRepository transactionRepository;
    private final PasswordEncoder passwordEncoder;

    public BillPaymentService(CardRepository cardRepository,
                              TransactionRepository transactionRepository,
                              PasswordEncoder passwordEncoder) {
        this.cardRepository = cardRepository;
        this.transactionRepository = transactionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Mobil operator ödənişi
    @Transactional
    public BillPaymentResponse payMobileBill(MobileBillRequest request) {
        log.info("Mobil ödəniş - Card: {}, Operator: {}",
                maskCardNumber(request.getCardNumber()), request.getOperator());

        Card card = getAndValidateCard(request.getCardNumber(), request.getPin());

        // Telefon nömrəsi yoxla
        if (!request.getPhoneNumber().matches("^\\+994(50|51|55|70|77|99)[0-9]{7}$")) {
            throw new RegistrationException("Telefon nömrəsi düzgün formatda deyil");
        }

        // Operator-u BillType-a çevir
        Transaction.BillType billType = getOperatorBillType(request.getOperator());

        return processPayment(
                card,
                request.getAmount(),
                Transaction.TransactionType.BILL_MOBILE,
                billType,
                request.getPhoneNumber(),
                "Mobil operator ödənişi - " + request.getOperator()
        );
    }

    // Kommunal ödəniş (Su, Qaz, İşıq)
    @Transactional
    public BillPaymentResponse payUtilityBill(UtilityBillRequest request) {
        log.info("Kommunal ödəniş - Card: {}, Type: {}",
                maskCardNumber(request.getCardNumber()), request.getUtilityType());

        Card card = getAndValidateCard(request.getCardNumber(), request.getPin());

        // Abonent nömrəsi yoxla
        if (request.getSubscriberNumber() == null || request.getSubscriberNumber().length() < 5) {
            throw new RegistrationException("Abonent nömrəsi düzgün deyil");
        }

        return processPayment(
                card,
                request.getAmount(),
                getUtilityTransactionType(request.getUtilityType()),
                getBillType(request.getUtilityType()),
                request.getSubscriberNumber(),
                request.getUtilityType() + " ödənişi"
        );
    }

    // İnternet ödənişi
    @Transactional
    public BillPaymentResponse payInternetBill(InternetBillRequest request) {
        log.info("İnternet ödənişi - Card: {}, Provider: {}",
                maskCardNumber(request.getCardNumber()), request.getProvider());

        Card card = getAndValidateCard(request.getCardNumber(), request.getPin());

        // Account ID yoxla
        if (request.getAccountId() == null || request.getAccountId().isEmpty()) {
            throw new RegistrationException("Hesab nömrəsi boş ola bilməz");
        }

        Transaction.BillType billType = "AZERTELECOM".equals(request.getProvider())
                ? Transaction.BillType.AZERTELECOM
                : Transaction.BillType.INTERNET_OTHER;

        return processPayment(
                card,
                request.getAmount(),
                Transaction.TransactionType.BILL_INTERNET,
                billType,
                request.getAccountId(),
                "İnternet ödənişi - " + request.getProvider()
        );
    }

    // Ümumi ödəniş prosesi
    private BillPaymentResponse processPayment(Card card,
                                               BigDecimal amount,
                                               Transaction.TransactionType transactionType,
                                               Transaction.BillType billType,
                                               String recipientAccount,
                                               String description) {
        // Məbləğ yoxlanışı
        if (amount.compareTo(new BigDecimal("1.00")) < 0) {
            throw new RegistrationException("Minimum ödəniş məbləği 1 AZN-dir");
        }

        // Maksimum limit
        if (amount.compareTo(new BigDecimal("1000.00")) > 0) {
            throw new RegistrationException("Maksimum ödəniş məbləği 1000 AZN-dir");
        }

        // Balans yoxla
        if (card.getBalance().compareTo(amount) < 0) {
            throw new RegistrationException("Balansda kifayət qədər məbləğ yoxdur");
        }

        // Transaction yarat
        Transaction transaction = new Transaction();
        transaction.setTransactionId(generateTransactionId());
        transaction.setFromCard(card);
        transaction.setType(transactionType);
        transaction.setStatus(Transaction.TransactionStatus.PENDING);
        transaction.setAmount(amount);
        transaction.setFee(BigDecimal.ZERO); // Bill ödənişlərində komissiya yoxdur
        transaction.setBillType(billType);
        transaction.setRecipientAccount(recipientAccount);
        transaction.setDescription(description);

        try {
            // Ödənişi həyata keçir
            card.setBalance(card.getBalance().subtract(amount));
            cardRepository.save(card);

            transaction.setStatus(Transaction.TransactionStatus.COMPLETED);
            transactionRepository.save(transaction);

            log.info("Ödəniş tamamlandı - Transaction ID: {}", transaction.getTransactionId());

            return new BillPaymentResponse(
                    "Ödəniş uğurla həyata keçirildi",
                    transaction.getTransactionId(),
                    amount,
                    recipientAccount,
                    card.getBalance(),
                    transaction.getCreatedAt()
            );

        } catch (Exception e) {
            transaction.setStatus(Transaction.TransactionStatus.FAILED);
            transaction.setFailureReason(e.getMessage());
            transactionRepository.save(transaction);

            log.error("Ödəniş uğursuz - Transaction ID: {}", transaction.getTransactionId(), e);
            throw new RegistrationException("Ödəniş zamanı xəta baş verdi");
        }
    }

    // Helper metodlar
    private Card getAndValidateCard(String cardNumber, String pin) {
        Card card = cardRepository.findByCardNumber(cardNumber)
                .orElseThrow(() -> new RegistrationException("Kart tapılmadı"));

        if (!passwordEncoder.matches(pin, card.getPin())) {
            throw new InvalidCredentialsException("Yanlış PIN");
        }

        if (card.getStatus() != Card.CardStatus.ACTIVE) {
            throw new RegistrationException("Kart aktiv deyil");
        }

        return card;
    }

    private Transaction.BillType getOperatorBillType(String operator) {
        return switch (operator.toUpperCase()) {
            case "AZERCELL" -> Transaction.BillType.AZERCELL;
            case "BAKCELL" -> Transaction.BillType.BAKCELL;
            case "NAR" -> Transaction.BillType.NAR;
            default -> throw new RegistrationException("Naməlum operator");
        };
    }

    private Transaction.TransactionType getUtilityTransactionType(String utilityType) {
        return switch (utilityType.toUpperCase()) {
            case "WATER" -> Transaction.TransactionType.BILL_UTILITY_WATER;
            case "GAS" -> Transaction.TransactionType.BILL_UTILITY_GAS;
            case "ELECTRICITY" -> Transaction.TransactionType.BILL_UTILITY_ELECTRICITY;
            default -> throw new RegistrationException("Naməlum kommunal növü");
        };
    }

    private Transaction.BillType getBillType(String utilityType) {
        return switch (utilityType.toUpperCase()) {
            case "WATER" -> Transaction.BillType.AZERSU;
            case "GAS" -> Transaction.BillType.AZERIQAZ;
            case "ELECTRICITY" -> Transaction.BillType.AZERENERJI;
            default -> throw new RegistrationException("Naməlum kommunal növü");
        };
    }

    private String generateTransactionId() {
        return "TXN" + UUID.randomUUID().toString().replace("-", "").substring(0, 20).toUpperCase();
    }

    private String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() != 16) {
            return cardNumber;
        }
        return cardNumber.substring(0, 4) + " **** **** " + cardNumber.substring(12);
    }
}