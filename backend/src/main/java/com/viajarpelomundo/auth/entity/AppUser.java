package com.viajarpelomundo.auth.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

/**
 * Representa um utilizador registado.
 * A password nunca é guardada em texto normal.
 */
@Entity
@Table(name = "app_users")
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 254)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Column(nullable = false, length = 20)
    private String role;

    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    protected AppUser() {
    }

    public AppUser(String email, String passwordHash) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = "CUSTOMER";
    }

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public String getRole() { return role; }
}
