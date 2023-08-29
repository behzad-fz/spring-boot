package com.bank.modules.transaction.repository;

import com.bank.modules.transaction.entity.Recipient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecipientRepository extends JpaRepository<Recipient, Long> {
}
