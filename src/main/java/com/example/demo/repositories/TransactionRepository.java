package com.example.demo.repositories;

import com.example.demo.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {

    Page<Transaction> findAll(Pageable pageable);

    Page<Transaction> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<Transaction> findByFromIbanOrToIban(String fromIban, String toIban, Pageable pageable);

    Page<Transaction> findByFromIbanInOrToIbanInOrderByCreatedAtDesc(List<String> fromIbans, List<String> toIbans, Pageable pageable);

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.fromIban = ?1 AND t.createdAt BETWEEN ?2 AND ?3")
    Optional<BigDecimal> sumByFromIbanAndDate(String fromIban, LocalDateTime start, LocalDateTime end);



}
