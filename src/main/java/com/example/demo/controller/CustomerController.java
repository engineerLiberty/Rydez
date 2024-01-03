package com.example.demo.controller;

import com.example.demo.dto.request.CancelABookingDto;
import com.example.demo.dto.request.ChangePasswordDto;
import com.example.demo.dto.request.DirectDeliveryDto;
import com.example.demo.dto.request.FeedbackDto;
import com.example.demo.dto.request.RiderFeedbackDto;
import com.example.demo.dto.request.ThirdPartySenderDto;
import com.example.demo.dto.request.TrackingDto;
import com.example.demo.dto.request.UpdateCustomerDetailsDto;
import com.example.demo.dto.request.WeeklyOrderSummaryDto;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.OrderDetailsResponse;
import com.example.demo.dto.response.OrderPriceResponse;
import com.example.demo.dto.response.LoggedInUserProfileResponse;
import com.example.demo.model.Orders;
import com.example.demo.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.json.simple.parser.ParseException;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth/client")
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse> changePassword(@Valid @RequestBody ChangePasswordDto changePasswordDto) {
        return customerService.changePassword(changePasswordDto);}
    @PostMapping("/new-order")
    public ResponseEntity<ApiResponse> bookADelivery(@Valid @RequestBody OrderPriceResponse orderPriceResponse) throws IOException, ParseException, InterruptedException {
        return customerService.bookADelivery(orderPriceResponse);}

    @PatchMapping("/cancel-order/{referenceNumber}")
    public ResponseEntity<ApiResponse> cancelABooking(@PathVariable String referenceNumber, @Valid @RequestBody CancelABookingDto cancelABookingDto) {
        return customerService.cancelABooking(referenceNumber, cancelABookingDto);}
    @GetMapping("/weekly-summary")
    public List<Orders> weeklyOrderSummary(@Valid @RequestBody WeeklyOrderSummaryDto weeklyOrderSummaryDto) {
        return customerService.weeklyOrderSummary(weeklyOrderSummaryDto);}
    @PatchMapping("/confirm-receipt/{referenceNumber}")
    public String confirmOrderReceipt(@PathVariable String referenceNumber, @Valid @RequestBody RiderFeedbackDto riderFeedbackDto) {
        return customerService.confirmDelivery(referenceNumber, riderFeedbackDto);}
    @PatchMapping("/feedback/{referenceNumber}")
    public String giveFeedback(@Valid @RequestBody FeedbackDto feedbackDto) {
        return customerService.giveFeedback(feedbackDto);}
    @PostMapping("/tracking-request")
    public ResponseEntity<ApiResponse> trackingRequest(@Valid @RequestBody TrackingDto trackingDto) {
        return customerService.trackingRequest(trackingDto);}
    @PatchMapping("/update-customer-details")
    public ResponseEntity<ApiResponse> updateCustomerDetails(@Valid @RequestBody UpdateCustomerDetailsDto updateCustomerDetailsDto) {
        return customerService.updateCustomerDetails(updateCustomerDetailsDto);
    }

    //returns customers count automatically every midnight.
    @GetMapping("/update-customers-count")
    @Scheduled(cron = "0 0 0 * * ?")
    public long updateCustomersCount() {
        return customerService.updateCustomersCount();
    }

    // returns orders count automatically every midnight.
    @GetMapping("/update-total-transactions")
    @Scheduled(cron = "0 0 0 * * ?")
    public long countAllOrders () {
        return customerService.countAllOrders();
    }

    @GetMapping("/logged-in-customer-profile")
    public ResponseEntity<LoggedInUserProfileResponse> displayUserInformation() {
        return customerService.displayUserInformation();
    }

    @PostMapping("/direct/return-distance")
    public OrderPriceResponse directDeliveryDistanceCalculator(@Valid @RequestBody DirectDeliveryDto directDeliveryDto) throws IOException, InterruptedException, ParseException {
        return customerService.directDeliveryDistanceCalculator(directDeliveryDto);
    }

    @PostMapping("/third-party/return-distance")
    public OrderPriceResponse indirectDeliveryDistanceCalculator(@Valid @RequestBody ThirdPartySenderDto thirdPartySenderDto) throws IOException, InterruptedException, ParseException {
        return customerService.indirectDeliveryDistanceCalculator(thirdPartySenderDto);
    }

    @PatchMapping("/update-status/abort")
    public void abort(@Valid @RequestBody OrderPriceResponse orderPriceResponse) {
        customerService.abort(orderPriceResponse);
    }

    @GetMapping("/return-all-my-order-details/{clientCode}")
    public List<OrderDetailsResponse> allMyOrders(@PathVariable Long clientCode) {
        return customerService.allMyOrders(clientCode);
    }

    @GetMapping("/return-sort-by-id/{clientCode}")
    public List<Orders> sortedCustomersOrdersDesc(@PathVariable Long clientCode) {
        return customerService.sortedCustomersOrdersById(clientCode);
    }

    @GetMapping("/return-one/{id}")
    public Optional<OrderDetailsResponse> returnById(@PathVariable Long id) {
        return customerService.returnById(id);
    }

}
