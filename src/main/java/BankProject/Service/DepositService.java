package BankProject.Service;

import BankProject.DTO.DepositDTO.*;
import BankProject.Entity.Card;
import BankProject.Entity.Deposit;
import BankProject.Entity.Transaction;
import BankProject.Entity.User;
import BankProject.Exceptions.*;
import BankProject.Repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DepositService {

    private final DepositRepository depositRepository;
    private final UserRepository userRepository;
    private final CardRepository cardRepository;
    private final TransactionRepository transactionRepository;
    private final PasswordEncoder passwordEncoder;

    public DepositService(DepositRepository depositRepository,
                          UserRepository userRepository,
                          CardRepository cardRepository,
                          TransactionRepository transactionRepository,
                          PasswordEncoder passwordEncoder) {
        this.depositRepository = depositRepository;
        this.userRepository = userRepository;
        this.cardRepository = cardRepository;
        this.transactionRepository = transactionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Depozit aç
    @Transactional
    public DepositOpenResponse openDeposit(DepositOpenRequest request) {
        log.info("Depozit açılır - FIN: {}, Amount: {}", request.getFin(), request.getAmount());

        User user = userRepository.findByFin(request.getFin())
                .orElseThrow(() -> new UserNotFoundException("İstifadəçi tapılmadı"));

        Card card = cardRepository.findById(request.getCardId())
                .orElseThrow(() -> new RegistrationException("Kart tapılmadı"));

        // Kartın istifadəçiyə aid olduğunu yoxla
        if (!card.getUser().getId().equals(user.getId())) {
            throw new RegistrationException("Kart bu istifadəçiyə aid deyil");
        }

        // PIN yoxla
        if (!passwordEncoder.matches(request.getPin(), card.getPin())) {
            throw new InvalidCredentialsException("Yanlış PIN");
        }

        // Depozit məbləği yoxlanışı
        if (request.getAmount().compareTo(new BigDecimal("100")) < 0) {
            throw new RegistrationException("Minimum depozit məbləği 100 AZN-dir");
        }

        // Balans yoxla
        if (card.getBalance().compareTo(request.getAmount()) < 0) {
            throw new RegistrationException("Balansda kifayət qədər məbləğ yoxdur");
        }

        // Müddət yoxlanışı
        if (request.getTermMonths() < 3 || request.getTermMonths() > 36) {
            throw new RegistrationException("Depozit müddəti 3-36 ay arasında olmalıdır");
        }

        // Faiz dərəcəsini təyin et
        BigDecimal interestRate = getInterestRateForDeposit(
                request.getDepositType(),
                request.getTermMonths()
        );

        // Ümumi məbləği hesabla
        BigDecimal totalAmount = calculateTotalAmount(
                request.getAmount(),
                interestRate,
                request.getTermMonths()
        );

        // Transaction yarat
        Transaction transaction = new Transaction();
        transaction.setTransactionId(generateTransactionId());
        transaction.setFromCard(card);
        transaction.setType(Transaction.TransactionType.DEPOSIT);
        transaction.setStatus(Transaction.TransactionStatus.PENDING);
        transaction.setAmount(request.getAmount());
        transaction.setFee(BigDecimal.ZERO);
        transaction.setDescription("Depozit açılışı");

        try {
            // Məbləği kartdan çıxart
            card.setBalance(card.getBalance().subtract(request.getAmount()));
            cardRepository.save(card);

            // Depozit yarat
            Deposit deposit = new Deposit();
            deposit.setUser(user);
            deposit.setCard(card);
            deposit.setDepositNumber(generateDepositNumber());
            deposit.setPrincipalAmount(request.getAmount());
            deposit.setInterestRate(interestRate);
            deposit.setTermMonths(request.getTermMonths());
            deposit.setTotalAmount(totalAmount);
            deposit.setDepositType(request.getDepositType());
            deposit.setStatus(Deposit.DepositStatus.ACTIVE);
            deposit.setAutoRenewal(request.getAutoRenewal() != null ? request.getAutoRenewal() : false);
            deposit.setStartDate(LocalDate.now());
            deposit.setMaturityDate(LocalDate.now().plusMonths(request.getTermMonths()));

            depositRepository.save(deposit);

            transaction.setStatus(Transaction.TransactionStatus.COMPLETED);
            transactionRepository.save(transaction);

            log.info("Depozit açıldı - Deposit Number: {}", deposit.getDepositNumber());

            return new DepositOpenResponse(
                    "Depozit uğurla açıldı",
                    deposit.getId(),
                    deposit.getDepositNumber(),
                    request.getAmount(),
                    interestRate,
                    totalAmount,
                    deposit.getMaturityDate(),
                    card.getBalance()
            );

        } catch (Exception e) {
            transaction.setStatus(Transaction.TransactionStatus.FAILED);
            transaction.setFailureReason(e.getMessage());
            transactionRepository.save(transaction);

            log.error("Depozit açılışı uğursuz - Transaction ID: {}", transaction.getTransactionId(), e);
            throw new RegistrationException("Depozit açılışı zamanı xəta baş verdi");
        }
    }

    // Depoziti bağla
    @Transactional
    public DepositCloseResponse closeDeposit(DepositCloseRequest request) {
        log.info("Depozit bağlanır - Deposit ID: {}", request.getDepositId());

        Deposit deposit = depositRepository.findById(request.getDepositId())
                .orElseThrow(() -> new RegistrationException("Depozit tapılmadı"));

        if (deposit.getStatus() != Deposit.DepositStatus.ACTIVE) {
            throw new RegistrationException("Depozit aktiv deyil");
        }

        Card card = deposit.getCard();

        // PIN yoxla
        if (!passwordEncoder.matches(request.getPin(), card.getPin())) {
            throw new InvalidCredentialsException("Yanlış PIN");
        }

        // Erkən bağlanma cəzası
        BigDecimal amountToReturn;
        boolean isEarlyClose = LocalDate.now().isBefore(deposit.getMaturityDate());

        if (isEarlyClose) {
            // Erkən bağlanmada yalnız əsas məbləğ qaytarılır
            amountToReturn = deposit.getPrincipalAmount();
            log.info("Erkən depozit bağlanması - Faiz məbləği itirildi");
        } else {
            // Müddət bitibsə tam məbləğ qaytarılır
            BigDecimal accruedInterest = calculateAccruedInterest(
                    deposit.getPrincipalAmount(),
                    deposit.getInterestRate(),
                    deposit.getTermMonths()
            );
            deposit.setAccruedInterest(accruedInterest);
            amountToReturn = deposit.getPrincipalAmount().add(accruedInterest);
        }

        // Transaction yarat
        Transaction transaction = new Transaction();
        transaction.setTransactionId(generateTransactionId());
        transaction.setToCard(card);
        transaction.setType(Transaction.TransactionType.WITHDRAWAL);
        transaction.setStatus(Transaction.TransactionStatus.PENDING);
        transaction.setAmount(amountToReturn);
        transaction.setFee(BigDecimal.ZERO);
        transaction.setDescription("Depozit bağlanışı - " + deposit.getDepositNumber());

        try {
            // Məbləği karta köçür
            card.setBalance(card.getBalance().add(amountToReturn));
            cardRepository.save(card);

            // Depoziti bağla
            deposit.setStatus(Deposit.DepositStatus.CLOSED);
            deposit.setClosedDate(LocalDate.now());
            depositRepository.save(deposit);

            transaction.setStatus(Transaction.TransactionStatus.COMPLETED);
            transactionRepository.save(transaction);

            log.info("Depozit bağlandı - Deposit Number: {}", deposit.getDepositNumber());

            return new DepositCloseResponse(
                    isEarlyClose ?
                            "Depozit erkən bağlandı. Yalnız əsas məbləğ qaytarıldı" :
                            "Depozit uğurla bağlandı",
                    deposit.getDepositNumber(),
                    amountToReturn,
                    deposit.getAccruedInterest(),
                    isEarlyClose,
                    card.getBalance()
            );

        } catch (Exception e) {
            transaction.setStatus(Transaction.TransactionStatus.FAILED);
            transaction.setFailureReason(e.getMessage());
            transactionRepository.save(transaction);

            log.error("Depozit bağlanışı uğursuz - Transaction ID: {}", transaction.getTransactionId(), e);
            throw new RegistrationException("Depozit bağlanışı zamanı xəta baş verdi");
        }
    }

    // İstifadəçinin depozitlərini siyahıla
    @Transactional(readOnly = true)
    public List<DepositListResponse> getUserDeposits(String fin) {
        User user = userRepository.findByFin(fin)
                .orElseThrow(() -> new UserNotFoundException("İstifadəçi tapılmadı"));

        List<Deposit> deposits = depositRepository.findByUserId(user.getId());

        return deposits.stream()
                .map(deposit -> new DepositListResponse(
                        deposit.getId(),
                        deposit.getDepositNumber(),
                        deposit.getDepositType().toString(),
                        deposit.getPrincipalAmount(),
                        deposit.getInterestRate(),
                        deposit.getTotalAmount(),
                        deposit.getStatus().toString(),
                        deposit.getStartDate(),
                        deposit.getMaturityDate()
                ))
                .collect(Collectors.toList());
    }

    // Helper metodlar
    private BigDecimal getInterestRateForDeposit(Deposit.DepositType depositType, Integer termMonths) {
        BigDecimal baseRate = switch (depositType) {
            case FIXED -> new BigDecimal("8.00");
            case DEMAND -> new BigDecimal("2.00");
            case ACCUMULATIVE -> new BigDecimal("6.00");
        };

        // Müddətə görə bonus
        if (termMonths >= 12) {
            baseRate = baseRate.add(new BigDecimal("1.00"));
        }
        if (termMonths >= 24) {
            baseRate = baseRate.add(new BigDecimal("1.00"));
        }

        return baseRate;
    }

    private BigDecimal calculateTotalAmount(BigDecimal principal,
                                            BigDecimal annualRate,
                                            Integer months) {
        BigDecimal monthlyRate = annualRate.divide(new BigDecimal("1200"), 6, RoundingMode.HALF_UP);
        BigDecimal totalInterest = principal.multiply(monthlyRate).multiply(new BigDecimal(months));

        return principal.add(totalInterest).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateAccruedInterest(BigDecimal principal,
                                                BigDecimal annualRate,
                                                Integer months) {
        BigDecimal monthlyRate = annualRate.divide(new BigDecimal("1200"), 6, RoundingMode.HALF_UP);
        return principal.multiply(monthlyRate).multiply(new BigDecimal(months))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private String generateDepositNumber() {
        return "DP" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }

    private String generateTransactionId() {
        return "TXN" + UUID.randomUUID().toString().replace("-", "").substring(0, 20).toUpperCase();
    }
}