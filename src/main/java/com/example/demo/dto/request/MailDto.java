package com.example.demo.dto.request;

import lombok.Builder;
import lombok.Data;

import java.io.File;

@Data
@Builder
public class MailDto {

    private String to;
    private String subject;
    private String message;
//    private File file;
}
