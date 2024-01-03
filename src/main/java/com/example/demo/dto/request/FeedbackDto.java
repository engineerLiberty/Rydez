package com.example.demo.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class FeedbackDto {

    @NotBlank(message = "Message cannot be blank.")
    private String message;
}
