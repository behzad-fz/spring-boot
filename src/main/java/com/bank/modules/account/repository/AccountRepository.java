package com.bank.modules.account.repository;

import com.bank.modules.account.entity.Account;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    Account findByUUID(String uuid);

    Account findByIban(String iban);

    /**
     * Pessimistic row lock (SELECT ... FOR UPDATE) for balance mutation paths,
     * so concurrent transactions serialize on the account row instead of racing
     * through the read-modify-write cycle.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Account a where a.UUID = :uuid")
    Account findByUUIDForUpdate(@Param("uuid") String uuid);

    List<Account> findByCustomerId(Long customerId);
}
