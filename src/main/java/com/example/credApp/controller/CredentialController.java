package com.example.credApp.controller;

import com.example.credApp.service.CredentialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/credentials")
public class CredentialController {

    @Autowired
    private CredentialService credentialService;

    @PostMapping("/extract-active")
    public ResponseEntity<String> extractActiveCredentials(@RequestBody Map<String, String> request) {
        String userEmail = request.get("userEmail");
        try {
            String encryptedCredential = credentialService.getEncryptedActiveCredentials(userEmail);
            return ResponseEntity.ok(encryptedCredential);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error retrieving encrypted credentials");
        }
    }

    @PostMapping("/validate")
    public ResponseEntity<?> validateCredential(@RequestBody Map<String, String> request) {
        String encryptedCredential = request.get("encryptedCredential");

        if (encryptedCredential == null || encryptedCredential.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", "Encrypted credential is required."
            ));
        }

        try {
            Map<String, String> validationResponse = credentialService.validateEncryptedCredential(encryptedCredential);
            return ResponseEntity.ok(validationResponse); // Return Nano ID and user email
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(Map.of(
                "status", "error",
                "message", e.getReason()
            ));
        }
    }
}





// package com.example.credApp.controller;

// import com.example.credApp.service.CredentialService;

// import java.util.Map;

// // import org.hibernate.mapping.Map;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;
// import org.springframework.web.server.ResponseStatusException;

// // import java.util.List;

// @RestController
// @RequestMapping("/api/credentials")
// public class CredentialController {

//     @Autowired
//     private CredentialService credentialService;

//     @PostMapping("/extract-active")
//         public ResponseEntity<String> extractActiveCredentials(@RequestBody Map<String, String> request) {
//             String userEmail = request.get("userEmail");
//             try {
//                 String encryptedCredentials = credentialService.getEncryptedActiveCredentials(userEmail);
//                 return ResponseEntity.ok(encryptedCredentials);
//             } catch (Exception e) {
//                 e.printStackTrace();
//                 throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error retrieving encrypted credentials");
//             }
//         }
// }