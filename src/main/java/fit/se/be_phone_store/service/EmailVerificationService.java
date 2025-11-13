package fit.se.be_phone_store.service;

import fit.se.be_phone_store.entity.User;
import fit.se.be_phone_store.exception.AuthenticationException;
import fit.se.be_phone_store.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Email Verification Service
 * Manages email verification codes (in-memory for testing)
 * In production, should use Redis or database
 */
@Service
public class EmailVerificationService {

    @Autowired
    private UserRepository userRepository;

    // In-memory storage for verification codes
    // Key: email, Value: VerificationCodeInfo
    private final Map<String, VerificationCodeInfo> verificationCodes = new ConcurrentHashMap<>();

    // Code expiration time: 15 minutes
    private static final int CODE_EXPIRATION_MINUTES = 15;


    public String generateVerificationCode(String email) {
        // Generate 6-digit code
        Random random = new Random();
        String code = String.format("%06d", random.nextInt(1000000));

        LocalDateTime expirationTime = LocalDateTime.now().plusMinutes(CODE_EXPIRATION_MINUTES);
        verificationCodes.put(email, new VerificationCodeInfo(code, expirationTime));

        // Log code to console for testing
        System.out.println("========================================");
        System.out.println("EMAIL VERIFICATION CODE");
        System.out.println("========================================");
        System.out.println("Email: " + email);
        System.out.println("Verification Code: " + code);
        System.out.println("Expires at: " + expirationTime);
        System.out.println("========================================");

        return code;
    }


    public boolean verifyCode(String email, String code) {
        VerificationCodeInfo codeInfo = verificationCodes.get(email);

        if (codeInfo == null) {
            System.out.println("Verification failed: No code found for email " + email);
            return false;
        }


        if (LocalDateTime.now().isAfter(codeInfo.getExpirationTime())) {
            verificationCodes.remove(email);
            System.out.println("Verification failed: Code expired for email " + email);
            return false;
        }

        if (!codeInfo.getCode().equals(code)) {
            System.out.println("Verification failed: Invalid code for email " + email);
            return false;
        }

        verificationCodes.remove(email);
        System.out.println("Verification successful for email: " + email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AuthenticationException("User not found"));
        
        user.setEnabled(true);
        userRepository.save(user);

        return true;
    }


    public boolean hasPendingVerification(String email) {
        VerificationCodeInfo codeInfo = verificationCodes.get(email);
        if (codeInfo == null) {
            return false;
        }
        
        if (LocalDateTime.now().isAfter(codeInfo.getExpirationTime())) {
            verificationCodes.remove(email);
            return false;
        }
        
        return true;
    }


    private static class VerificationCodeInfo {
        private final String code;
        private final LocalDateTime expirationTime;

        public VerificationCodeInfo(String code, LocalDateTime expirationTime) {
            this.code = code;
            this.expirationTime = expirationTime;
        }

        public String getCode() {
            return code;
        }

        public LocalDateTime getExpirationTime() {
            return expirationTime;
        }
    }
}

