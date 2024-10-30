package com.example.credApp.repository;


import com.example.credApp.model.Credential;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CredentialRepository extends JpaRepository<Credential, Integer> {
    List<Credential> findByUserEmailAndStatus(String userEmail, String status);
    Credential findFirstByUserIdIsNullAndUserEmailIsNull();
}


