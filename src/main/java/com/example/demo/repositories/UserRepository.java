package com.example.demo.repositories;

import com.example.demo.entity.User;
import com.example.demo.common.enums.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByBsn(String bsn);

    Optional<User> findByPhoneNumber(String phoneNumber);

    Page<User> findByRole(UserRole role, Pageable pageable);

    Page<User> findByRoleAndApproved(UserRole role, boolean approved, Pageable pageable);

    @Query("""
            SELECT u FROM User u
            WHERE LOWER(u.firstName) LIKE LOWER(CONCAT('%', :firstName, '%'))
              AND LOWER(u.lastName) LIKE LOWER(CONCAT('%', :lastName, '%'))
              AND u.role = 'CUSTOMER'
              AND u.approved = true
            """)
    Page<User> searchCustomers(@Param("firstName") String firstName, @Param("lastName") String lastName, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.role = 'CUSTOMER' AND u.id NOT IN (SELECT DISTINCT a.owner.id FROM Account a)")
    Page<User> findCustomersWithoutAccounts(Pageable pageable);
}
