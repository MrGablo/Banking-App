package com.example.demo.repositories;

import com.example.demo.models.Account;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends ListCrudRepository<Account, String> {
    // Spring Data provides: findAll(), findById(String), save(Account), deleteById(String), etc.
}

