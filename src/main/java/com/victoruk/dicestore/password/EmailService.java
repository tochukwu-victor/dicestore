package com.victoruk.dicestore.password;
import com.victoruk.dicestore.exception.EmailSendException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Slf4j
@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    public void sendResetPasswordEmail(String to, String name, String resetLink) {
        try {
            // Prepare thymeleaf context
            Context context = new Context();
            context.setVariable("name", name);
            context.setVariable("resetLink", resetLink);

            // Process template into HTML string
            String htmlContent = templateEngine.process("reset-password-email", context);

            // Build email
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(fromEmail); // ✅ added
            helper.setTo(to);
            helper.setSubject("Reset your password - Dicestore");
            helper.setText(htmlContent, true); // true = HTML

            mailSender.send(mimeMessage);

            System.out.println(htmlContent);
        } catch (MessagingException e) {
            log.error("Failed to send reset password email to {}", to, e);
            throw new EmailSendException("Could not send reset password email");
        }

    }


}
