package com.example.demo.repositories;

import com.example.demo.models.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, String> {

    boolean existsByIban(String iban);

    Optional<Account> findByIban(String iban);

    Page<Account> findByActive(boolean active, Pageable pageable);

    Page<Account> findAll(Pageable pageable);

    List<Account> findByOwnerId(Long ownerId);
}
