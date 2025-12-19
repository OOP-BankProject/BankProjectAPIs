package BankProject.Repository;

import BankProject.Entity.Deposit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface DepositRepository extends JpaRepository<Deposit, Long> {
    List<Deposit> findByUserId(Long userId);
    Optional<Deposit> findByDepositNumber(String depositNumber);
    List<Deposit> findByStatus(Deposit.DepositStatus status);
}
