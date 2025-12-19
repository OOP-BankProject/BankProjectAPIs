package BankProject.Service;


import BankProject.DTO.CardDTO.*;
import BankProject.Entity.Card;
import BankProject.Entity.User;
import BankProject.Exceptions.*;
import BankProject.Repository.CardRepository;
import BankProject.Repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CardService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String CARD_PREFIX = "4169"; // Bank prefix
    private static final Integer MAX_FAILED_PIN_ATTEMPTS = 3;

    public CardService(CardRepository cardRepository,
                       UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {
        this.cardRepository = cardRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Kart sifariş et
    @Transactional
    public CardOrderResponse orderCard(CardOrderRequest request) {
        log.info("Kart sifarişi - User FIN: {}", request.getFin());

        User user = userRepository.findByFin(request.getFin())
                .orElseThrow(() -> new UserNotFoundException("İstifadəçi tapılmadı"));

        // Əgər kredit kartı sifarişidirsə, kredit limitini yoxla
        if (request.getCardType() == Card.CardType.CREDIT) {
            if (request.getCreditLimit() == null ||
                    request.getCreditLimit().compareTo(new BigDecimal("100")) < 0) {
                throw new RegistrationException("Kredit limiti minimum 100 AZN olmalıdır");
            }
        }

        // Yeni kart yarat
        Card card = new Card();
        card.setUser(user);
        card.setCardNumber(generateCardNumber());
        card.setCvv(passwordEncoder.encode(generateCVV()));
        card.setExpiryDate(LocalDate.now().plusYears(3));
        card.setCardHolderName(user.getFirstName() + " " + user.getLastName());
        card.setCardType(request.getCardType());
        card.setStatus(Card.CardStatus.PENDING);
        card.setIsVirtual(request.getIsVirtual() != null ? request.getIsVirtual() : false);
        card.setBalance(BigDecimal.ZERO);

        if (request.getCardType() == Card.CardType.CREDIT) {
            card.setCreditLimit(request.getCreditLimit());
            card.setUsedCredit(BigDecimal.ZERO);
        }

        cardRepository.save(card);

        log.info("Kart yaradıldı - Card Number: {}", maskCardNumber(card.getCardNumber()));

        return new CardOrderResponse(
                "Kartınız uğurla sifariş edildi. 5-7 iş günü ərzində çatdırılacaq",
                card.getId(),
                maskCardNumber(card.getCardNumber()),
                card.getCardType().toString(),
                card.getStatus().toString()
        );
    }

    // PIN təyin et
    @Transactional
    public PinSetupResponse setupPin(PinSetupRequest request) {
        log.info("PIN təyin edilir - Card ID: {}", request.getCardId());

        Card card = cardRepository.findById(request.getCardId())
                .orElseThrow(() -> new RegistrationException("Kart tapılmadı"));

        // PIN validation
        if (!request.getPin().matches("^[0-9]{4}$")) {
            throw new RegistrationException("PIN 4 rəqəmdən ibarət olmalıdır");
        }

        if (!request.getPin().equals(request.getConfirmPin())) {
            throw new PasswordMismatchException("PIN kodları uyğun gəlmir");
        }

        card.setPin(passwordEncoder.encode(request.getPin()));
        card.setIsPinSet(true);
        card.setStatus(Card.CardStatus.ACTIVE);
        cardRepository.save(card);

        log.info("PIN uğurla təyin edildi - Card ID: {}", request.getCardId());

        return new PinSetupResponse(
                "PIN kodu uğurla təyin edildi. Kartınız aktivdir",
                card.getId(),
                maskCardNumber(card.getCardNumber())
        );
    }

    // Kartları siyahıla
    @Transactional(readOnly = true)
    public List<CardListResponse> getUserCards(String fin) {
        User user = userRepository.findByFin(fin)
                .orElseThrow(() -> new UserNotFoundException("İstifadəçi tapılmadı"));

        List<Card> cards = cardRepository.findByUserId(user.getId());

        return cards.stream()
                .map(card -> new CardListResponse(
                        card.getId(),
                        maskCardNumber(card.getCardNumber()),
                        card.getCardHolderName(),
                        card.getCardType().toString(),
                        card.getStatus().toString(),
                        card.getBalance(),
                        card.getExpiryDate(),
                        card.getIsVirtual(),
                        card.getCreditLimit(),
                        card.getUsedCredit()
                ))
                .collect(Collectors.toList());
    }

    // Kart məlumatları
    @Transactional(readOnly = true)
    public CardDetailsResponse getCardDetails(Long cardId, String pin) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RegistrationException("Kart tapılmadı"));

        // PIN yoxla
        if (!card.getIsPinSet() || !passwordEncoder.matches(pin, card.getPin())) {
            card.setFailedPinAttempts(card.getFailedPinAttempts() + 1);

            if (card.getFailedPinAttempts() >= MAX_FAILED_PIN_ATTEMPTS) {
                card.setStatus(Card.CardStatus.BLOCKED);
                cardRepository.save(card);
                throw new RegistrationException("Kart bloklanıb. Çoxlu səhv PIN cəhdi");
            }

            cardRepository.save(card);
            throw new InvalidCredentialsException("Yanlış PIN. Qalan cəhd: " +
                    (MAX_FAILED_PIN_ATTEMPTS - card.getFailedPinAttempts()));
        }

        // Uğurlu giriş - failed attempts sıfırla
        card.setFailedPinAttempts(0);
        cardRepository.save(card);

        return new CardDetailsResponse(
                card.getId(),
                card.getCardNumber(),
                card.getCardHolderName(),
                card.getExpiryDate(),
                card.getCardType().toString(),
                card.getStatus().toString(),
                card.getBalance(),
                card.getCreditLimit(),
                card.getUsedCredit()
        );
    }

    // Kartı blokla
    @Transactional
    public CardBlockResponse blockCard(CardBlockRequest request) {
        Card card = cardRepository.findById(request.getCardId())
                .orElseThrow(() -> new RegistrationException("Kart tapılmadı"));

        if (!passwordEncoder.matches(request.getPin(), card.getPin())) {
            throw new InvalidCredentialsException("Yanlış PIN");
        }

        card.setStatus(Card.CardStatus.BLOCKED);
        cardRepository.save(card);

        log.info("Kart bloklandı - Card ID: {}", request.getCardId());

        return new CardBlockResponse(
                "Kart uğurla bloklandı",
                card.getId(),
                maskCardNumber(card.getCardNumber())
        );
    }

    // Helper metodlar
    private String generateCardNumber() {
        SecureRandom random = new SecureRandom();
        StringBuilder cardNumber = new StringBuilder(CARD_PREFIX);

        for (int i = 0; i < 12; i++) {
            cardNumber.append(random.nextInt(10));
        }

        // Kartın unikal olduğundan əmin ol
        while (cardRepository.existsByCardNumber(cardNumber.toString())) {
            cardNumber = new StringBuilder(CARD_PREFIX);
            for (int i = 0; i < 12; i++) {
                cardNumber.append(random.nextInt(10));
            }
        }

        return cardNumber.toString();
    }

    private String generateCVV() {
        Random random = new Random();
        return String.format("%03d", random.nextInt(1000));
    }

    private String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() != 16) {
            return cardNumber;
        }
        return cardNumber.substring(0, 4) + " **** **** " + cardNumber.substring(12);
    }
}