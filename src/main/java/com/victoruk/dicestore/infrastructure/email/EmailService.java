package com.victoruk.dicestore.infrastructure.email;

import com.victoruk.dicestore.common.config.appProperties.AppProperties;
import com.victoruk.dicestore.common.exception.EmailSendException;
import com.victoruk.dicestore.order.entity.Order;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Slf4j
@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final AppProperties appProperties;
    private final String fromEmail;

    public EmailService(
            JavaMailSender mailSender,
            TemplateEngine templateEngine,
            AppProperties appProperties,
            @Value("${spring.mail.username}") String fromEmail
    ) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        this.appProperties = appProperties;
        this.fromEmail = fromEmail;
    }

    /**
     * Sends the password reset email asynchronously.
     */
    @Async
    public void sendResetPasswordEmail(String to, String name, String resetLink) {
        log.info("Sending password reset email to {}", to);
        try {
            String htmlContent = renderResetPasswordTemplate(to, name, resetLink);
            sendHtmlEmail(to, "Reset your password - Dicestore", htmlContent);
            log.info("Password reset email successfully sent to {}", to);
        } catch (EmailSendException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected failure while preparing/sending reset email to {}", to, e);
            throw new EmailSendException("Could not send reset password email");
        }
    }

    /**
     * Sends order confirmation email after successful payment.
     * Non-critical — failure is logged but never thrown since payment already succeeded.
     */
    @Async
    public void sendOrderConfirmationEmail(String to, String name, Order order) {
        log.info("Sending order confirmation email to {}", to);
        try {
            Context context = new Context();
            context.setVariable("name", name);
            context.setVariable("orderId", order.getOrderId());
            context.setVariable("totalPrice", order.getTotalPrice());
            context.setVariable("items", order.getOrderItems());

            String htmlContent = templateEngine.process(
                    appProperties.paystack().orderConfirmationEmailTemplate(), context);

            sendHtmlEmail(to, "Order Confirmed - Dicestore #" + order.getOrderId(), htmlContent);
            log.info("Order confirmation email successfully sent to {}", to);

        } catch (Exception e) {
            // Non-critical — payment already succeeded, swallow the exception
            log.error("Failed to send order confirmation email to {} for order [{}]",
                    to, order.getOrderId(), e);
        }
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private String renderResetPasswordTemplate(String to, String name, String resetLink) {
        try {
            Context context = new Context();
            context.setVariable("name", name);
            context.setVariable("resetLink", resetLink);
            return templateEngine.process(appProperties.resetToken().emailTemplate(), context);
        } catch (Exception e) {
            log.error("Thymeleaf template rendering failed for recipient {}", to, e);
            throw new EmailSendException("Could not render reset password email template");
        }
    }

    private void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            log.error("SMTP failure sending email to {}", to, e);
            throw new EmailSendException("Could not send email");
        }
    }
}