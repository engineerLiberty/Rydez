package com.example.demo.service;

import com.example.demo.dto.request.CancelABookingDto;
import com.example.demo.dto.request.ChangePasswordDto;
import com.example.demo.dto.request.CompleteBusinessRegistrationDto;
import com.example.demo.dto.request.CompleteRegistrationDto;
import com.example.demo.dto.request.DirectDeliveryDto;
import com.example.demo.dto.request.FeedbackDto;
import com.example.demo.dto.request.ForgotPasswordDto;
import com.example.demo.dto.request.LoginDto;
import com.example.demo.dto.request.ResetPasswordDto;
import com.example.demo.dto.request.RiderFeedbackDto;
import com.example.demo.dto.request.SignUpDto;
import com.example.demo.dto.request.ThirdPartySenderDto;
import com.example.demo.dto.request.TrackingDto;
import com.example.demo.dto.request.UpdateCustomerDetailsDto;
import com.example.demo.dto.request.WeeklyOrderSummaryDto;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.CustomerProfileResponse;
import com.example.demo.dto.response.OrderDetailsResponse;
import com.example.demo.dto.response.OrderPriceResponse;
import com.example.demo.dto.response.LoggedInUserProfileResponse;
import com.example.demo.exceptions.ValidationException;
import com.example.demo.model.Orders;
import org.json.simple.parser.ParseException;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface CustomerService {

    ResponseEntity<ApiResponse> signUp (SignUpDto signUpDto) throws ValidationException;
    ResponseEntity<ApiResponse> completeRegistration (String token, CompleteRegistrationDto completeRegistrationDto);
    ResponseEntity<ApiResponse> completeBusinessRegistration (String token, CompleteBusinessRegistrationDto completeBusinessRegistrationDto);
    ResponseEntity<String> login (LoginDto loginDto);
    ResponseEntity<ApiResponse> forgotPassword(ForgotPasswordDto forgotPasswordDto);
    ResponseEntity<ApiResponse> resetPassword (String token, ResetPasswordDto resetPasswordDto);
    ResponseEntity<ApiResponse> changePassword (ChangePasswordDto changePasswordDto);
    ResponseEntity<ApiResponse> bookADelivery(OrderPriceResponse orderPriceResponse) throws IOException, ParseException, InterruptedException;
    ResponseEntity<ApiResponse> cancelABooking(String referenceNumber, CancelABookingDto cancelABookingDto);
    String confirmDelivery(String referenceNumber, RiderFeedbackDto riderFeedbackDto);
    String giveFeedback(FeedbackDto feedbackDto);
    List<Orders> weeklyOrderSummary (WeeklyOrderSummaryDto weeklyOrderSummaryDto);
    ResponseEntity<ApiResponse> trackingRequest (TrackingDto trackingDto);
    ResponseEntity<ApiResponse> updateCustomerDetails (UpdateCustomerDetailsDto updateCustomerDetailsDto);
    List<CustomerProfileResponse> viewAll();
    long updateCustomersCount();
    long countAllOrders();
    ResponseEntity<LoggedInUserProfileResponse> displayUserInformation();
    OrderPriceResponse directDeliveryDistanceCalculator (DirectDeliveryDto directDeliveryDto) throws IOException, InterruptedException, ParseException;
    OrderPriceResponse indirectDeliveryDistanceCalculator(ThirdPartySenderDto thirdPartySenderDto) throws IOException, InterruptedException, ParseException;
//    void abort(OrderPriceResponse orderPriceResponse);

    void abort(String referenceNumber);

    List<OrderDetailsResponse> allMyOrders(Long clientCode);
    List<Orders> sortedCustomersOrders(Long clientCode);

    List<Orders> sortedCustomersOrdersById(Long clientCode);

    Optional<OrderDetailsResponse> returnById (Long id);
}
