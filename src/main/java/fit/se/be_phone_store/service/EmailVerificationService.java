package fit.se.be_phone_store.service;

import fit.se.be_phone_store.entity.User;
import fit.se.be_phone_store.exception.AuthenticationException;
import fit.se.be_phone_store.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Email Verification Service
 * Manages email verification codes (in-memory for testing)
 * In production, should use Redis or database
 */
@Service
@Slf4j
public class EmailVerificationService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

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

        // Log verification code to console for testing
        log.info("========================================");
        log.info("EMAIL VERIFICATION CODE");
        log.info("========================================");
        log.info("Email: {}", email);
        log.info("Verification Code: {}", code);
        log.info("Expires at: {}", expirationTime);
        log.info("========================================");
        
        // Also print to console for easy visibility
        // System.out.println("========================================");
        // System.out.println("EMAIL VERIFICATION CODE");
        // System.out.println("========================================");
        // System.out.println("Email: " + email);
        // System.out.println("Verification Code: " + code);
        // System.out.println("Expires at: " + expirationTime);
        // System.out.println("========================================");

        // Send verification email via SendGrid
        try {
            String subject = "Xác thực email - Phone Store";
            String htmlContent = buildVerificationEmailHtml(code, expirationTime);
            emailService.sendEmail(email, subject, htmlContent);
            log.info("Verification email sent successfully to: {}", email);
        } catch (Exception e) {
            log.error("Failed to send verification email to: {}. Error: {}", email, e.getMessage(), e);
            log.warn("Email sending failed, but verification code is still valid. Code: {}", code);
        }

        return code;
    }

    /**
     * Build HTML email template for verification code
     */
    private String buildVerificationEmailHtml(String code, LocalDateTime expirationTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String expirationTimeStr = expirationTime.format(formatter);
        
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<meta charset='UTF-8'>" +
                "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                "<title>Xác thực email</title>" +
                "</head>" +
                "<body style='margin: 0; padding: 0; font-family: Arial, sans-serif; background-color: #f4f4f4;'>" +
                "<table width='100%' cellpadding='0' cellspacing='0' style='background-color: #f4f4f4; padding: 20px;'>" +
                "<tr>" +
                "<td align='center'>" +
                "<table width='600' cellpadding='0' cellspacing='0' style='background-color: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 2px 4px rgba(0,0,0,0.1);'>" +
                "<!-- Header -->" +
                "<tr>" +
                "<td style='background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); padding: 30px; text-align: center;'>" +
                "<h1 style='color: #ffffff; margin: 0; font-size: 28px;'>Phone Store</h1>" +
                "</td>" +
                "</tr>" +
                "<!-- Content -->" +
                "<tr>" +
                "<td style='padding: 40px 30px;'>" +
                "<h2 style='color: #333333; margin: 0 0 20px 0; font-size: 24px;'>Xác thực email của bạn</h2>" +
                "<p style='color: #666666; font-size: 16px; line-height: 1.6; margin: 0 0 30px 0;'>" +
                "Cảm ơn bạn đã đăng ký tài khoản tại Phone Store. Vui lòng sử dụng mã xác thực sau để kích hoạt tài khoản:" +
                "</p>" +
                "<!-- Verification Code Box -->" +
                "<div style='background-color: #f8f9fa; border: 2px dashed #667eea; border-radius: 8px; padding: 20px; text-align: center; margin: 30px 0;'>" +
                "<p style='color: #333333; font-size: 14px; margin: 0 0 10px 0; font-weight: bold;'>Mã xác thực:</p>" +
                "<p style='color: #667eea; font-size: 32px; font-weight: bold; letter-spacing: 5px; margin: 0; font-family: monospace;'>" + code + "</p>" +
                "</div>" +
                "<p style='color: #999999; font-size: 14px; line-height: 1.6; margin: 20px 0 0 0;'>" +
                "<strong>Lưu ý:</strong> Mã xác thực này sẽ hết hạn sau <strong>" + CODE_EXPIRATION_MINUTES + " phút</strong> (vào lúc " + expirationTimeStr + ")." +
                "</p>" +
                "<p style='color: #999999; font-size: 14px; line-height: 1.6; margin: 20px 0 0 0;'>" +
                "Nếu bạn không yêu cầu mã này, vui lòng bỏ qua email này." +
                "</p>" +
                "</td>" +
                "</tr>" +
                "<!-- Footer -->" +
                "<tr>" +
                "<td style='background-color: #f8f9fa; padding: 20px 30px; text-align: center; border-top: 1px solid #e9ecef;'>" +
                "<p style='color: #999999; font-size: 12px; margin: 0;'>" +
                "© 2024 Phone Store. All rights reserved." +
                "</p>" +
                "</td>" +
                "</tr>" +
                "</table>" +
                "</td>" +
                "</tr>" +
                "</table>" +
                "</body>" +
                "</html>";
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

