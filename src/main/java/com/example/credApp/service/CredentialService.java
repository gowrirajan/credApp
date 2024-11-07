package com.example.credApp.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.credApp.model.Credential;
import com.example.credApp.repository.CredentialRepository;
import org.springframework.jdbc.core.JdbcTemplate;

@Service
public class CredentialService {

    @Autowired
    private CredentialRepository credentialRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate; // Use JdbcTemplate to retrieve nano_id from DB

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES";
    private static final int STATUS_ASSIGNED = 101; 
    private static final int STATUS_AVAILABLE = 100; 

    private String fetchNanoIdFromDB() {
        return jdbcTemplate.queryForObject("SELECT generate_nano_id()", String.class);
    }

    public String getEncryptedActiveCredentials(String userEmail) throws Exception {
        System.out.println("Fetching encrypted credentials for user: " + userEmail);

        // First, check for existing assigned credentials for the user
        List<Credential> existingCredentials = credentialRepository.findByUserEmailAndStatus(userEmail, STATUS_ASSIGNED);

        if (!existingCredentials.isEmpty()) {
            Credential credential = existingCredentials.get(0);
            System.out.println("Found existing assigned credential for user: " + userEmail);

            credential.setModifiedTs(LocalDateTime.now());
            credential.setModifiedBy("admin");
            credentialRepository.save(credential);

            String newEncryptedValue = encrypt(credential.getCredential(), generateKey());
            System.out.println("Encrypted existing credential: " + newEncryptedValue);

            List<String> encryptedCredentials = credential.getEncryptedCredentials();
            if (encryptedCredentials == null) {
                encryptedCredentials = new ArrayList<>();
            }
            encryptedCredentials.add(newEncryptedValue);
            credential.setEncryptedCredentials(encryptedCredentials);
            credentialRepository.save(credential);

            return encryptedCredentials.toString();
        }

        // If no assigned credentials found, check for available credentials
        List<Credential> availableCredentials = credentialRepository.findByStatus(STATUS_AVAILABLE);
        if (!availableCredentials.isEmpty()) {
            Credential availableCredential = availableCredentials.get(0);
            System.out.println("Found available credential for user: " + userEmail);

            availableCredential.setUserEmail(userEmail);
            availableCredential.setUserId(userEmail);
            availableCredential.setStatus(STATUS_ASSIGNED);
            availableCredential.setModifiedTs(LocalDateTime.now());
            availableCredential.setModifiedBy("admin");

            String newEncryptedValue = encrypt(availableCredential.getCredential(), generateKey());
            System.out.println("Encrypted available credential: " + newEncryptedValue);

            List<String> encryptedCredentials = availableCredential.getEncryptedCredentials();
            if (encryptedCredentials == null) {
                encryptedCredentials = new ArrayList<>();
            }
            encryptedCredentials.add(newEncryptedValue);
            availableCredential.setEncryptedCredentials(encryptedCredentials);
            credentialRepository.save(availableCredential);

            return encryptedCredentials.toString();
        }

        // If no available credential found, create a new one with nano_id
        Credential newCredential = new Credential();
        newCredential.setCredential(fetchNanoIdFromDB()); // Use nano_id instead of UUID
        newCredential.setUserEmail(userEmail);
        newCredential.setUserId(userEmail);
        newCredential.setStatus(STATUS_ASSIGNED);
        newCredential.setCreatedTs(LocalDateTime.now());
        newCredential.setCreatedBy("admin");
        newCredential.setModifiedTs(LocalDateTime.now());
        newCredential.setModifiedBy("admin");

        List<String> encryptedCredentials = new ArrayList<>();
        String newEncryptedValue = encrypt(newCredential.getCredential(), generateKey());
        System.out.println("Encrypted new credential: " + newEncryptedValue);
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
        keyGen.init(256); // Key size
        return keyGen.generateKey();
    }
}










// package com.example.credApp.service;

// import java.time.LocalDateTime;
// import java.util.ArrayList;
// import java.util.Base64;
// import java.util.List;
// import java.util.UUID;

// import javax.crypto.Cipher;
// import javax.crypto.KeyGenerator;
// import javax.crypto.SecretKey;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Service;

// import com.example.credApp.model.Credential;
// import com.example.credApp.repository.CredentialRepository;
// import org.springframework.jdbc.core.JdbcTemplate;

// @Service
// public class CredentialService {

//     @Autowired
//     private CredentialRepository credentialRepository;

//     private static final String ALGORITHM = "AES";
//     private static final String TRANSFORMATION = "AES";
//     private static final int STATUS_ASSIGNED = 101;
//     private static final int STATUS_AVAILABLE = 100;

//     private String fetchNanoIdFromDB() {
//         return jdbcTemplate.queryForObject("SELECT generate_nano_id()", String.class);
//     }

//     public String getEncryptedActiveCredentials(String userEmail) throws Exception {
//         System.out.println("Fetching encrypted credentials for user: " + userEmail);
    
//         // First, check for existing assigned credentials for the user
//         List<Credential> existingCredentials = credentialRepository.findByUserEmailAndStatus(userEmail, STATUS_ASSIGNED);
    
//         if (!existingCredentials.isEmpty()) {
//             // If found, use the existing credential
//             Credential credential = existingCredentials.get(0);
//             System.out.println("Found existing assigned credential for user: " + userEmail);
    
//             // Update modified timestamp
//             credential.setModifiedTs(LocalDateTime.now());
//             credential.setModifiedBy("admin");
//             credentialRepository.save(credential); // Save changes
    
//             // Encrypt the existing UUID
//             String newEncryptedValue = encrypt(credential.getCredential().toString(), generateKey());
//             System.out.println("Encrypted existing credential: " + newEncryptedValue);
    
//             // Append to the list of encrypted credentials
//             List<String> encryptedCredentials = credential.getEncryptedCredentials();
//             if (encryptedCredentials == null) {
//                 encryptedCredentials = new ArrayList<>(); // Initialize if null
//             }
//             encryptedCredentials.add(newEncryptedValue);
//             credential.setEncryptedCredentials(encryptedCredentials);
//             credentialRepository.save(credential);
    
//             return encryptedCredentials.toString();
//         }
    
//         // If no assigned credentials found, check for available credentials
//         List<Credential> availableCredentials = credentialRepository.findByStatus(STATUS_AVAILABLE);
//         if (!availableCredentials.isEmpty()) {
//             Credential availableCredential = availableCredentials.get(0);
//             System.out.println("Found available credential for user: " + userEmail);
    
//             // Assign the available credential to the new user
//             availableCredential.setUserEmail(userEmail);
//             availableCredential.setUserId(userEmail); // Set userId to userEmail
//             availableCredential.setStatus(STATUS_ASSIGNED);
//             availableCredential.setModifiedTs(LocalDateTime.now());
//             availableCredential.setModifiedBy("admin");
    
//             // Encrypt the credential
//             String newEncryptedValue = encrypt(availableCredential.getCredential().toString(), generateKey());
//             System.out.println("Encrypted available credential: " + newEncryptedValue);
    
//             List<String> encryptedCredentials = availableCredential.getEncryptedCredentials();
//             if (encryptedCredentials == null) {
//                 encryptedCredentials = new ArrayList<>(); // Initialize if null
//             }
//             encryptedCredentials.add(newEncryptedValue);
//             availableCredential.setEncryptedCredentials(encryptedCredentials);
//             credentialRepository.save(availableCredential); // Save updated available credential
    
//             return encryptedCredentials.toString(); // Return as string
//         }
    
//         // If no available credential found, create a new one
//         Credential newCredential = new Credential();
//         newCredential.setCredential(UUID.randomUUID()); // Generate a new UUID
//         newCredential.setUserEmail(userEmail);
//         newCredential.setUserId(userEmail); // Set userId to userEmail
//         newCredential.setStatus(STATUS_ASSIGNED);
//         newCredential.setCreatedTs(LocalDateTime.now());
//         newCredential.setCreatedBy("admin");
//         newCredential.setModifiedTs(LocalDateTime.now());
//         newCredential.setModifiedBy("admin");
    
//         // Initialize encrypted credentials list
//         List<String> encryptedCredentials = new ArrayList<>();
//         String newEncryptedValue = encrypt(newCredential.getCredential().toString(), generateKey());
//         System.out.println("Encrypted new credential: " + newEncryptedValue);
//         encryptedCredentials.add(newEncryptedValue);
//         newCredential.setEncryptedCredentials(encryptedCredentials);
    
//         // Save the new credential
//         credentialRepository.save(newCredential);
    
//         return encryptedCredentials.toString(); // Return as string
//     }
    

//     private String encrypt(String data, SecretKey key) {
//         try {
//             Cipher cipher = Cipher.getInstance(TRANSFORMATION);
//             cipher.init(Cipher.ENCRYPT_MODE, key);
//             byte[] encryptedData = cipher.doFinal(data.getBytes());
//             return Base64.getEncoder().encodeToString(encryptedData);
//         } catch (Exception e) {
//             throw new RuntimeException("Error while encrypting data", e);
//         }
//     }

//     private SecretKey generateKey() throws Exception {
//         KeyGenerator keyGen = KeyGenerator.getInstance(ALGORITHM);
//         keyGen.init(256); // Key size
//         return keyGen.generateKey();
//     }
// }
