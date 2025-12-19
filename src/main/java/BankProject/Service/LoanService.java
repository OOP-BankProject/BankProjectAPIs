package BankProject.Service;

import BankProject.DTO.LoanDTO.*;
import BankProject.Entity.Card;
import BankProject.Entity.Loan;
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
public class LoanService {

    private final LoanRepository loanRepository;
    private final UserRepository userRepository;
    private final CardRepository cardRepository;
    private final TransactionRepository transactionRepository;
    private final PasswordEncoder passwordEncoder;

    public LoanService(LoanRepository loanRepository,
                       UserRepository userRepository,
                       CardRepository cardRepository,
                       TransactionRepository transactionRepository,
                       PasswordEncoder passwordEncoder) {
        this.loanRepository = loanRepository;
        this.userRepository = userRepository;
        this.cardRepository = cardRepository;
        this.transactionRepository = transactionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Kredit müraciəti
    @Transactional
    public LoanApplicationResponse applyForLoan(LoanApplicationRequest request) {
        log.info("Kredit müraciəti - FIN: {}, Type: {}", request.getFin(), request.getLoanType());

        User user = userRepository.findByFin(request.getFin())
                .orElseThrow(() -> new UserNotFoundException("İstifadəçi tapılmadı"));

        Card card = cardRepository.findById(request.getCardId())
                .orElseThrow(() -> new RegistrationException("Kart tapılmadı"));

        // Kartın istifadəçiyə aid olduğunu yoxla
        if (!card.getUser().getId().equals(user.getId())) {
            throw new RegistrationException("Kart bu istifadəçiyə aid deyil");
        }

        // Kredit məbləği yoxlanışı
        if (request.getAmount().compareTo(new BigDecimal("500")) < 0) {
            throw new RegistrationException("Minimum kredit məbləği 500 AZN-dir");
        }

        if (request.getAmount().compareTo(new BigDecimal("50000")) > 0) {
            throw new RegistrationException("Maksimum kredit məbləği 50,000 AZN-dir");
        }

        // Müddət yoxlanışı
        if (request.getTermMonths() < 6 || request.getTermMonths() > 60) {
            throw new RegistrationException("Kredit müddəti 6-60 ay arasında olmalıdır");
        }

        // Faiz dərəcəsini təyin et (kredit növünə görə)
        BigDecimal interestRate = getInterestRateForLoanType(request.getLoanType());

        // Aylıq ödənişi hesabla
        BigDecimal monthlyPayment = calculateMonthlyPayment(
                request.getAmount(),
                interestRate,
                request.getTermMonths()
        );

        // Kredit yarat
        Loan loan = new Loan();
        loan.setUser(user);
        loan.setCard(card);
        loan.setLoanNumber(generateLoanNumber());
        loan.setLoanType(request.getLoanType());
        loan.setPrincipalAmount(request.getAmount());
        loan.setInterestRate(interestRate);
        loan.setTermMonths(request.getTermMonths());
        loan.setMonthlyPayment(monthlyPayment);
        loan.setRemainingAmount(request.getAmount());
        loan.setRemainingMonths(request.getTermMonths());
        loan.setStatus(Loan.LoanStatus.PENDING);
        loan.setStartDate(LocalDate.now());
        loan.setNextPaymentDate(LocalDate.now().plusMonths(1));

        loanRepository.save(loan);

        log.info("Kredit müraciəti yaradıldı - Loan Number: {}", loan.getLoanNumber());

        return new LoanApplicationResponse(
                "Kredit müraciətiniz qəbul edildi. 1-3 iş günü ərzində cavab veriləcək",
                loan.getId(),
                loan.getLoanNumber(),
                loan.getStatus().toString(),
                monthlyPayment
        );
    }

    // Krediti təsdiqlə (Admin funksiyası)
    @Transactional
    public LoanApprovalResponse approveLoan(LoanApprovalRequest request) {
        Loan loan = loanRepository.findById(request.getLoanId())
                .orElseThrow(() -> new RegistrationException("Kredit tapılmadı"));

        if (loan.getStatus() != Loan.LoanStatus.PENDING) {
            throw new RegistrationException("Yalnız gözləyən kreditlər təsdiqlənə bilər");
        }

        if (request.getApproved()) {
            loan.setStatus(Loan.LoanStatus.APPROVED);

            // Krediti karta köçür
            Card card = loan.getCard();
            card.setBalance(card.getBalance().add(loan.getPrincipalAmount()));
            cardRepository.save(card);

            loan.setStatus(Loan.LoanStatus.ACTIVE);
            log.info("Kredit təsdiqləndi və karta köçürüldü - Loan Number: {}", loan.getLoanNumber());
        } else {
            loan.setStatus(Loan.LoanStatus.REJECTED);
            log.info("Kredit rədd edildi - Loan Number: {}", loan.getLoanNumber());
        }

        loanRepository.save(loan);

        return new LoanApprovalResponse(
                request.getApproved() ? "Kredit təsdiqləndi və karta köçürüldü" : "Kredit rədd edildi",
                loan.getId(),
                loan.getLoanNumber(),
                loan.getStatus().toString()
        );
    }

    // Kredit ödənişi
    @Transactional
    public LoanPaymentResponse makePayment(LoanPaymentRequest request) {
        log.info("Kredit ödənişi - Loan ID: {}", request.getLoanId());

        Loan loan = loanRepository.findById(request.getLoanId())
                .orElseThrow(() -> new RegistrationException("Kredit tapılmadı"));

        if (loan.getStatus() != Loan.LoanStatus.ACTIVE) {
            throw new RegistrationException("Kredit aktiv deyil");
        }

        Card card = cardRepository.findById(request.getCardId())
                .orElseThrow(() -> new RegistrationException("Kart tapılmadı"));

        if (!passwordEncoder.matches(request.getPin(), card.getPin())) {
            throw new InvalidCredentialsException("Yanlış PIN");
        }

        // Ödəniş məbləğini yoxla
        BigDecimal paymentAmount = request.getAmount() != null ?
                request.getAmount() : loan.getMonthlyPayment();

        if (paymentAmount.compareTo(loan.getRemainingAmount()) > 0) {
            paymentAmount = loan.getRemainingAmount();
        }

        if (card.getBalance().compareTo(paymentAmount) < 0) {
            throw new RegistrationException("Balansda kifayət qədər məbləğ yoxdur");
        }

        // Transaction yarat
        Transaction transaction = new Transaction();
        transaction.setTransactionId(generateTransactionId());
        transaction.setFromCard(card);
        transaction.setType(Transaction.TransactionType.LOAN_PAYMENT);
        transaction.setStatus(Transaction.TransactionStatus.PENDING);
        transaction.setAmount(paymentAmount);
        transaction.setFee(BigDecimal.ZERO);
        transaction.setDescription("Kredit ödənişi - " + loan.getLoanNumber());

        try {
            // Ödənişi həyata keçir
            card.setBalance(card.getBalance().subtract(paymentAmount));
            cardRepository.save(card);

            // Krediti yenilə
            loan.setRemainingAmount(loan.getRemainingAmount().subtract(paymentAmount));
            loan.setTotalPaid(loan.getTotalPaid().add(paymentAmount));

            if (loan.getRemainingAmount().compareTo(BigDecimal.ZERO) <= 0) {
                loan.setStatus(Loan.LoanStatus.PAID);
                loan.setEndDate(LocalDate.now());
                loan.setRemainingMonths(0);
            } else {
                loan.setRemainingMonths(loan.getRemainingMonths() - 1);
                loan.setNextPaymentDate(loan.getNextPaymentDate().plusMonths(1));
            }

            loanRepository.save(loan);

            transaction.setStatus(Transaction.TransactionStatus.COMPLETED);
            transactionRepository.save(transaction);

            log.info("Kredit ödənişi tamamlandı - Transaction ID: {}", transaction.getTransactionId());

            return new LoanPaymentResponse(
                    "Ödəniş uğurla həyata keçirildi",
                    transaction.getTransactionId(),
                    paymentAmount,
                    loan.getRemainingAmount(),
                    loan.getRemainingMonths(),
                    card.getBalance()
            );

        } catch (Exception e) {
            transaction.setStatus(Transaction.TransactionStatus.FAILED);
            transaction.setFailureReason(e.getMessage());
            transactionRepository.save(transaction);

            log.error("Kredit ödənişi uğursuz - Transaction ID: {}", transaction.getTransactionId(), e);
            throw new RegistrationException("Ödəniş zamanı xəta baş verdi");
        }
    }

    // İstifadəçinin kreditlərini siyahıla
    @Transactional(readOnly = true)
    public List<LoanListResponse> getUserLoans(String fin) {
        User user = userRepository.findByFin(fin)
                .orElseThrow(() -> new UserNotFoundException("İstifadəçi tapılmadı"));

        List<Loan> loans = loanRepository.findByUserId(user.getId());

        return loans.stream()
                .map(loan -> new LoanListResponse(
                        loan.getId(),
                        loan.getLoanNumber(),
                        loan.getLoanType().toString(),
                        loan.getPrincipalAmount(),
                        loan.getRemainingAmount(),
                        loan.getMonthlyPayment(),
                        loan.getRemainingMonths(),
                        loan.getStatus().toString(),
                        loan.getNextPaymentDate()
                ))
                .collect(Collectors.toList());
    }

    // Helper metodlar
    private BigDecimal getInterestRateForLoanType(Loan.LoanType loanType) {
        return switch (loanType) {
            case CONSUMER -> new BigDecimal("18.00");
            case MORTGAGE -> new BigDecimal("12.00");
            case AUTO -> new BigDecimal("15.00");
            case BUSINESS -> new BigDecimal("20.00");
            case EDUCATION -> new BigDecimal("10.00");
        };
    }

    private BigDecimal calculateMonthlyPayment(BigDecimal principal,
                                               BigDecimal annualRate,
                                               Integer months) {
        BigDecimal monthlyRate = annualRate.divide(new BigDecimal("1200"), 6, RoundingMode.HALF_UP);
        BigDecimal onePlusR = BigDecimal.ONE.add(monthlyRate);
        BigDecimal powerN = onePlusR.pow(months);

        BigDecimal numerator = principal.multiply(monthlyRate).multiply(powerN);
        BigDecimal denominator = powerN.subtract(BigDecimal.ONE);

        return numerator.divide(denominator, 2, RoundingMode.HALF_UP);
    }

    private String generateLoanNumber() {
        return "LN" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }

    private String generateTransactionId() {
        return "TXN" + UUID.randomUUID().toString().replace("-", "").substring(0, 20).toUpperCase();
    }
}