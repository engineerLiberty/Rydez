package com.example.demo.controller;

import com.example.demo.dto.request.CompleteBusinessRegistrationDto;
import com.example.demo.dto.request.CompleteRegistrationDto;
import com.example.demo.dto.request.ForgotPasswordDto;
import com.example.demo.dto.request.LoginDto;
import com.example.demo.dto.request.ResetPasswordDto;
import com.example.demo.dto.request.SignUpDto;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.exceptions.ValidationException;
import com.example.demo.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final CustomerService customerService;

    @PostMapping("/sign-up")
    public ResponseEntity<ApiResponse> signUp(@RequestBody @Valid SignUpDto signUpDto) throws ValidationException {
        System.out.println("Request body comes here " + signUpDto.toString());
        return customerService.signUp(signUpDto);}
    @PostMapping("/complete-registration/{token}")
    public ResponseEntity<ApiResponse> completeRegistration(@PathVariable String token, @Valid @RequestBody CompleteRegistrationDto completeRegistrationDto) {
        return customerService.completeRegistration(token, completeRegistrationDto);}
    @PostMapping("/corporate/complete-business-registration/{token}")
    public ResponseEntity<ApiResponse> completeBusinessRegistration(@PathVariable String token, @Valid @RequestBody CompleteBusinessRegistrationDto completeBusinessRegistrationDto) {
        return customerService.completeBusinessRegistration(token, completeBusinessRegistrationDto);}
    @PostMapping("/login")
    public ResponseEntity<String> login(@Valid @RequestBody LoginDto loginDto) {
        return customerService.login(loginDto);}
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse> customerForgotPassword(@Valid @RequestBody ForgotPasswordDto forgotPasswordDto){
        return customerService.forgotPassword(forgotPasswordDto);}
    @PostMapping("/reset-password/{token}")
    public ResponseEntity<ApiResponse> resetPassword(@PathVariable String token, @Valid @RequestBody ResetPasswordDto resetPasswordDto) {
        return customerService.resetPassword(token, resetPasswordDto);}

}