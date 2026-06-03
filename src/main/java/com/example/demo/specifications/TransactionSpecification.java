package com.example.demo.specifications;

import com.example.demo.dtos.TransactionSearchRequest;
import com.example.demo.entity.Transaction;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public class TransactionSpecification {

    public static Specification<Transaction> withFilters(
            TransactionSearchRequest filter,
            List<String> ownedIbans
    ) {
        return (root, query, criteriaBuilder) -> {
            var predicates = criteriaBuilder.conjunction();

            // Customer ownership filter:
            // only transactions where fromIban or toIban belongs to the logged-in customer
            if (ownedIbans != null && !ownedIbans.isEmpty()) {
                predicates = criteriaBuilder.and(
                        predicates,
                        criteriaBuilder.or(
                                root.get("fromIban").in(ownedIbans),
                                root.get("toIban").in(ownedIbans)
                        )
                );
            }

            if (filter.startDate() != null) {
                predicates = criteriaBuilder.and(
                        predicates,
                        criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), filter.startDate())
                );
            }

            if (filter.endDate() != null) {
                predicates = criteriaBuilder.and(
                        predicates,
                        criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), filter.endDate())
                );
            }

            if (filter.minAmount() != null) {
                predicates = criteriaBuilder.and(
                        predicates,
                        criteriaBuilder.greaterThanOrEqualTo(root.get("amount"), filter.minAmount())
                );
            }

            if (filter.maxAmount() != null) {
                predicates = criteriaBuilder.and(
                        predicates,
                        criteriaBuilder.lessThanOrEqualTo(root.get("amount"), filter.maxAmount())
                );
            }

            if (filter.exactAmount() != null) {
                predicates = criteriaBuilder.and(
                        predicates,
                        criteriaBuilder.equal(root.get("amount"), filter.exactAmount())
                );
            }

            if (filter.iban() != null && !filter.iban().isBlank()) {
                String ibanPattern = "%" + filter.iban().toLowerCase() + "%";

                predicates = criteriaBuilder.and(
                        predicates,
                        criteriaBuilder.or(
                                criteriaBuilder.like(criteriaBuilder.lower(root.get("fromIban")), ibanPattern),
                                criteriaBuilder.like(criteriaBuilder.lower(root.get("toIban")), ibanPattern)
                        )
                );
            }

            return predicates;
        };
    }
}