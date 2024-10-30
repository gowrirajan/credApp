package com.example.credApp.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "credential", schema = "credential_inventory")
public class Credential {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "credential", nullable = false)
    private UUID credential;

    private String userId; 
    private String userEmail;
    private Integer status;
    private LocalDateTime createdTs;
    private String createdBy;
    private LocalDateTime modifiedTs;
    private String modifiedBy;

    @ElementCollection
    private List<String> encryptedCredentials;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UUID getCredential() {
        return credential;
    }

    public void setCredential(UUID credential) {
        this.credential = credential;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail; // Ensure this method is defined
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public LocalDateTime getCreatedTs() {
        return createdTs;
    }

    public void setCreatedTs(LocalDateTime createdTs) {
        this.createdTs = createdTs;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getModifiedTs() {
        return modifiedTs;
    }

    public void setModifiedTs(LocalDateTime modifiedTs) {
        this.modifiedTs = modifiedTs;
    }

    public String getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public List<String> getEncryptedCredentials() {
        return encryptedCredentials;
    }

    public void setEncryptedCredentials(List<String> encryptedCredentials) {
        this.encryptedCredentials = encryptedCredentials;
    }
}

