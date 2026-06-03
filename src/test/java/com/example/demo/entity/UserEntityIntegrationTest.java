package com.example.demo.entity;

import com.example.demo.common.enums.UserRole;
import com.example.demo.repositories.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class UserEntityIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void saveAndLoad_persistsUserFieldsAndTimestamps() {
        User saved = userRepository.saveAndFlush(customer("jane.customer@example.com", "223456789"));
        entityManager.clear();

        User loaded = userRepository.findByEmail("jane.customer@example.com").orElseThrow();

        assertNotNull(saved.getId());
        assertEquals("Jane", loaded.getFirstName());
        assertEquals("Customer", loaded.getLastName());
        assertEquals("223456789", loaded.getBsn());
        assertEquals("+31612345678", loaded.getPhoneNumber());
        assertEquals("hash", loaded.getPasswordHash());
        assertEquals(UserRole.CUSTOMER, loaded.getRole());
        assertTrue(loaded.isApproved());
        assertNotNull(loaded.getCreatedAt());
        assertNotNull(loaded.getUpdatedAt());
    }

    @Test
    void save_whenDuplicateEmail_throwsDataIntegrityViolationException() {
        userRepository.saveAndFlush(customer("duplicate@example.com", "323456789"));

        User duplicate = customer("duplicate@example.com", "423456789");

        assertThrows(DataIntegrityViolationException.class, () -> {
            userRepository.save(duplicate);
            entityManager.flush();
        });
    }

    @Test
    void findCustomersWithoutAccounts_returnsOnlyCustomersWithoutAccounts() {
        userRepository.saveAndFlush(customer("no.accounts@example.com", "523456789"));
        userRepository.saveAndFlush(employee("employee@example.com", "623456789"));

        var page = userRepository.findCustomersWithoutAccounts(org.springframework.data.domain.PageRequest.of(0, 10));

        assertEquals(1, page.getTotalElements());
        assertEquals("no.accounts@example.com", page.getContent().getFirst().getEmail());
        assertTrue(page.getContent().getFirst().isApproved());
    }

    private User customer(String email, String bsn) {
        User user = new User();
        user.setFirstName("Jane");
        user.setLastName("Customer");
        user.setEmail(email);
        user.setBsn(bsn);
        user.setPhoneNumber("+31612345678");
        user.setPasswordHash("hash");
        user.setRole(UserRole.CUSTOMER);
        user.setApproved(true);
        return user;
    }

    private User employee(String email, String bsn) {
        User user = customer(email, bsn);
        user.setRole(UserRole.EMPLOYEE);
        return user;
    }
}
