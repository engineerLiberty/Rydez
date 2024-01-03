package com.example.demo.controller;

import com.example.demo.dto.request.ChangeDistanceDto;
import com.example.demo.dto.request.ChangePriceDto;
import com.example.demo.dto.request.CreateDistancePriceDto;
import com.example.demo.dto.request.DistanceMapDto;
import com.example.demo.dto.request.MakeStaffDto;
import com.example.demo.dto.request.OrdersHistoryDto;
import com.example.demo.dto.response.AllOrdersDetailsResponseDto;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.CustomerProfileResponse;
import com.example.demo.dto.response.DistancePriceResponse;
import com.example.demo.dto.response.DistancePriceResponseDto;
import com.example.demo.dto.response.GetAllStaffProfileDto;
import com.example.demo.dto.response.GetRiderProfileResponse;
import com.example.demo.dto.response.LoggedInStaffProfileResponseDto;
import com.example.demo.dto.response.OrderDetailsResponse;
import com.example.demo.enums.OrderStatus;
import com.example.demo.enums.Role;
import com.example.demo.model.Bike;
import com.example.demo.model.DPrice;
import com.example.demo.model.DistancePrice;
import com.example.demo.model.Feedback;
import com.example.demo.model.Orders;
import com.example.demo.model.Staff;
import com.example.demo.service.StaffService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/auth/super-admin")
public class SuperAdminController {

    private final StaffService staffService;


    @GetMapping("/view-an-order/{referenceNumber}")
    Optional<OrderDetailsResponse> viewAnOrderByReferenceNumber(@PathVariable String referenceNumber) {
        return staffService.viewAnOrderByReferenceNumber(referenceNumber);
    }

    @GetMapping("/view-orders-by-status/{orderStatus}")
    List<Orders> viewAllOrdersByStatus(@PathVariable OrderStatus orderStatus) {
        return staffService.viewAllOrdersByStatus(orderStatus);
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
        return staffService.exportOrdersDataToExcel(response);
    }

    @DeleteMapping("/delete-staff/{staffId}")
    public ResponseEntity<ApiResponse> deleteStaff(@PathVariable Long staffId) {
        return staffService.deleteStaff(staffId);
    }

    @GetMapping("/view-all-staff")
    public List<GetAllStaffProfileDto> viewAllStaff() {
        return staffService.viewAllStaff();
    }

    @GetMapping("/view-staff-details/{staffId}")
    public Optional<GetAllStaffProfileDto> viewStaffDetails(@PathVariable Long staffId) {
        return staffService.viewStaffDetails(staffId);
    }

    @GetMapping("/daily-orders")
    public List<Orders> viewAllOrdersToday() {
        return staffService.viewAllOrdersToday();
    }

    @GetMapping("/weekly-orders")
    public List<Orders> viewAllOrdersInAWeek(@Valid @RequestBody OrdersHistoryDto ordersHistoryDto) {
        return staffService.viewAllOrdersInAWeek(ordersHistoryDto);
    }

    @GetMapping("/monthly-orders")
    public List<Orders> viewAllOrdersInAMonth(@Valid @RequestBody OrdersHistoryDto ordersHistoryDto) {
        return staffService.viewAllOrdersInAMonth(ordersHistoryDto);
    }

    @PostMapping("/create-distance-price")
    public String createDistancePriceList(@Valid @RequestBody CreateDistancePriceDto distancePriceDto) {
        return staffService.createDistancePriceList(distancePriceDto);
    }

    @PatchMapping("/change-price")
    String changePrice(@Valid @RequestBody ChangePriceDto changePriceDto) {
        return staffService.changePrice(changePriceDto);
    }

    @PatchMapping("/change-distance")
    String changeDistance(@Valid @RequestBody ChangeDistanceDto changeDistanceDto) {
        return staffService.changeDistance(changeDistanceDto);
    }

    @DeleteMapping("/delete-distance-price")
    String deleteDistancePrice(@Valid @RequestBody Double distance) {
        return staffService.deleteDistancePrice(distance);
    }

    @GetMapping("/view-all-distance-price")
    public List<DistancePriceResponseDto> viewDistancePriceList() {
        return staffService.viewDistancePriceList();
    }

    @GetMapping("/view-all-customers")
    public List<CustomerProfileResponse> viewAllCustomers() {
        return staffService.viewAllCustomers();
    }

    @PostMapping("/make-admin")
    public ResponseEntity<ApiResponse> makeAdmin(@Valid @RequestBody MakeStaffDto makeStaffDto) {
        return staffService.makeAdmin(makeStaffDto);
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

    @GetMapping("/all-feedback/{pageNumber}/{pageSize}")
    public Page<Feedback> viewAllFeedbacks(@PathVariable Integer pageNumber, @PathVariable Integer pageSize) {
        return staffService.viewAllFeedbacks(pageNumber, pageSize);
    }

    @GetMapping("/get-all-orders")
    public List<AllOrdersDetailsResponseDto> getAllOrders() {
        return staffService.getAllOrders();
    }

    @GetMapping("/get-all-bikes")
    public List<Bike> getAllBikes() {
        return staffService.getAllBikes();
    }

    @GetMapping("/get-one-bike/{id}")
    public Optional<Bike> getABike(@PathVariable Long id) {
        return staffService.getABike(id);
    }

    @GetMapping("/get-a-rider/{id}")
    public Optional<GetRiderProfileResponse> viewRiderById(@PathVariable Long id) {
        return staffService.viewRiderById(id);
    }

    @GetMapping("/view-rider-by-staffId/{staffId}")
    public Optional<GetRiderProfileResponse> viewRider(@PathVariable Long staffId) {
        return staffService.viewRider(staffId);
    }

    @GetMapping("/view-all-riders")
    public List<GetRiderProfileResponse> getAllRiders() {
        return staffService.getAllRiders();
    }

    @GetMapping("/customers-count")
    public long customersCount() {
        return staffService.customersCount();
    }

    @GetMapping("/admin-count")
    public long adminCount() {
        return staffService.adminCount();
    }

    @GetMapping("/riders-count")
    public long ridersCount() {
        return staffService.ridersCount();
    }

    @GetMapping("/get-customer-by-clientCode/{clientCode}")
    public Optional<CustomerProfileResponse> getCustomer(@PathVariable String clientCode) {
        return staffService.getCustomer(clientCode);
    }

    @GetMapping("/get-staff-by-id/{id}")
    public Optional<GetAllStaffProfileDto> viewAStaffDetails(@PathVariable Long id) {
        return staffService.viewAStaffDetails(id);
    }

    @GetMapping("/view-order-by-id/{id}")
    public Optional<AllOrdersDetailsResponseDto> viewAnOrder(@PathVariable Long id) {
        return staffService.viewAnOrder(id);
    }

}
//    @PostMapping("/create-dp-map")
//    public DPrice createDPMap(@Valid @RequestBody DistanceMapDto distanceMapDto) {
//        return staffService.createDPMap(distanceMapDto);
//    }
//
//    @GetMapping("/view-dp-list")
//    public List<DistancePriceResponse> distanceList() {
//        return staffService.distanceList();
//    }
//    }
