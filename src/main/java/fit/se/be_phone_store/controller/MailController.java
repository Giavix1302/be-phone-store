package fit.se.be_phone_store.controller;

import fit.se.be_phone_store.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/mail")
@RequiredArgsConstructor
@Slf4j
public class MailController {

    private final EmailService emailService;

    @GetMapping("/test")
    public ResponseEntity<Map<String, String>> sendTestEmail() {
        try {
            emailService.sendEmail(
                    "quynhnhu18121812@gmail.com",
                    "Test Email",
                    "<h2>Hello from Spring Boot + SendGrid!</h2>"
            );
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Email sent successfully!");
            response.put("status", "success");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error in sendTestEmail endpoint", e);
            throw e; 
        }
    }
}
