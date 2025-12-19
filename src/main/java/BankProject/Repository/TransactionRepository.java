package BankProject.Repository;

import BankProject.Entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByFromCardIdOrderByCreatedAtDesc(Long fromCardId);
    List<Transaction> findByToCardIdOrderByCreatedAtDesc(Long toCardId);
    List<Transaction> findByFromCardIdOrToCardIdOrderByCreatedAtDesc(Long fromCardId, Long toCardId);
    List<Transaction> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}
