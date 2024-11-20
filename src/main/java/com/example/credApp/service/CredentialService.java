package com.example.credApp.service;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;

import javax.crypto.Cipher;
// import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.credApp.model.Credential;
import com.example.credApp.repository.CredentialRepository;
import static com.aventrix.jnanoid.jnanoid.NanoIdUtils.*;
// import com.aventrix.nanoid.NanoId;

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

        credential.setModifiedTs(LocalDateTime.now());
        credential.setModifiedBy("admin");

        // Encrypt the credential and set the encrypted value
        String newEncryptedValue = encrypt(credential.getCredential());
        // System.out.println("Encrypted credential: " + newEncryptedValue); 


        credential.setEncryptedCredentials(newEncryptedValue);
        credentialRepository.save(credential); 

        return newEncryptedValue; 
    }

    public Map<String, String> validateEncryptedCredential(String encryptedCredential) {
        try {

            String decryptedValue = decrypt(encryptedCredential);

            Credential credential = credentialRepository.findByCredential(decryptedValue);

            if (credential != null) {

                return Map.of(
                        "credential", credential.getCredential(),
                        "userEmail", credential.getUserEmail()
                );
            } else {
                // Not Found: Return 404
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Decrypted value not found in the database");
            }
        } catch (RuntimeException e) {
            // Decryption failed: Return 401
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid encrypted value or decryption failed");
        }
    }

    public String decrypt(String encryptedData) {
        try {
            SecretKey key = loadStaticKey();
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] decodedData = Base64.getDecoder().decode(encryptedData);
            byte[] decryptedData = cipher.doFinal(decodedData);
            return new String(decryptedData);
        } catch (Exception e) {
            throw new RuntimeException("Error while decrypting data", e);
        }
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

    // generates random key for encryption
    // private SecretKey generateKey() throws Exception {
    //     KeyGenerator keyGen = KeyGenerator.getInstance(ALGORITHM);
    //     keyGen.init(256); // Key size
    //     return keyGen.generateKey();
    // }

    private SecretKey loadStaticKey() {
        byte[] decodedKey = Base64.getDecoder().decode(STATIC_KEY);
        return new SecretKeySpec(decodedKey, ALGORITHM);
    }
}
