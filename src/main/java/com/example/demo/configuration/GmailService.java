package com.example.demo.configuration;

import com.example.demo.dto.request.MailDto;
import com.example.demo.dto.response.ApiResponse;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.io.UnsupportedEncodingException;

@RequiredArgsConstructor
@Service
@Builder
public class GmailService implements EmailService{
    private static final Logger LOGGER = LoggerFactory.getLogger(GmailService.class);
    private final JavaMailSender mailSender;

    @Async
    @Override
    public ApiResponse<String> sendEmail(MailDto mailDto){

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
        try {
            helper.setFrom("arixpresslogistics@gmail.com", "AriXpress");
            helper.setTo(mailDto.getTo());
            helper.setSubject(mailDto.getSubject());
            helper.setText(mailDto.getMessage(), true);
            mailSender.send(mimeMessage);
        }
        catch (MessagingException | UnsupportedEncodingException e) {
            LOGGER.error("An error occurred while sending an email to address : "
                    + mailDto.getTo() + "; error: " + e.getMessage());
        }
        LOGGER.info(String.format("Email Sent to -> %s", mailDto.getTo()));
        return new ApiResponse<>("Email sent", "Successful", null);
    }

    @Async
    @Override
    public ApiResponse<String> sendAttachment(MailDto mailDto) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();

        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "utf-8");
        try {
            helper.setFrom("arixpresslogistics@gmail.com", "AriXpress");
            helper.setTo(mailDto.getTo());
            helper.setSubject("Order Details PDF");
            File pdfFile = new File("Order_Details.pdf");
            helper.addAttachment(pdfFile.getName(), pdfFile);
            helper.setText(mailDto.getMessage(), true);
            mailSender.send(mimeMessage);
        }
        catch (MessagingException | UnsupportedEncodingException e) {
            LOGGER.error("An error occurred while sending an email to address : "
                    + mailDto.getTo() + "; error: " + e.getMessage());
        }
        LOGGER.info(String.format("Email Sent to -> %s", mailDto.getTo()));
        return new ApiResponse<>("Email sent", "Successful", null);
    }

}
