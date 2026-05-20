package com.example.demo.repositories;

import com.example.demo.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Page<Transaction> findAll(Pageable pageable);

    Page<Transaction> findByFromIbanOrToIban(String fromIban, String toIban, Pageable pageable);

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.fromIban = ?1 AND t.createdAt BETWEEN ?2 AND ?3")
    Optional<BigDecimal> sumByFromIbanAndDate(String fromIban, LocalDateTime start, LocalDateTime end);

    @Query("""
    SELECT t FROM Transaction t
    WHERE (:startDate IS NULL OR t.createdAt >= :startDate)
    AND (:endDate IS NULL OR t.createdAt <= :endDate)
    AND (:minAmount IS NULL OR t.amount >= :minAmount)
    AND (:maxAmount IS NULL OR t.amount <= :maxAmount)
    AND (:exactAmount IS NULL OR t.amount = :exactAmount)
    AND (
        :iban IS NULL
        OR LOWER(t.fromIban) LIKE LOWER(CONCAT('%', :iban, '%'))
        OR LOWER(t.toIban) LIKE LOWER(CONCAT('%', :iban, '%'))
    )
""")
    Page<Transaction> searchTransactions(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("minAmount") BigDecimal minAmount,
            @Param("maxAmount") BigDecimal maxAmount,
            @Param("exactAmount") BigDecimal exactAmount,
            @Param("iban") String iban,
            Pageable pageable
    );
}
