package com.example.demo.repositories;

import com.example.demo.common.enums.AccountType;
import com.example.demo.entity.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    boolean existsByIban(String iban);

    Optional<Account> findByIban(String iban);

    void deleteByIban(String iban);

    Page<Account> findByActive(boolean active, Pageable pageable);

    Page<Account> findAll(Pageable pageable);

    Page<Account> findByOwnerId(Long ownerId, Pageable pageable);

    List<Account> findByOwnerId(Long ownerId);

    List<Account> findByOwnerIdIn(List<Long> ownerIds);

    List<Account> findByOwnerIdAndType(Long ownerId, AccountType type);

    List<Account> findByOwnerIdInAndType(List<Long> ownerIds, AccountType type);
}
