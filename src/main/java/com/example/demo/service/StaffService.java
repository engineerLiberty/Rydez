package com.example.demo.service;

import com.example.demo.dto.request.AssignRiderToBikeDto;
import com.example.demo.dto.request.ChangeDistanceDto;
import com.example.demo.dto.request.ChangePasswordDto;
import com.example.demo.dto.request.ChangePriceDto;
import com.example.demo.dto.request.CreateDistancePriceDto;
import com.example.demo.dto.request.DispatchOrderDto;
import com.example.demo.dto.request.DistanceMapDto;
import com.example.demo.dto.request.MakeStaffDto;
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
import com.example.demo.dto.response.DistancePriceResponse;
import com.example.demo.dto.response.DistancePriceResponseDto;
import com.example.demo.dto.response.GetAllStaffProfileDto;
import com.example.demo.dto.response.GetRiderProfileResponse;
import com.example.demo.dto.response.LoggedInStaffProfileResponseDto;
import com.example.demo.dto.response.OrderDetailsResponse;
import com.example.demo.enums.OrderStatus;
import com.example.demo.enums.RiderStatus;
import com.example.demo.model.Bike;
import com.example.demo.model.DPrice;
import com.example.demo.model.DistancePrice;
import com.example.demo.model.Feedback;
import com.example.demo.model.Orders;
import com.example.demo.model.Staff;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface StaffService {

    ResponseEntity<ApiResponse> updateStaffInformation(StaffRelevantDetailsDto staffRelevantDetailsDto);

    ResponseEntity<ApiResponse> changePassword(ChangePasswordDto changePasswordDto);

    ResponseEntity<String> dispatchOrder(String referenceNumber, DispatchOrderDto dispatchOrderDto) throws IOException, MessagingException;

    ResponseEntity<ApiResponse> changeOrderStatus(String referenceNumber);

    ResponseEntity<ApiResponse> registerABike(RegisterBikeDto registerBikeDto);

    ResponseEntity<ApiResponse> registerARider(RegisterRiderDto registerRiderDto);

    ApiResponse assignBikeToRider(AssignRiderToBikeDto assignRiderToBikeDto);

    Optional<OrderDetailsResponse> viewAnOrderByReferenceNumber(String referenceNumber);

    List<Orders> viewAllOrdersByStatus(OrderStatus orderStatus);

    List<Staff> viewAllRidersByStatus(RiderStatus riderStatus);

    Integer countRidesPerRider(Long staffId);

    Optional<GetAllStaffProfileDto> viewStaffDetails(Long staffId);

    List<GetAllStaffProfileDto> viewAllStaff();

    ResponseEntity<ApiResponse> deleteStaff(Long staffId);

    ResponseEntity<ApiResponse> makeAdmin(MakeStaffDto makeStaffDto);

    List<Orders> clientWeeklyOrderSummary(WeeklyOrderSummaryDto weeklyOrderSummaryDto);

    List<Orders> viewAllOrdersToday();

    List<Orders> viewAllOrders();

    List<Orders> exportOrdersDataToExcel(HttpServletResponse response) throws IOException;

    List<Orders> viewAllOrdersInAMonth(OrdersHistoryDto ordersHistoryDto);

    List<Orders> viewAllOrdersInAWeek(OrdersHistoryDto ordersHistoryDto);

    int viewDeliveryCountOfRider(Long riderId, RidersDeliveryCountPerMonthDto ridersDeliveryCountPerMonthDto);

    BigDecimal weeklyBill(Long clientCode, PeriodicBillDto periodicBillDto);

    List<Orders> generatePeriodicOrderDetailsPdf(Long clientCode, HttpServletResponse response, PeriodicBillDto periodicBillDto) throws IOException, MessagingException;

    Page<Feedback> viewAllFeedbacks(Integer pageNumber, Integer pageSize);

    Page<Feedback> viewPeriodicFeedback(Integer pageNumber, Integer pageSize, LocalDate startDate, LocalDate endDate);

    Page<Orders> getOrdersPagination(Integer pageNumber, Integer pageSize, String sortProperty);

    String createDistancePriceList(CreateDistancePriceDto distancePriceDto);

    String changePrice(ChangePriceDto changePriceDto);

    String changeDistance(ChangeDistanceDto changeDistanceDto);

    String deleteDistancePrice(Double distance);

    List<DistancePriceResponseDto> viewDistancePriceList();

    List<CustomerProfileResponse> viewAllCustomers();

    String sendOrdersSummary(Long clientCode, PeriodicBillDto periodicBillDto) throws MessagingException, IOException;

    long automaticStaffCount();

    public ResponseEntity<LoggedInStaffProfileResponseDto> displayStaffInformation();

    Optional<CustomerProfileResponse> getCustomer (Long id);

    Optional<CustomerProfileResponse> getCustomer (String clientCode);


    Optional<Bike> getBike (String bikeNumber);

    Optional<GetRiderProfileResponse> viewRider (Long staffId);

    Optional<AllOrdersDetailsResponseDto> viewAnOrder (Long id);

    List<Bike>getAllBikes();

    Optional<Bike> getABike(Long id);

    List<AllOrdersDetailsResponseDto> getAllOrders();

    Optional<GetRiderProfileResponse> viewRiderById(Long id);

    List<GetRiderProfileResponse> getAllRiders();

    long customersCount();

    long adminCount();

    long ridersCount();

    Optional<GetAllStaffProfileDto> viewAStaffDetails(Long id);

    List<DistancePriceResponse> distanceList ();

    DPrice createDPMap (DistanceMapDto distanceMapDto);


}


