package com.example.credApp.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
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
    private static final int STATUS_ASSIGNED = 101; 
    private static final int STATUS_AVAILABLE = 100; 

    public String getEncryptedActiveCredentials(String userEmail) throws Exception {
        
        List<Credential> existingCredentials = credentialRepository.findByUserEmailAndStatus(userEmail, STATUS_ASSIGNED);

        if (!existingCredentials.isEmpty()) {
            
            Credential credential = existingCredentials.get(0);
            
            credential.setModifiedTs(LocalDateTime.now());
            credential.setModifiedBy("admin");
            credentialRepository.save(credential); 
            
            String newEncryptedValue = encrypt(credential.getCredential().toString(), generateKey());
            
            List<String> encryptedCredentials = credential.getEncryptedCredentials();
            if (encryptedCredentials == null) {
                encryptedCredentials = new ArrayList<>(); 
            }
            encryptedCredentials.add(newEncryptedValue);
            credential.setEncryptedCredentials(encryptedCredentials);
            credentialRepository.save(credential); 
            return encryptedCredentials.toString(); 
        }

        List<Credential> availableCredentials = credentialRepository.findByStatus(STATUS_AVAILABLE);
        if (!availableCredentials.isEmpty()) {
            Credential availableCredential = availableCredentials.get(1);
            
            availableCredential.setUserEmail(userEmail);
            availableCredential.setUserId(userEmail); 
            availableCredential.setStatus(STATUS_ASSIGNED);
            availableCredential.setModifiedTs(LocalDateTime.now());
            availableCredential.setModifiedBy("admin");

            String newEncryptedValue = encrypt(availableCredential.getCredential().toString(), generateKey());
            List<String> encryptedCredentials = availableCredential.getEncryptedCredentials();
            if (encryptedCredentials == null) {
                encryptedCredentials = new ArrayList<>();
            }
            encryptedCredentials.add(newEncryptedValue);
            availableCredential.setEncryptedCredentials(encryptedCredentials);
            credentialRepository.save(availableCredential); 

            return encryptedCredentials.toString(); 
        }

        Credential newCredential = new Credential();
        newCredential.setCredential(UUID.randomUUID()); 
        newCredential.setUserEmail(userEmail);
        newCredential.setUserId(userEmail); 
        newCredential.setStatus(STATUS_ASSIGNED);
        newCredential.setCreatedTs(LocalDateTime.now());
        newCredential.setCreatedBy("admin");
        newCredential.setModifiedTs(LocalDateTime.now());
        newCredential.setModifiedBy("admin");

        
        List<String> encryptedCredentials = new ArrayList<>();
        String newEncryptedValue = encrypt(newCredential.getCredential().toString(), generateKey());
        encryptedCredentials.add(newEncryptedValue);
        newCredential.setEncryptedCredentials(encryptedCredentials);

        credentialRepository.save(newCredential);

        return encryptedCredentials.toString(); 
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
        keyGen.init(256); 
        return keyGen.generateKey();
    }
}
