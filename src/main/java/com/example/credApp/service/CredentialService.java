package com.example.credApp.service;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.credApp.model.Credential;
import com.example.credApp.repository.CredentialRepository;

@Service
public class CredentialService {

    @Autowired
    private CredentialRepository credentialRepository;

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES";

    public String getEncryptedActiveCredentials(String userEmail) throws Exception {
        // Find active credentials for the user
        List<Credential> activeCredentials = credentialRepository.findByUserEmailAndStatus(userEmail, "active");

        // Log the number of active credentials found
        System.out.println("Active credentials found for " + userEmail + ": " + activeCredentials.size());

        // If no active credentials found, check for existing credential with null userId and userEmail
        if (activeCredentials.isEmpty()) {
            Credential existingCredential = credentialRepository.findFirstByUserIdIsNullAndUserEmailIsNull();
            if (existingCredential != null) {
                // Update the existing credential
                existingCredential.setUserEmail(userEmail);
                existingCredential.setUserId(userEmail);
                existingCredential.setStatus("active"); // Set status to active
                existingCredential.setModifiedTs(LocalDateTime.now());
                existingCredential.setModifiedBy("admin");
                credentialRepository.save(existingCredential);
                
                activeCredentials = List.of(existingCredential);
            } else {
                // No existing credential found to update
                return "No existing credential found to update.";
            }
        } else {
            // Change the status of all found active credentials to inactive
            for (Credential c : activeCredentials) {
                c.setStatus("inactive");
                credentialRepository.save(c);
            }
        }

        // Encrypt and return the credentials
        SecretKey secretKey = generateKey();
        return activeCredentials.stream()
                .map(c -> encrypt(c.getCredential(), secretKey))
                .collect(Collectors.joining(", ")); // Join with a comma and space
    }

    private String encrypt(String data, SecretKey key) {
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] encryptedData = cipher.doFinal(data.getBytes());
            return Base64.getEncoder().encodeToString(encryptedData);
        } catch (Exception e) {
            throw new RuntimeException("Error while encrypting data", e);
        }
    }

    private SecretKey generateKey() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance(ALGORITHM);
        keyGen.init(256); // Use the appropriate key size for your needs
        return keyGen.generateKey();
    }
}
