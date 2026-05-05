package com.example.demo.repositories;

import com.example.demo.models.User;
import com.example.demo.models.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByBsn(String bsn);

    Page<User> findByRole(UserRole role, Pageable pageable);

    Page<User> findByRoleAndApproved(UserRole role, boolean approved, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.role = 'CUSTOMER' AND u.id NOT IN (SELECT DISTINCT a.owner.id FROM Account a)")
    Page<User> findCustomersWithoutAccounts(Pageable pageable);
}
