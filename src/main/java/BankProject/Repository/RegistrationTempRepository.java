package BankProject.Repository;

import BankProject.Entity.RegistrationTemp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface RegistrationTempRepository extends JpaRepository<RegistrationTemp, Long> {

    Optional<RegistrationTemp> findByFin(String fin);

    Optional<RegistrationTemp> findByPhoneNumber(String phoneNumber);

    @Modifying
    @Transactional
    @Query("DELETE FROM RegistrationTemp r WHERE r.createdAt < :expiryTime")
    void deleteExpiredRecords(LocalDateTime expiryTime);

    @Modifying
    @Transactional
    void deleteByFin(String fin);
}