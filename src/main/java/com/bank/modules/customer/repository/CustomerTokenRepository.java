package com.bank.modules.customer.repository;

import com.bank.modules.customer.entity.CustomerToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CustomerTokenRepository extends JpaRepository<CustomerToken, Integer> {

    @Query(value = """
      select t from CustomerToken t 
      inner join Customer c on t.customer.id = c.id
      where c.id = :id and (t.expired = false or t.revoked = false)
      """)
    List<CustomerToken> findAllValidTokenByCustomer(Long id);

    Optional<CustomerToken> findByToken(String token);
}
