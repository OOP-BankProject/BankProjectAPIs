package BankProject.Service;

import BankProject.DTO.TransferDTO.*;
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
import java.math.RoundingMode;
import java.util.UUID;

@Service
@Slf4j
public class TransferService {

    private final CardRepository cardRepository;
    private final TransactionRepository transactionRepository;
    private final PasswordEncoder passwordEncoder;

    private static final BigDecimal TRANSFER_FEE_RATE = new BigDecimal("0.005"); // 0.5%
    private static final BigDecimal MIN_TRANSFER_FEE = new BigDecimal("0.10");
    private static final BigDecimal MAX_TRANSFER_FEE = new BigDecimal("5.00");

    public TransferService(CardRepository cardRepository,
                           TransactionRepository transactionRepository,
                           PasswordEncoder passwordEncoder) {
        this.cardRepository = cardRepository;
        this.transactionRepository = transactionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Kart-kart transfer
    @Transactional
    public TransferResponse cardToCardTransfer(CardToCardTransferRequest request) {
        log.info("Transfer başladı - From Card: {}", maskCardNumber(request.getFromCardNumber()));

        // Göndərən kartı tap
        Card fromCard = cardRepository.findByCardNumber(request.getFromCardNumber())
                .orElseThrow(() -> new RegistrationException("Göndərən kart tapılmadı"));

        // PIN yoxla
        if (!passwordEncoder.matches(request.getPin(), fromCard.getPin())) {
            throw new InvalidCredentialsException("Yanlış PIN");
        }

        // Kart statusu yoxla
        if (fromCard.getStatus() != Card.CardStatus.ACTIVE) {
            throw new RegistrationException("Kart aktiv deyil");
        }

        // Alan kartı tap
        Card toCard = cardRepository.findByCardNumber(request.getToCardNumber())
                .orElseThrow(() -> new RegistrationException("Alan kart tapılmadı"));

        if (toCard.getStatus() != Card.CardStatus.ACTIVE) {
            throw new RegistrationException("Alan kart aktiv deyil");
        }

        // Eyni karta köçürmə yoxlanışı
        if (fromCard.getId().equals(toCard.getId())) {
            throw new RegistrationException("Eyni karta köçürmə mümkün deyil");
        }

        // Məbləğ yoxlanışı
        if (request.getAmount().compareTo(new BigDecimal("0.01")) < 0) {
            throw new RegistrationException("Məbləğ minimum 0.01 AZN olmalıdır");
        }

        // Komissiya hesabla
        BigDecimal fee = calculateTransferFee(request.getAmount());
        BigDecimal totalAmount = request.getAmount().add(fee);

        // Balans yoxla
        if (fromCard.getBalance().compareTo(totalAmount) < 0) {
            throw new RegistrationException("Balansda kifayət qədər məbləğ yoxdur");
        }

        // Transaction yarat
        Transaction transaction = new Transaction();
        transaction.setTransactionId(generateTransactionId());
        transaction.setFromCard(fromCard);
        transaction.setToCard(toCard);
        transaction.setType(Transaction.TransactionType.CARD_TO_CARD);
        transaction.setStatus(Transaction.TransactionStatus.PENDING);
        transaction.setAmount(request.getAmount());
        transaction.setFee(fee);
        transaction.setDescription(request.getDescription());

        try {
            // Pulu köçür
            fromCard.setBalance(fromCard.getBalance().subtract(totalAmount));
            toCard.setBalance(toCard.getBalance().add(request.getAmount()));

            cardRepository.save(fromCard);
            cardRepository.save(toCard);

            transaction.setStatus(Transaction.TransactionStatus.COMPLETED);
            transactionRepository.save(transaction);

            log.info("Transfer tamamlandı - Transaction ID: {}", transaction.getTransactionId());

            return new TransferResponse(
                    "Transfer uğurla həyata keçirildi",
                    transaction.getTransactionId(),
                    request.getAmount(),
                    fee,
                    fromCard.getBalance(),
                    transaction.getCreatedAt()
            );

        } catch (Exception e) {
            transaction.setStatus(Transaction.TransactionStatus.FAILED);
            transaction.setFailureReason(e.getMessage());
            transactionRepository.save(transaction);

            log.error("Transfer uğursuz - Transaction ID: {}", transaction.getTransactionId(), e);
            throw new RegistrationException("Transfer zamanı xəta baş verdi");
        }
    }

    // Balans artır (Test üçün - real layihədə olmamalıdır)
    @Transactional
    public BalanceAddResponse addBalance(BalanceAddRequest request) {
        Card card = cardRepository.findByCardNumber(request.getCardNumber())
                .orElseThrow(() -> new RegistrationException("Kart tapılmadı"));

        if (!passwordEncoder.matches(request.getPin(), card.getPin())) {
            throw new InvalidCredentialsException("Yanlış PIN");
        }

        card.setBalance(card.getBalance().add(request.getAmount()));
        cardRepository.save(card);

        log.info("Balans artırıldı - Card: {}, Amount: {}",
                maskCardNumber(card.getCardNumber()), request.getAmount());

        return new BalanceAddResponse(
                "Balans uğurla artırıldı",
                card.getBalance()
        );
    }

    // Komissiya hesablama
    private BigDecimal calculateTransferFee(BigDecimal amount) {
        BigDecimal fee = amount.multiply(TRANSFER_FEE_RATE)
                .setScale(2, RoundingMode.HALF_UP);

        if (fee.compareTo(MIN_TRANSFER_FEE) < 0) {
            return MIN_TRANSFER_FEE;
        }
        if (fee.compareTo(MAX_TRANSFER_FEE) > 0) {
            return MAX_TRANSFER_FEE;
        }

        return fee;
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
