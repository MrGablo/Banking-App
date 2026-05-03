package com.example.demo.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;

    private String lastName;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(unique = true, nullable = false)
    private String bsn;

    private String phoneNumber;

    private String passwordHash;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    private boolean approved;

    protected User() {
        // JPA only
    }

    public User(String firstName, String lastName, String email, String bsn, String phoneNumber, String passwordHash, UserRole role, boolean approved) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.bsn = bsn;
        this.phoneNumber = phoneNumber;
        this.passwordHash = passwordHash;
        this.role = role;
        this.approved = approved;
    }
}

