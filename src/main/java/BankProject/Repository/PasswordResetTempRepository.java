package BankProject.Repository;

import BankProject.Entity.PasswordResetTemp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PasswordResetTempRepository extends JpaRepository<PasswordResetTemp, Long> {

    Optional<PasswordResetTemp> findByFin(String fin);

    @Modifying
    @Transactional
    @Query("DELETE FROM PasswordResetTemp p WHERE p.createdAt < :expiryTime")
    void deleteExpiredRecords(LocalDateTime expiryTime);

    @Modifying
    @Transactional
    void deleteByFin(String fin);
}