package com.example.demo.configuration;

import com.example.demo.dto.request.MailDto;
import com.example.demo.dto.response.ApiResponse;

import javax.mail.MessagingException;

public interface EmailService {

    ApiResponse<String> sendEmail(MailDto mailDto) ;

    ApiResponse<String> sendAttachment(MailDto mailDto) throws MessagingException;
}