package fit.se.be_phone_store.service;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import fit.se.be_phone_store.exception.EmailSendingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {

    @Value("${sendgrid.api-key}")
    private String sendGridApiKey;

    @Value("${sendgrid.from-email}")
    private String fromEmail;

    public void sendEmail(String to, String subject, String htmlContent) {
        try {
            Email from = new Email(fromEmail);
            Email toEmail = new Email(to);
            Content content = new Content("text/html", htmlContent);
            Mail mail = new Mail(from, subject, toEmail, content);

            SendGrid sg = new SendGrid(sendGridApiKey);
            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            
            var response = sg.api(request);
            
            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                log.info("Email sent successfully to: {}", to);
            } else {
                log.error("Failed to send email. Status code: {}, Body: {}", 
                    response.getStatusCode(), response.getBody());
                throw new EmailSendingException(
                    String.format("Failed to send email. Status code: %d, Body: %s", 
                        response.getStatusCode(), response.getBody())
                );
            }
        } catch (EmailSendingException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error sending email to: {}", to, e);
            throw new EmailSendingException("Failed to send email: " + e.getMessage(), e);
        }
    }
}