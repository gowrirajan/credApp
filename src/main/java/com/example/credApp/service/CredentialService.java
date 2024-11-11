package com.example.credApp.service;

import java.time.LocalDateTime;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.credApp.model.Credential;
import com.example.credApp.repository.CredentialRepository;
import static com.aventrix.jnanoid.jnanoid.NanoIdUtils.*;

@Service
public class CredentialService {

    @Autowired
    private CredentialRepository credentialRepository;

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES";
    private static final int STATUS_ASSIGNED = 101; 
    private static final int STATUS_AVAILABLE = 100; 
    private static final String STATIC_KEY = "YWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXphYmNkZWY=";

    public String getEncryptedActiveCredentials(String userEmail) throws Exception {
        System.out.println("Fetching encrypted credentials for user: " + userEmail);

        // Fetch the existing assigned credentials for the user
        Credential credential = credentialRepository.findByUserEmailAndStatus(userEmail, STATUS_ASSIGNED)
                .stream().findFirst().orElse(null);

        if (credential == null) {
            // No assigned credential, check for an available one
            credential = credentialRepository.findByStatus(STATUS_AVAILABLE)
                    .stream().findFirst().orElse(null);

            if (credential != null) {
                System.out.println("Found available credential for user: " + userEmail);

                // Assign it to the user
                credential.setUserEmail(userEmail);
                credential.setUserId(userEmail); 
                credential.setStatus(STATUS_ASSIGNED);
            } else {
                // No available credential, create a new one
                credential = new Credential();
                credential.setCredential(randomNanoId()); // Generate a new Nano ID
                credential.setUserEmail(userEmail);
                credential.setUserId(userEmail);
                credential.setStatus(STATUS_ASSIGNED);
                credential.setCreatedTs(LocalDateTime.now());
                credential.setCreatedBy("admin");
            }
        }

        // Update modified timestamp and encrypt the credential
        credential.setModifiedTs(LocalDateTime.now());
        credential.setModifiedBy("admin");

        // Encrypt the credential and set the encrypted value
        String newEncryptedValue = encrypt(credential.getCredential());
        System.out.println("Encrypted credential: " + newEncryptedValue);

        // Set the single encrypted credential value
        credential.setEncryptedCredentials(newEncryptedValue);
        credentialRepository.save(credential); // Save the credential with updated encryption

        return newEncryptedValue; // Return the single encrypted value
    }

    private String encrypt(String data) {
        try {
            SecretKey key = loadStaticKey();
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
        keyGen.init(256); // Key size
        return keyGen.generateKey();
    }

    private SecretKey loadStaticKey() {
        byte[] decodedKey = Base64.getDecoder().decode(STATIC_KEY);
        return new SecretKeySpec(decodedKey, ALGORITHM);
    }
}
