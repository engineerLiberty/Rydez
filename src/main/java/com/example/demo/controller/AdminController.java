package com.example.demo.controller;

import com.example.demo.dto.request.AssignRiderToBikeDto;
import com.example.demo.dto.request.ChangePasswordDto;
import com.example.demo.dto.request.CreateDistancePriceDto;
import com.example.demo.dto.request.DispatchOrderDto;
import com.example.demo.dto.request.OrdersHistoryDto;
import com.example.demo.dto.request.PeriodicBillDto;
import com.example.demo.dto.request.RegisterBikeDto;
import com.example.demo.dto.request.RegisterRiderDto;
import com.example.demo.dto.request.RidersDeliveryCountPerMonthDto;
import com.example.demo.dto.request.StaffRelevantDetailsDto;
import com.example.demo.dto.request.WeeklyOrderSummaryDto;
import com.example.demo.dto.response.AllOrdersDetailsResponseDto;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.CustomerProfileResponse;
import com.example.demo.dto.response.DistancePriceResponseDto;
import com.example.demo.dto.response.GetAllStaffProfileDto;
import com.example.demo.dto.response.GetRiderProfileResponse;
import com.example.demo.dto.response.LoggedInStaffProfileResponseDto;
import com.example.demo.dto.response.OrderDetailsResponse;
import com.example.demo.enums.OrderStatus;
import com.example.demo.enums.RiderStatus;
import com.example.demo.model.Bike;
import com.example.demo.model.DistancePrice;
import com.example.demo.model.Feedback;
import com.example.demo.model.Orders;
import com.example.demo.model.Staff;
import com.example.demo.service.StaffService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/auth/admin")
public class AdminController {

    private final StaffService staffService;

    @PostMapping("/update-staff-info")
    public ResponseEntity<ApiResponse> updateStaffInformation(@Valid @RequestBody StaffRelevantDetailsDto staffRelevantDetailsDto) {
        return staffService.updateStaffInformation(staffRelevantDetailsDto);}
    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse> changePassword(@Valid @RequestBody ChangePasswordDto changePasswordDto) {
        return staffService.changePassword(changePasswordDto);}
    @PostMapping("/dispatch-order/{referenceNumber}")
    public ResponseEntity<String> dispatchOrder(@PathVariable String referenceNumber, @Valid @RequestBody DispatchOrderDto dispatchOrderDto) throws IOException, MessagingException {
        return staffService.dispatchOrder(referenceNumber, dispatchOrderDto);}
    @PatchMapping("/change-order-status/{referenceNumber}")
    public ResponseEntity<ApiResponse> changeOrderStatus(@PathVariable String referenceNumber) {
        return staffService.changeOrderStatus(referenceNumber);}
    @PostMapping("/register-bike")
    public ResponseEntity<ApiResponse> registerABike(@Valid @RequestBody RegisterBikeDto registerBikeDto) {
        return staffService.registerABike(registerBikeDto);}
    @PatchMapping("/assign-bike")
    public ApiResponse assignBikeToRider(@RequestBody AssignRiderToBikeDto assignRiderToBikeDto) {
        return staffService.assignBikeToRider(assignRiderToBikeDto);}
    @GetMapping("/view-riders-by-status/{riderStatus}")
    public List<Staff> viewAllRidersByStatus(@PathVariable RiderStatus riderStatus) {
        return staffService.viewAllRidersByStatus(riderStatus);}

    @GetMapping("/return-an-order/{referenceNumber}")
    Optional<OrderDetailsResponse> viewAnOrderByReferenceNumber(@PathVariable String referenceNumber) {
        return staffService.viewAnOrderByReferenceNumber(referenceNumber);}
    @GetMapping("/view-orders-by-status/{orderStatus}")
    List<Orders> viewAllOrdersByStatus(@PathVariable OrderStatus orderStatus) {
        return staffService.viewAllOrdersByStatus(orderStatus);}
    @GetMapping("/count-trips/{staffId}")
    Integer countRidesPerRider(@PathVariable Long staffId) {
        return staffService.countRidesPerRider(staffId);
    }
    @GetMapping("/view-all-orders")
    public List<Orders> viewAllOrders() {
        return staffService.viewAllOrders();
    }
    @GetMapping("/order-details-export")
    public List<Orders> exportOrdersDataToExcel(HttpServletResponse response) throws IOException {
        response.setContentType("application/octet-stream");
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=Orders_Information.xlsx";
        response.setHeader(headerKey, headerValue);
        return staffService.exportOrdersDataToExcel(response);}

    @PostMapping("/register-rider")
    public ResponseEntity<ApiResponse> registerARider(@Valid @RequestBody RegisterRiderDto registerRiderDto) {
        return staffService.registerARider(registerRiderDto);}
    @GetMapping("/view-client-weekly-orders")
    public List<Orders> clientWeeklyOrderSummary(@Valid @RequestBody WeeklyOrderSummaryDto weeklyOrderSummaryDto) throws Exception {
        return staffService.clientWeeklyOrderSummary(weeklyOrderSummaryDto);}
    @GetMapping("/daily-orders")
    public List<Orders> viewAllOrdersToday() {return staffService.viewAllOrdersToday();}
    @GetMapping("/weekly-orders")
    public List<Orders> viewAllOrdersInAWeek(@Valid @RequestBody OrdersHistoryDto ordersHistoryDto) {
        return staffService.viewAllOrdersInAWeek(ordersHistoryDto);}
    @GetMapping("/monthly-orders")
    public List<Orders> viewAllOrdersInAMonth(@Valid @RequestBody OrdersHistoryDto ordersHistoryDto) {
        return staffService.viewAllOrdersInAMonth(ordersHistoryDto);}
    @GetMapping("/riders-deliveries-count/{riderId}")
    public int viewDeliveryCountOfRider(@PathVariable Long riderId, @Valid @RequestBody RidersDeliveryCountPerMonthDto ridersDeliveryCountPerMonthDto) {
        return staffService.viewDeliveryCountOfRider(riderId, ridersDeliveryCountPerMonthDto);}
    @GetMapping("/clients-bill/{clientCode}")
    public BigDecimal weeklyBill(@PathVariable Long clientCode, @Valid @RequestBody PeriodicBillDto periodicBillDto) {
        return staffService.weeklyBill(clientCode, periodicBillDto);}
    @GetMapping("/clients-orders-invoice/{clientCode}")
    public List<Orders> generatePeriodicOrderDetailsPdf(@PathVariable Long clientCode, HttpServletResponse response, @Valid @RequestBody PeriodicBillDto periodicBillDto) throws MessagingException, IOException {
        return staffService.generatePeriodicOrderDetailsPdf(clientCode, response, periodicBillDto);}
    @GetMapping("/all-feedback/{pageNumber}/{pageSize}")
    public Page<Feedback> viewAllFeedbacks(@PathVariable Integer pageNumber, @PathVariable Integer pageSize) {
        return staffService.viewAllFeedbacks(pageNumber, pageSize);}
    @GetMapping("/periodic-feedback/{pageNumber}/{pageSize}")
    public Page<Feedback> viewPeriodicFeedback(@PathVariable Integer pageNumber, @PathVariable Integer pageSize, @Valid @RequestBody LocalDate startDate, @Valid @RequestBody LocalDate endDate) {
        return staffService.viewPeriodicFeedback(pageNumber, pageSize, startDate, endDate);}
    @GetMapping("/paging-and-sorting-orders/{pageNumber}/{pageSize}")
    public Page<Orders> getOrdersPagination(@PathVariable Integer pageNumber, @PathVariable Integer pageSize) {
        return staffService.getOrdersPagination(pageNumber, pageSize, null);}
    @GetMapping("/paging-and-sorting-orders/{pageNumber}/{pageSize}/{sortProperty}")
    public Page<Orders> getOrdersPagination(@PathVariable Integer pageNumber,
                                            @PathVariable Integer pageSize,
                                            @PathVariable String sortProperty) {
        return staffService.getOrdersPagination(pageNumber, pageSize, sortProperty);}
    @PostMapping("/create-distance-price")
    public String createDistancePriceList(@Valid @RequestBody CreateDistancePriceDto distancePriceDto) {
        return staffService.createDistancePriceList(distancePriceDto);}

    @GetMapping("/view-all-distance-price")
    public List<DistancePriceResponseDto> viewDistancePriceList(){
        return staffService.viewDistancePriceList();}
    @GetMapping("/view-all-customers")
    public List<CustomerProfileResponse> viewAllCustomers() {
        return staffService.viewAllCustomers();}
    @PostMapping("/mail-orders-summary-to-customer/{clientCode}")
    public String sendOrdersSummary(@PathVariable Long clientCode, @RequestBody PeriodicBillDto periodicBillDto) throws MessagingException, IOException {
        return  staffService.sendOrdersSummary(clientCode, periodicBillDto);
    }

    // returns staff count automatically every midnight.
    @GetMapping("/staff-count-daily-auto-update")
    @Scheduled(cron = "0 0 0 * * ?")
    public long automaticStaffCount() {
        return staffService.automaticStaffCount();
    }

    @GetMapping("/logged-in-staff-profile")
    public ResponseEntity<LoggedInStaffProfileResponseDto> displayStaffInformation() {
        return staffService.displayStaffInformation();
    }

    @GetMapping("/get-customer-profile/{id}")
    public Optional<CustomerProfileResponse> getCustomer(@PathVariable Long id) {
        return staffService.getCustomer(id);
    }

    @GetMapping("/get-bike/{bikeNumber}")
    public Optional<Bike> getBike(@PathVariable String bikeNumber) {
        return staffService.getBike(bikeNumber);
    }


//    @GetMapping("/view-rider-details/{staffId}")
//    public Optional<GetRiderProfileResponse> viewRider(@PathVariable Long staffId) {
//        return staffService.viewRider(staffId);
//    }


    @GetMapping("/view-an-order/{id}") //tested and is fine
    public Optional<AllOrdersDetailsResponseDto> viewAnOrder(@PathVariable Long id) {
        return staffService.viewAnOrder(id);
    }


    @GetMapping("/get-all-orders") //tested and is fine
    public List<AllOrdersDetailsResponseDto> getAllOrders() {
        return staffService.getAllOrders();
    }

    @GetMapping("/get-all-bikes") //tested and is fine
    public List<Bike> getAllBikes() {
        return staffService.getAllBikes();
    }

    @GetMapping("/get-one-bike/{id}") //tested and is fine
    public Optional<Bike> getABike(@PathVariable Long id) {
        return staffService.getABike(id);
    }

    @GetMapping("/get-a-rider/{id}") //tested and is fine
    public Optional<GetRiderProfileResponse> viewRiderById(@PathVariable Long id) {
        return staffService.viewRiderById(id);
    }

    @GetMapping("/view-rider-by-staffId/{staffId}")
    public Optional<GetRiderProfileResponse> viewRider(@PathVariable Long staffId) {
        return staffService.viewRider(staffId);
    }

    @GetMapping("/view-all-riders") //tested and is fine
    public List<GetRiderProfileResponse> getAllRiders() {
        return staffService.getAllRiders();
    }

    @GetMapping("/get-customer-by-clientCode/{clientCode}")
    public Optional<CustomerProfileResponse> getCustomer(@PathVariable String clientCode) {
        return staffService.getCustomer(clientCode);
    }

    @GetMapping("/view-staff-details/{staffId}")
    public Optional<GetAllStaffProfileDto> viewStaffDetails(@PathVariable Long staffId) {return staffService.viewStaffDetails(staffId);}

}