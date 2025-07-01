package com.david.notification_service.service;

import com.david.notification_service.exception.EmailServiceException;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final SendGrid sendGrid;

    @Value( "${spring.sendgrid.from-email}" )
    private String fromEmail;

    public void sendEmail(String toEmail, String subject, String body) {
        log.info("EmailService: Sending email to {}, subject: {}, body: {}", toEmail, subject, body);
        Email from = new Email(fromEmail);
        Email to = new Email(toEmail);

        Content content = new Content("text/plain", body);
        Mail mail = new Mail(from, subject, to, content);

        try {
            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sendGrid.api(request);
            if (response.getStatusCode() != 202) {
                log.error("EmailService: Failed to send email, status code: {}, body: {}", response.getStatusCode(), response.getBody());
                throw new EmailServiceException("Failed to send email");
            }
            log.info("EmailService: Email sent successfully, status code: {}", response.getStatusCode());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
