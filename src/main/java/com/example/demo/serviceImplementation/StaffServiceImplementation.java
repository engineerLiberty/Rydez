package com.example.demo.serviceImplementation;

import com.example.demo.configuration.EmailService;
import com.example.demo.dto.request.AssignRiderToBikeDto;
import com.example.demo.dto.request.ChangeDistanceDto;
import com.example.demo.dto.request.ChangePasswordDto;
import com.example.demo.dto.request.ChangePriceDto;
import com.example.demo.dto.request.CreateDistancePriceDto;
import com.example.demo.dto.request.DispatchOrderDto;
import com.example.demo.dto.request.DistanceMapDto;
import com.example.demo.dto.request.MailDto;
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
import com.example.demo.dto.response.LoggedInUserProfileResponse;
import com.example.demo.dto.response.OrderDetailsResponse;
import com.example.demo.enums.CustomerType;
import com.example.demo.enums.OrderStatus;
import com.example.demo.enums.PaymentType;
import com.example.demo.enums.RiderStatus;
import com.example.demo.enums.Role;
import com.example.demo.exceptions.ResourceAlreadyExistsException;
import com.example.demo.exceptions.ResourceNotFoundException;
import com.example.demo.exceptions.RiderUnavailableException;
import com.example.demo.exceptions.UnsupportedOperationException;
import com.example.demo.exceptions.UserNotFoundException;
import com.example.demo.exceptions.ValidationException;
import com.example.demo.model.Bike;
import com.example.demo.model.Customer;
import com.example.demo.model.DPrice;
import com.example.demo.model.DistancePrice;
import com.example.demo.model.Feedback;
import com.example.demo.model.Orders;
import com.example.demo.model.Staff;
import com.example.demo.repository.BikeRepository;
import com.example.demo.repository.CustomerRepository;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.StaffRepository;
import com.example.demo.service.CustomerService;
import com.example.demo.service.DistancePriceService;
import com.example.demo.service.FeedbackService;
import com.example.demo.service.StaffService;
import com.example.demo.utils.AppUtil;
import com.example.demo.utils.ExcelExportUtils;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.webjars.NotFoundException;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StaffServiceImplementation implements StaffService {

    private final StaffRepository staffRepository;
    private final EmailService emailService;
    private final CustomerService customerService;
    private final DistancePriceService distancePriceService;
    private final FeedbackService feedbackService;
    private final BikeRepository bikeRepository;
    private final AppUtil appUtil;
    private final OrderRepository orderRepository;
    private final PasswordEncoder passwordEncoder;
    private final CustomerRepository customerRepository;


    @Override
    public ResponseEntity<ApiResponse> updateStaffInformation(StaffRelevantDetailsDto staffRelevantDetailsDto) {
        Staff user = appUtil.getLoggedInStaff();
        user.setState(staffRelevantDetailsDto.getStateOfOrigin());
        user.setNextOfKinFirstName(staffRelevantDetailsDto.getNextOfKinFirstName());
        user.setNextOfKinLastName(staffRelevantDetailsDto.getNextOfKinLastName());
        user.setNextOfKinAddress(staffRelevantDetailsDto.getNextOfKinAddress());
        user.setNextOfKinPhoneNumber(staffRelevantDetailsDto.getNextOfKinPhoneNumber());
        user.setStateOfOrigin(staffRelevantDetailsDto.getStateOfOrigin());
        staffRepository.save(user);
        return ResponseEntity.ok(new ApiResponse("Successful", "Staff information update successful", null));}


    @Override
    public ResponseEntity<ApiResponse> changePassword(ChangePasswordDto changePasswordDto) {
        Staff staff = appUtil.getLoggedInStaff();
        if (!(changePasswordDto.getConfirmNewPassword().equals(changePasswordDto.getNewPassword()))) {
            throw new InputMismatchException("Confirm password and Password do not match!");}
        staff.setPassword(passwordEncoder.encode(changePasswordDto.getNewPassword()));
        staffRepository.save(staff);
        return ResponseEntity.ok(new ApiResponse<>("Success", "Password change successful", null));}

@Override
public ResponseEntity<String> dispatchOrder(String referenceNumber, DispatchOrderDto dispatchOrderDto) throws IOException, MessagingException {
    Staff staff = appUtil.getLoggedInStaff();
    if (!(staff.getRole().equals(Role.ADMIN))) {
        throw new ValidationException("You are not permitted to perform this operation");}

    Optional<Orders> order = Optional.ofNullable(orderRepository.findByReferenceNumber(referenceNumber))
            .orElseThrow(() -> new ResourceNotFoundException("Order with the reference number " + referenceNumber + " does not exist"));
    if (order.get().getOrderStatus().equals(OrderStatus.INPROGRESS))
        throw new UnsupportedOperationException("This Order has been dispatched!");
    if (order.get().getOrderStatus().equals(OrderStatus.COMPLETED))
        throw new UnsupportedOperationException("This Order has been completed");
    if (order.get().getOrderStatus().equals(OrderStatus.CANCELLED))
        throw new UnsupportedOperationException("This order has been cancelled by the customer");
    Optional<Staff> staff1 = Optional.ofNullable(staffRepository.findByStaffId(dispatchOrderDto.getRiderId())
            .orElseThrow(() -> new UserNotFoundException("Rider does not exist! Check the rider Id")));
    Optional<Bike> bike = Optional.ofNullable(bikeRepository.findByStaffId(staff1.get().getStaffId())
            .orElseThrow(() -> new ResourceNotFoundException("No bike has been assigned to this rider!")));
    Boolean assignExtraOrder = dispatchOrderDto.getAssignExtraOrder();

    if (staff1.get().getRiderStatus().equals(RiderStatus.Engaged) && assignExtraOrder.equals(true)) {
        int x = staff1.get().getRidesCount();
        staff1.get().setRidesCount(x + 1);
        staff1.get().setRiderStatus(RiderStatus.TwiceEngaged);
        staffRepository.save(staff1.get());
    } else if (!(staff1.get().getRiderStatus().equals(RiderStatus.Free) && assignExtraOrder.equals(false))) {
        throw new RiderUnavailableException("This rider is currently engaged. Kindly assign another rider to this order.");
    } else {
        staff1.get();
        int x = staff1.get().getRidesCount();
        staff1.get().setRidesCount(x + 1);
        staff1.get().setRiderStatus(RiderStatus.Engaged);
        staffRepository.save(staff1.get());
    }
    Optional<Customer> customer = customerRepository.findByClientCode(order.get().getClientCode());
    String email = customer.get().getEmail();

    order.get().setDispatchAdminNumber(staff.getStaffId());
    order.get().setBikeNumber(bike.get().getBikeNumber());
    order.get().setRiderPhoneNumber(staff1.get().getPhoneNumber());
    order.get().setRiderName(staff1.get().getFirstName());
    order.get().setRiderId(dispatchOrderDto.getRiderId());
    order.get().setOrderStatus(OrderStatus.INPROGRESS);

    DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy  hh:mm:ss");
    String currentDateTime = dateFormat.format(new Date());

    try {
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, new FileOutputStream("Order_Details.pdf"));

        document.open();
        Font font = FontFactory.getFont(FontFactory.COURIER_BOLD);
        font.setColor(Color.BLACK);
        font.setSize(16);
        Paragraph p1 = new Paragraph("Order Details", font);
        p1.setAlignment(Paragraph.ALIGN_CENTER);

        Paragraph p2 = new Paragraph();
        p2.setAlignment(Paragraph.ALIGN_LEFT);
        p2.setFont(FontFactory.getFont(FontFactory.COURIER, 12, Color.black));
        p2.setMultipliedLeading(2);

        if (order.get().getCustomerType().equals(CustomerType.Corporate)) {
            p2.add("Order Date: " + order.get().getCreatedAt() + '\n' +
                    "ReferenceNumber: " + order.get().getReferenceNumber() + '\n' +
                    "Company Name: " + order.get().getCompanyName() + '\n' +
                    "Company Id: " + order.get().getClientCode() + '\n' +
                    "Receiver name : " + order.get().getReceiverName() + '\n' +
                    "Receiver address: " + order.get().getDeliveryAddress() + '\n' +
                    "Receiver phone number: " + order.get().getReceiverPhoneNumber() + '\n' +
                    "Item type: " + order.get().getItemType() + '\n' +
                    "Item quantity: " + order.get().getItemQuantity() + '\n' +
                    "Date: " + order.get().getCreatedAt() + '\n' +
                    "Tracking number: " + order.get().getBikeNumber() + '\n' +
                    "Rider name: " + order.get().getRiderName() + '\n' +
                    "Rider Phone Number: " + order.get().getReceiverPhoneNumber() + '\n' +
                    "Current Date: " + currentDateTime);
        } else if (order.get().getThirdPartyPickUp().equals(true) && order.get().getPaymentType().equals(PaymentType.banktransfer)) {
            p2.add("Order Date: " + order.get().getCreatedAt() + '\n' +
                    "ReferenceNumber: " + order.get().getReferenceNumber() + '\n' +
                    "ClientId: " + order.get().getClientCode() + '\n' +
                    "Third-party name: " + order.get().getThirdPartyName() + '\n' +
                    "Third-party address: " + order.get().getThirdPartyAddress() + '\n' +
                    "Third-party phone number " + order.get().getThirdPartyPhoneNumber() + '\n' +
                    "Receiver name : " + order.get().getReceiverName() + '\n' +
                    "Receiver address: " + order.get().getDeliveryAddress() + '\n' +
                    "Receiver phone number: " + order.get().getReceiverPhoneNumber() + '\n' +
                    "Item type: " + order.get().getItemType() + '\n' +
                    "Item quantity: " + order.get().getItemQuantity() + '\n' +
                    "Date: " + order.get().getCreatedAt() + '\n' +
                    "Tracking number: " + order.get().getBikeNumber() + '\n' +
                    "Rider name: " + order.get().getRiderName() + '\n' +
                    "Rider Phone Number: " + order.get().getReceiverPhoneNumber() + '\n' +
                    "Current Date: " + currentDateTime + '\n' +
                    "Price: " + order.get().getPrice() + '\n' +
                    "Account Name: " + "AriXpress Delivery Nigeria Limited " + '\n' +
                    "Account Number: " + "0044232307 " + '\n' +
                    "Bank Name: " + "GTB");
        } else if (order.get().getThirdPartyPickUp().equals(true) && order.get().getPaymentType().equals(PaymentType.cash)) {
            p2.add("Order Date: " + order.get().getCreatedAt() + '\n' +
                    "ReferenceNumber: " + order.get().getReferenceNumber() + '\n' +
                    "ClientId: " + order.get().getClientCode() + '\n' +
                    "Third-party name: " + order.get().getThirdPartyName() + '\n' +
                    "Third-party address: " + order.get().getThirdPartyAddress() + '\n' +
                    "Third-party phone number " + order.get().getThirdPartyPhoneNumber() + '\n' +
                    "Receiver name : " + order.get().getReceiverName() + '\n' +
                    "Receiver address: " + order.get().getDeliveryAddress() + '\n' +
                    "Receiver phone number: " + order.get().getReceiverPhoneNumber() + '\n' +
                    "Item type: " + order.get().getItemType() + '\n' +
                    "Item quantity: " + order.get().getItemQuantity() + '\n' +
                    "Date: " + order.get().getCreatedAt() + '\n' +
                    "Price: " + order.get().getPrice() + '\n' +
                    "Tracking number: " + order.get().getBikeNumber() + '\n' +
                    "Rider name: " + order.get().getRiderName() + '\n' +
                    "Rider Phone Number: " + order.get().getReceiverPhoneNumber());
        } else if (order.get().getThirdPartyPickUp().equals(false) && order.get().getPaymentType().equals(PaymentType.banktransfer)) {
            p2.add("Order Date: " + order.get().getCreatedAt() + '\n' +
                    "ReferenceNumber: " + order.get().getReferenceNumber() + '\n' +
                    "ClientId: " + order.get().getClientCode() + '\n' +
                    "Client name: " + order.get().getCustomerFirstName() + " " + order.get().getCustomerLastName() + '\n' +
                    "Pick-up address: " + order.get().getPickUpAddress() + '\n' +
                    "Receiver name: " + order.get().getReceiverName() + '\n' +
                    "Receiver address: " + order.get().getDeliveryAddress() + '\n' +
                    "Receiver phone number: " + order.get().getReceiverPhoneNumber() + '\n' +
                    "Item type: " + order.get().getItemType() + '\n' +
                    "Item quantity: " + order.get().getItemQuantity() + '\n' +
                    "Date: " + order.get().getCreatedAt() + '\n' +
                    "Tracking Number: " + order.get().getBikeNumber() + '\n' +
                    "Rider name: " + order.get().getRiderName() + '\n' +
                    "Rider Phone Number: " + order.get().getReceiverPhoneNumber() + '\n' +
                    "Current Date: " + currentDateTime + '\n' +
                    "Price: " + order.get().getPrice() + '\n' +
                    "Account Name: " + "AriXpress Delivery Nigeria Limited " + '\n' +
                    "Account Number: " + "0044232307 " + '\n' +
                    "Bank Name: " + "GTB");
        } else {
            p2.add("Order Date: " + order.get().getCreatedAt() + '\n' +
                    "ReferenceNumber: " + order.get().getReferenceNumber() + '\n' +
                    "ClientId: " + order.get().getClientCode() + '\n' +
                    "Client name: " + order.get().getCustomerFirstName() + " " + order.get().getCustomerLastName() + '\n' +
                    "Pick-up address: " + order.get().getPickUpAddress() + '\n' +
                    "Receiver name: " + order.get().getReceiverName() + '\n' +
                    "Receiver address: " + order.get().getDeliveryAddress() + '\n' +
                    "Receiver phone number: " + order.get().getReceiverPhoneNumber() + '\n' +
                    "Item type: " + order.get().getItemType() + '\n' +
                    "Item quantity: " + order.get().getItemQuantity() + '\n' +
                    "Tracking number: " + order.get().getBikeNumber() + '\n' +
                    "Rider name: " + order.get().getRiderName() + '\n' +
                    "Rider Phone Number: " + order.get().getReceiverPhoneNumber() + '\n' +
                    "Price: " + order.get().getPrice() + '\n' +
                    "Current Date: " + currentDateTime);
        }
        orderRepository.save(order.get());
        document.add(p1);
        document.add(p2);
        document.close();

        String content = "<h3>Hello," + customer.get().getFirstName() + "</h3>" + '\n' +
                "<p> Attached is the details of your order, with the dispatch rider's information.</p>";
        MailDto mailDto = MailDto.builder()
                .to(email)
                .subject("Orders Details")
                .message(content)
                .build();
        emailService.sendAttachment(mailDto);
    }
    catch (DocumentException | MessagingException e) {
        e.printStackTrace();
    }
    return ResponseEntity.ok("Dispatched!");
}

    @Override
    public ResponseEntity<ApiResponse> changeOrderStatus(String referenceNumber) {
        appUtil.getLoggedInStaff();
        Optional<Orders> order = Optional.ofNullable(orderRepository.findByReferenceNumber(referenceNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found!")));
        if(order.get().getOrderStatus().equals(OrderStatus.COMPLETED)){
            throw new UnsupportedOperationException("This order has been closed by the rider");}
        else if(order.get().getOrderStatus().equals(OrderStatus.PENDING)) {
            throw new UnsupportedOperationException("You cannot change the status of a PENDING order. Dispatch this order now.");}
        else if(order.get().getOrderStatus().equals(OrderStatus.CANCELLED)) {
            throw new UnsupportedOperationException("This is a cancelled order!");}
        else {order.get().setOrderStatus(OrderStatus.COMPLETED);}
        orderRepository.save(order.get());
        Optional<Staff> rider = staffRepository.findByStaffId(order.get().getRiderId());
        if(rider.get().getRiderStatus().equals(RiderStatus.TwiceEngaged)){
            rider.get().setRiderStatus(RiderStatus.Engaged);
        }else{rider.get().setRiderStatus(RiderStatus.Free);}
        return ResponseEntity.ok(new ApiResponse<>("Success", "Order closed.", null));}


    @Override
    public ResponseEntity<ApiResponse> registerABike(RegisterBikeDto registerBikeDto) {
        Staff user = appUtil.getLoggedInStaff();
        if (!(user.getRole().equals(Role.ADMIN))) {
            throw new ValidationException("You are not authorised to perform this operation.");}
          Optional<Bike> bike = bikeRepository.findByBikeNumber(registerBikeDto.getBikeNumber());
            if(bike.isPresent())
                throw new ResourceAlreadyExistsException("This bike is already registered!");
        Bike bike1 = new Bike();
        bike1.setBikeNumber(registerBikeDto.getBikeNumber());
        bike1.setPrice(registerBikeDto.getPrice());
        bike1.setMake(registerBikeDto.getMake());
        bikeRepository.save(bike1);
        return ResponseEntity.ok(new ApiResponse<>("Success", "Bike registered", null));}


    @Override
    public ResponseEntity<ApiResponse> registerARider(RegisterRiderDto registerRiderDto) {
        Staff staff = appUtil.getLoggedInStaff();
        if (!(staff.getRole().equals(Role.ADMIN))) {
            throw new ValidationException("You are not authorised to perform this operation.");}
        Long staffNumber = appUtil.generateRandomCode();
        Staff staff1 = new Staff();
        staff1.setRole(Role.RIDER);
        staff1.setFirstName(registerRiderDto.getFirstName());
        staff1.setLastName(registerRiderDto.getLastName());
        staff1.setAddress(registerRiderDto.getAddress());
        staff1.setStaffId(staffNumber);
        staff1.setAccountNumber(registerRiderDto.getAccountNumber());
        staff1.setBankName(registerRiderDto.getBankName());
        staff1.setPhoneNumber(registerRiderDto.getPhoneNumber());
        staff1.setRiderStatus(RiderStatus.Free);
        staff1.setRidesCount(0);
        staff1.setActive(true);
        staffRepository.save(staff1);
        return ResponseEntity.ok(new ApiResponse<>("Success", "Rider registration successful", "Your Rider ID is: " + staffNumber));}


    @Override
    public ApiResponse assignBikeToRider(AssignRiderToBikeDto assignRiderToBikeDto) {
        Staff user = appUtil.getLoggedInStaff();
        if (!(user.getRole().equals(Role.ADMIN))) {
            throw new ValidationException("You are not authorised to perform this operation");}
        Staff user1 = staffRepository.findByStaffId(assignRiderToBikeDto.getStaffId())
                .orElseThrow(() -> new UserNotFoundException("This user does not exist"));
        if (!user1.getRole().equals(Role.RIDER))
            throw new UnsupportedOperationException("You cannot perform this operation on this user");
        Optional<Bike> bike = bikeRepository.findByBikeNumber(assignRiderToBikeDto.getBikeNumber());
        if (bike.isEmpty())
            throw new ResourceNotFoundException("There is no bike with this number!");

        bike.get().setRiderName(user1.getFirstName() + " " + user1.getLastName());
        bike.get().setStaffId(user1.getStaffId());
        bike.get().setRiderPhoneNumber(user1.getPhoneNumber());
        bikeRepository.save(bike.get());

        user1.setBikeNumber(assignRiderToBikeDto.getBikeNumber());
        staffRepository.save(user1);

        return new ApiResponse<>("Success", "Successful", "Bike with the number "
                + assignRiderToBikeDto.getBikeNumber() + " has been assigned to " + bike.get().getRiderName());}


    @Override
    public Optional<OrderDetailsResponse> viewAnOrderByReferenceNumber(String referenceNumber) {
        Staff user = appUtil.getLoggedInStaff();
        if (!(user.getRole().equals(Role.ADMIN) || user.getRole().equals(Role.SUPER_ADMIN)))
            throw new ValidationException("You are not authorised to perform this operation.");
        Optional<Orders> order = Optional.ofNullable(orderRepository.findByReferenceNumber(referenceNumber)
                .orElseThrow(() -> new ResourceNotFoundException("There is no order with this referenceNumber")));

        OrderDetailsResponse response = OrderDetailsResponse.builder()
                .referenceNumber(order.get().getReferenceNumber())
                .thirdPartyName(order.get().getThirdPartyName())
                .price(order.get().getPrice())
                .phoneNumber(order.get().getCustomerPhoneNumber())
                .orderStatus(order.get().getOrderStatus())
                .thirdPartyAddress(order.get().getThirdPartyAddress())
                .itemQuantity(order.get().getItemQuantity())
                .itemType(order.get().getItemType())
                .receiverPhoneNumber(order.get().getReceiverPhoneNumber())
                .receiverName(order.get().getReceiverName())
                .pickUpAddress(order.get().getPickUpAddress())
                .deliveryAddress(order.get().getDeliveryAddress())
                .customerLastName(order.get().getCustomerLastName())
                .customerFirstName(order.get().getCustomerFirstName())
                .createdAt(order.get().getCreatedAt())
                .riderId(order.get().getRiderId())
                .thirdPartyPickUp(order.get().getThirdPartyPickUp())
                .distance(order.get().getDistance())
                .build();
        return Optional.ofNullable(response);
        }


    @Override
    public List<Orders> viewAllOrdersByStatus(OrderStatus orderStatus) {
        Staff user = appUtil.getLoggedInStaff();
        if (!user.getRole().equals(Role.ADMIN) || (!user.getRole().equals(Role.SUPER_ADMIN)))
            throw new ValidationException("You are not authorised to perform this operation.");
        return new ArrayList<>(orderRepository.findByOrderStatus(orderStatus));}


    @Override
    public List<Staff> viewAllRidersByStatus(RiderStatus riderStatus) {
        Staff user = appUtil.getLoggedInStaff();
        if (!user.getRole().equals(Role.ADMIN) || (!user.getRole().equals(Role.SUPER_ADMIN)))
            throw new ValidationException("You are not allowed to perform this operation");
        return new ArrayList<>(staffRepository.findByRiderStatus(riderStatus));}


    @Override
    public Integer countRidesPerRider(Long staffId) {
        Staff user = appUtil.getLoggedInStaff();
        if (!(user.getRole().equals(Role.ADMIN) || (user.getRole().equals(Role.SUPER_ADMIN)))) {
            throw new ValidationException("You are not authorised to perform this operation.");}
        List<Orders> tripCount = new ArrayList<>(orderRepository.findByRiderId(staffId));
        return tripCount.size();}


    @Override
    public Optional<GetAllStaffProfileDto> viewStaffDetails(Long staffId) {
        Staff staff = appUtil.getLoggedInStaff();
        if(!staff.getRole().equals(Role.SUPER_ADMIN) || !staff.getRole().equals(Role.ADMIN))
            throw new ValidationException("You are not authorised to perform this operation!");

        Optional<Staff> user1 = staffRepository.findByStaffId(staffId);
        if(user1.get().getRole().equals(Role.SUPER_ADMIN)) {
            throw new ValidationException("You cannot perform this operation on this user!");}

        GetAllStaffProfileDto staffProfileDto = GetAllStaffProfileDto.builder()
                .id(user1.get().getId())
                .staffId(user1.get().getStaffId())
                .firstName(user1.get().getFirstName())
                .lastName(user1.get().getLastName())
                .email(user1.get().getEmail())
                .createdAt(user1.get().getCreatedAt())
                .dob(user1.get().getDob())
                .stateOfOrigin(user1.get().getStateOfOrigin())
                .role(user1.get().getRole())
                .address(user1.get().getAddress())
                .role(user1.get().getRole())
                .phoneNumber(user1.get().getPhoneNumber())
                .bankName(user1.get().getBankName())
                .accountNumber(user1.get().getAccountNumber())
                .nextOfKinFirstName(user1.get().getNextOfKinFirstName())
                .nextOfKinLastName(user1.get().getNextOfKinLastName())
                .nextOfKinAddress(user1.get().getNextOfKinAddress())
                .nextOfKinPhoneNumber(user1.get().getNextOfKinPhoneNumber())
                .build();
        return Optional.ofNullable(staffProfileDto);
    }


    @Override
    public List<GetAllStaffProfileDto> viewAllStaff() {
        Staff staff = appUtil.getLoggedInStaff();
        if(!staff.getRole().equals(Role.SUPER_ADMIN) || staff.getRole().equals(Role.ADMIN))
            throw new ValidationException("You are not authorised to perform this transaction");
        List<Staff> allStaff = staffRepository.findAll();
        return allStaff.stream()
                .map(this::staffProfileDto)
                .collect(Collectors.toList());}

    private GetAllStaffProfileDto staffProfileDto (Staff staff) {
        return GetAllStaffProfileDto.builder()
                .id(staff.getId())
                .firstName(staff.getFirstName())
                .lastName(staff.getLastName())
                .staffId(staff.getStaffId())
                .address(staff.getAddress())
                .role(staff.getRole())
                .phoneNumber(staff.getPhoneNumber())
                .email(staff.getEmail())
                .nextOfKinFirstName(staff.getNextOfKinFirstName())
                .nextOfKinLastName(staff.getNextOfKinLastName())
                .nextOfKinPhoneNumber(staff.getNextOfKinPhoneNumber())
                .nextOfKinAddress(staff.getNextOfKinAddress())
                .dob(staff.getDob())
                .accountNumber(staff.getAccountNumber())
                .bankName(staff.getBankName())
                .createdAt(staff.getCreatedAt())
                .stateOfOrigin(staff.getStateOfOrigin())
                .averageRating(staff.getAverageRating())
                .build();
    }


    @Override
    public ResponseEntity<ApiResponse> deleteStaff(Long staffId) {
        Staff user = appUtil.getLoggedInStaff();
        if (user.getRole().equals(Role.SUPER_ADMIN))
            throw new ValidationException("You are not authorised to perform this operation!");
        Optional<Staff> user1 = staffRepository.findByStaffId(staffId);
        if (user1.isEmpty()) {throw new ValidationException("Staff number incorrect!");}
        staffRepository.delete(user1.get());
        return ResponseEntity.ok(new ApiResponse<>("Success", "Staff has been deleted!", null));}


    @Override
    public ResponseEntity<ApiResponse> makeAdmin(MakeStaffDto makeStaffDto) {
        Staff user = appUtil.getLoggedInStaff();
        if (!(user.getRole().equals(Role.SUPER_ADMIN))) {
            throw new ValidationException("You are not authorised to perform this operation");
        }
        Optional<Customer> user1 = customerRepository.findByEmail(makeStaffDto.getEmail());
        if (user1.isPresent()) {
            //calling the method to move user from customer to staff table/db
            moveStorageFromCustomerToStaffDb(makeStaffDto.getEmail());
            return ResponseEntity.ok(new ApiResponse<>("Success", "New admin created", null));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>("Failed", "There is no user with this email!", null));
    }

    private void moveStorageFromCustomerToStaffDb(String email){
        Optional<Customer> user1 = customerRepository.findByEmail(email);
        Staff staff = new Staff();
        staff.setEmail(email);
        staff.setRole(Role.ADMIN);
        staff.setFirstName(user1.get().getFirstName());
        staff.setLastName(user1.get().getLastName());
        staff.setPhoneNumber(user1.get().getPhoneNumber());
        staff.setPassword(user1.get().getPassword());
        staff.setGender(user1.get().getGender());
        staff.setAddress(user1.get().getAddress());
        staff.setActive(user1.get().isActive());
        staffRepository.save(staff);
        customerRepository.delete(user1.get());
    }


    @Override
    public List<Orders> viewAllOrders(){
        Staff user = appUtil.getLoggedInStaff();
        if (!user.getRole().equals(Role.SUPER_ADMIN) || user.getRole().equals(Role.ADMIN)) {
            throw new ValidationException("You are not authorised to perform this operation");}
        return orderRepository.findAll();}


    @Override
    public List<Orders> exportOrdersDataToExcel(HttpServletResponse response) throws IOException {
        appUtil.getLoggedInStaff();
        List<Orders> orders = orderRepository.findAll();
        ExcelExportUtils exportUtils = new ExcelExportUtils(orders);
        exportUtils.exportOrdersDataToExcel(response);
        return orders;}


    @Override
    public List<Orders> clientWeeklyOrderSummary(WeeklyOrderSummaryDto weeklyOrderSummaryDto) {
        Staff user = appUtil.getLoggedInStaff();
        if (!(user.getRole().equals(Role.ADMIN) || (user.getRole().equals(Role.STAFF))))
            throw new ValidationException("You are not authorised to perform this operation");
        return new ArrayList<>(orderRepository.findAllByClientCodeAndCreatedAtBetween(weeklyOrderSummaryDto.getClientCode(),
                weeklyOrderSummaryDto.getStartDate(), weeklyOrderSummaryDto.getEndDate()));}


    @Override
    public List<Orders> viewAllOrdersToday() {
    Staff user = appUtil.getLoggedInStaff();
        if (!(user.getRole().equals(Role.SUPER_ADMIN) || (user.getRole().equals(Role.ADMIN)))) {
            throw new ValidationException("You are not authorised to perform this operation");}
        LocalDate today = LocalDate.now();
        return orderRepository.findAllByCreatedAt(today);}


    @Override
    public List<Orders> viewAllOrdersInAMonth(OrdersHistoryDto ordersHistoryDto) {
        Staff user = appUtil.getLoggedInStaff();
        if (!(user.getRole().equals(Role.SUPER_ADMIN) || user.getRole().equals(Role.ADMIN))) {
            throw new ValidationException("You are not authorised to perform this operation");}
        return orderRepository.findAllByCreatedAtBetween(ordersHistoryDto.getStartDate(), ordersHistoryDto.getEndDate());}


    @Override
    public List<Orders> viewAllOrdersInAWeek(OrdersHistoryDto ordersHistoryDto) {
        Staff user = appUtil.getLoggedInStaff();
        if (!(user.getRole().equals(Role.SUPER_ADMIN) || user.getRole().equals(Role.ADMIN))) {
            throw new ValidationException("You are not authorised to perform this operation");}
        return orderRepository.findAllByCreatedAtBetween(ordersHistoryDto.getStartDate(), ordersHistoryDto.getEndDate());}


    @Override
    public int viewDeliveryCountOfRider(Long riderId, RidersDeliveryCountPerMonthDto ridersDeliveryCountPerMonthDto) {
        Staff staff = appUtil.getLoggedInStaff();
        if(!staff.getRole().equals(Role.ADMIN))
            throw new ValidationException("You are not permitted to perform this operation");
        List<Orders> ridesList = new ArrayList<>(orderRepository.findAllByRiderIdAndCreatedAtBetween(riderId,
                ridersDeliveryCountPerMonthDto.getStartDate(), ridersDeliveryCountPerMonthDto.getEndDate()));
        return ridesList.size();}


    @Override
    public BigDecimal weeklyBill(Long clientCode, PeriodicBillDto periodicBillDto) {
        Staff staff = appUtil.getLoggedInStaff();
        if (!staff.getRole().equals(Role.ADMIN))
            throw new ValidationException("You are not permitted to perform this operation");
        return orderRepository.findSumOfOrderPrices(clientCode, periodicBillDto.getStartDate(), periodicBillDto.getEndDate());}


    public List<Orders> generatePeriodicOrderDetailsPdf (Long clientCode, HttpServletResponse response, PeriodicBillDto periodicBillDto) throws IOException, MessagingException {
        appUtil.getLoggedInStaff();
        List<Orders> order = orderRepository.findOrderDetails(clientCode, periodicBillDto.getStartDate(), periodicBillDto.getEndDate());
        BigDecimal totalBill = orderRepository.findSumOfOrderPrices(clientCode, periodicBillDto.getStartDate(), periodicBillDto.getEndDate());
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy  hh:mm:ss");
        String currentDateTime = dateFormat.format(new Date());

        Document document = new Document(PageSize.A4);
        try {
            PdfWriter.getInstance(document, response.getOutputStream());
            document.open();
            Font font = FontFactory.getFont(FontFactory.COURIER_BOLD);
            font.setColor(Color.BLACK);
            font.setSize(20);
            Paragraph p1 = new Paragraph("Total Orders Invoice", font);
            p1.setAlignment(Paragraph.ALIGN_CENTER);

            Paragraph p2 = new Paragraph();
            p2.setAlignment(Paragraph.ALIGN_LEFT);
            p2.setFont(FontFactory.getFont(FontFactory.COURIER_BOLD, 12, Color.black));
            p2.setMultipliedLeading(2);

            Font font2 = FontFactory.getFont(FontFactory.COURIER_BOLD);
            font2.setColor(Color.RED);
            font2.setSize(15);
            Paragraph p3 = new Paragraph("Grand Total: " + totalBill, font2);
            p3.setAlignment(Paragraph.ALIGN_LEFT);

            for (Orders oneOrder : order) {
                p2.add("Client Id: " + oneOrder.getClientCode() + '\n' +
                        "Customer Name: " + oneOrder.getCompanyName() + '\n' +
                        "Order Date: " + oneOrder.getCreatedAt() + '\n' +
                        "ReferenceNumber: " + oneOrder.getReferenceNumber() + '\n' +
                        "Pick-up address: " + oneOrder.getPickUpAddress() + '\n' +
                        "Item: " + oneOrder.getItemType() + '\n' +
                        "Receiver address: " + oneOrder.getDeliveryAddress() + '\n' +
                        "Price: " + oneOrder.getPrice() + '\n' +
                        "Order Status: " + oneOrder.getOrderStatus() + '\n' + " " + '\n');
            }
            document.add(p1);
            document.add(p2);
            document.add(p3);
            document.close();
        }catch (DocumentException e) {
            e.printStackTrace();
        }
        return order;
    }


    @Override
    public Page<Feedback> viewAllFeedbacks(Integer pageNumber, Integer pageSize) {
        return feedbackService.findAll(pageNumber, pageSize);}


    @Override
    public Page<Feedback> viewPeriodicFeedback(Integer pageNumber, Integer pageSize, LocalDate startDate, LocalDate endDate) {
        return feedbackService.viewPeriodicFeedback(pageNumber, pageSize, startDate, endDate);}


    @Override
    public Page<Orders> getOrdersPagination(Integer pageNumber, Integer pageSize, String sortProperty) {
        Pageable pageable;
        pageable = PageRequest.of(pageNumber, pageSize, Sort.Direction.ASC, Objects.requireNonNullElse(sortProperty, "orderStatus"));
        return orderRepository.findAll(pageable);}


    @Override
    public String createDistancePriceList(CreateDistancePriceDto distancePriceDto) {
        Staff staff = appUtil.getLoggedInStaff();
        if(!(staff.getRole().equals(Role.SUPER_ADMIN) || staff.getRole().equals(Role.ADMIN)))
            throw new ValidationException("You are not authorized to perform this action");
        distancePriceService.createDistancePriceList(distancePriceDto);
        return "Price Created Successfully";}

    @Override
    public String changePrice(ChangePriceDto changePriceDto) {
        Staff staff = appUtil.getLoggedInStaff();
        if(!(staff.getRole().equals(Role.SUPER_ADMIN)))
            throw new ValidationException("You are not authorized to perform this action");
        distancePriceService.changePrice(changePriceDto);
        return "Successful";}


    @Override
    public String changeDistance(ChangeDistanceDto changeDistanceDto) {
        Staff staff = appUtil.getLoggedInStaff();
        if(!(staff.getRole().equals(Role.SUPER_ADMIN)))
            throw new ValidationException("You are not authorized to perform this action");
        distancePriceService.changeDistance(changeDistanceDto);
        return "Successful";}


    @Override
    public String deleteDistancePrice(Double distance) {
        Staff staff = appUtil.getLoggedInStaff();
        if(!(staff.getRole().equals(Role.SUPER_ADMIN) || staff.getRole().equals(Role.ADMIN)))
            throw new ValidationException("You are not authorized to perform this action");
        distancePriceService.deleteDistancePrice(distance);
        return "Successful";}

    @Override
    public List<DistancePriceResponseDto> viewDistancePriceList() {
        Staff staff = appUtil.getLoggedInStaff();
        if(!(staff.getRole().equals(Role.SUPER_ADMIN) || staff.getRole().equals(Role.ADMIN)))
            throw new ValidationException("You are not authorized to perform this action");
        return distancePriceService.viewDistancePriceList();
    }


    @Override
    public List<CustomerProfileResponse> viewAllCustomers() {
        return customerService.viewAll();
    }


    @Override
    public String sendOrdersSummary(Long clientCode, PeriodicBillDto periodicBillDto) throws MessagingException, IOException {
        appUtil.getLoggedInStaff();

        Optional<Customer> customer = customerRepository.findByClientCode(clientCode);
        String email = customer.get().getEmail();

        List<Orders> order = orderRepository.findOrderDetails(clientCode, periodicBillDto.getStartDate(), periodicBillDto.getEndDate());
        BigDecimal totalBill = orderRepository.findSumOfOrderPrices(clientCode, periodicBillDto.getStartDate(), periodicBillDto.getEndDate());
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy  hh:mm:ss");
        dateFormat.format(new Date());

        Document document = new Document(PageSize.A4);
        try {
            PdfWriter.getInstance(document, new FileOutputStream("Order_Details.pdf"));
            document.open();

            Font font = FontFactory.getFont(FontFactory.COURIER_BOLD);
            font.setColor(Color.BLACK);
            font.setSize(15);
            Paragraph p1 = new Paragraph("Total Orders Invoice", font);
            p1.setAlignment(Paragraph.ALIGN_CENTER);

            Paragraph p2 = new Paragraph();
            p2.setAlignment(Paragraph.ALIGN_LEFT);
            p2.setFont(FontFactory.getFont(FontFactory.COURIER_BOLD, 12, Color.black));
            p2.setMultipliedLeading(2);

            Font font2 = FontFactory.getFont(FontFactory.COURIER_BOLD);
            font2.setColor(Color.RED);
            font2.setSize(15);
            Paragraph p3 = new Paragraph("Grand Total: " + totalBill, font2);
            p3.setAlignment(Paragraph.ALIGN_CENTER);

            for (Orders oneOrder : order) {
                p2.add("Client Id: " + oneOrder.getClientCode() + '\n' +
                        "Company Name: " + oneOrder.getCompanyName() + '\n' +
                        "Order Date: " + oneOrder.getCreatedAt() + '\n' +
                        "ReferenceNumber: " + oneOrder.getReferenceNumber() + '\n' +
                        "Pick-up address: " + oneOrder.getPickUpAddress() + '\n' +
                        "Item: " + oneOrder.getItemType() + '\n' +
                        "Receiver address: " + oneOrder.getDeliveryAddress() + '\n' +
                        "Price: " + oneOrder.getPrice() + '\n' +
                        "Order Status: " + oneOrder.getOrderStatus() + '\n' + " " + '\n');}
            document.add(p1);
            document.add(p2);
            document.add(p3);
            document.close();

            String content = "<h4>Hello," + customer.get().getFirstName() + "</h4>" + '\n'+
                    "<p> Here is a history of all your delivery requests from " +periodicBillDto.getStartDate() +" "+"to "+periodicBillDto.getEndDate()+"</p>";
            MailDto mailDto = MailDto.builder()
                    .to(email)
                    .subject("Orders Details")
                    .message(content)
                    .build();
            emailService.sendAttachment(mailDto);
        }catch (DocumentException | MessagingException e) {
            e.printStackTrace();
        }
        return "Orders Invoice Mail sent!";
    }

    @Override
    public long automaticStaffCount() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SS");
        Date now = new Date();
        dateFormat.format(now);
        return staffRepository.count();
    }

    @Override
    public ResponseEntity<LoggedInStaffProfileResponseDto> displayStaffInformation() {
        Staff staff = appUtil.getLoggedInStaff();
        LoggedInStaffProfileResponseDto response = LoggedInStaffProfileResponseDto.builder()
                .role(staff.getRole())
                .firstName(staff.getFirstName())
                .lastName(staff.getLastName())
                .email(staff.getEmail())
                .phoneNumber(staff.getPhoneNumber())
                .staffId(staff.getStaffId())
                .role(staff.getRole())
                .build();
        return ResponseEntity.ok(response);
    }

    @Override
    public Optional<CustomerProfileResponse> getCustomer(Long id) {
//        if(!(staff.getRole().equals(Role.SUPER_ADMIN) || !(staff.getRole().equals(Role.ADMIN)))){
//            throw new UnsupportedOperationException ("You are not authorized to perform this operation!");
//        }
        Staff staff = appUtil.getLoggedInStaff();
        if(staff.getRole().equals(Role.SUPER_ADMIN) || staff.getRole().equals(Role.ADMIN)) {

            Optional<Customer> customer = customerRepository.findById(id);
            if(customer.isEmpty()) {
            throw new UserNotFoundException("User does not exist");}

            CustomerProfileResponse profileResponse = CustomerProfileResponse.builder()
                    .customerType(customer.get().getCustomerType())
                    .isActive(customer.get().isActive())
                    .address(customer.get().getAddress())
                    .email(customer.get().getEmail())
                    .discount(customer.get().getDiscount())
                    .firstName(customer.get().getFirstName())
                    .lastName(customer.get().getLastName())
                    .clientCode(customer.get().getClientCode())
                    .gender(customer.get().getGender())
                    .orderCount(customer.get().getOrderCount())
                    .id(customer.get().getId())
                    .phoneNumber(customer.get().getPhoneNumber())
                    .role(customer.get().getRole())
                    .build();
            return Optional.ofNullable(profileResponse);
        }
    else {
        throw new UnsupportedOperationException("You are not authorized to perform this operation!");
    }

}


//    if(staff.getRole().equals(Role.SUPER_ADMIN) || (staff.getRole().equals(Role.ADMIN))){
//        throw new UnsupportedOperationException ("You are not authorized to perform this operation!");
//    }
//    Optional<Customer> customer = customerRepository.findById(id);
//        if(customer.isEmpty()) {
//        throw new UserNotFoundException("User does not exist");}
//
//    CustomerProfileResponse profileResponse = CustomerProfileResponse.builder()
//            .customerType(customer.get().getCustomerType())
//            .isActive(customer.get().isActive())
//            .address(customer.get().getAddress())
//            .email(customer.get().getEmail())
//            .discount(customer.get().getDiscount())
//            .firstName(customer.get().getFirstName())
//            .lastName(customer.get().getLastName())
//            .clientCode(customer.get().getClientCode())
//            .gender(customer.get().getGender())
//            .orderCount(customer.get().getOrderCount())
//            .id(customer.get().getId())
//            .phoneNumber(customer.get().getPhoneNumber())
//            .role(customer.get().getRole())
//            .build();
//        return Optional.ofNullable(profileResponse);
//}
//
    @Override
    public Optional<CustomerProfileResponse> getCustomer(String clientCode) {
        Staff staff = appUtil.getLoggedInStaff();
        if(!(staff.getRole().equals(Role.ADMIN) || !(staff.getRole().equals(Role.SUPER_ADMIN)))){
            throw new UnsupportedOperationException ("You are not authorized to perform this operation!");
        }
        Optional<Customer> customer = customerRepository.findByClientCode(Long.valueOf(clientCode));
        if(customer.isEmpty()) {
            throw new UserNotFoundException("User does not exist");}

        CustomerProfileResponse profileResponse = CustomerProfileResponse.builder()
                .customerType(customer.get().getCustomerType())
                .isActive(customer.get().isActive())
                .address(customer.get().getAddress())
                .email(customer.get().getEmail())
                .discount(customer.get().getDiscount())
                .firstName(customer.get().getFirstName())
                .lastName(customer.get().getLastName())
                .clientCode(customer.get().getClientCode())
                .gender(customer.get().getGender())
                .orderCount(customer.get().getOrderCount())
                .id(customer.get().getId())
                .phoneNumber(customer.get().getPhoneNumber())
                .role(customer.get().getRole())
                .build();
        return Optional.ofNullable(profileResponse);
    }


    @Override
    public Optional<Bike> getBike(String bikeNumber) {
        Staff staff = appUtil.getLoggedInStaff();
        if(!staff.getRole().equals(Role.ADMIN) || staff.getRole().equals(Role.SUPER_ADMIN)){
            throw new UnsupportedOperationException ("You are not authorized to perform this operation!");
        }
        Optional<Bike> bike = bikeRepository.findByBikeNumber(bikeNumber);
        if(bike.isEmpty()){
            throw new NotFoundException("Bike not found!");
        }
        return bike;
    }

    @Override
    public Optional<GetRiderProfileResponse> viewRider(Long staffId) {
        Staff staff = appUtil.getLoggedInStaff();
//        if(!(staff.getRole().equals(Role.SUPER_ADMIN)) || !(staff.getRole().equals(Role.ADMIN))){
//            throw new UnsupportedOperationException ("You are not authorized to perform this operation!");
//        }
        if(staff.getRole().equals(Role.SUPER_ADMIN) || staff.getRole().equals(Role.ADMIN)) {

            Optional<Staff> bikeMan = staffRepository.findByStaffId(staffId);
            if(bikeMan.isEmpty()){
                throw new NotFoundException("There is no rider with this ID");}
            if(bikeMan.get().getRole() != Role.RIDER){
                throw new UnsupportedOperationException("This ID is not a rider's ID.");
            }
            GetRiderProfileResponse riderProfileResponse = GetRiderProfileResponse.builder()
                    .staffId(bikeMan.get().getStaffId())
                    .createdAt(bikeMan.get().getCreatedAt())
                    .firstName(bikeMan.get().getFirstName())
                    .lastName(bikeMan.get().getLastName())
                    .address(bikeMan.get().getAddress())
                    .averageRating(bikeMan.get().getAverageRating())
                    .role(bikeMan.get().getRole())
                    .riderStatus(bikeMan.get().getRiderStatus())
                    .ridesCount(bikeMan.get().getRidesCount())
                    .phoneNumber(bikeMan.get().getPhoneNumber())
                    .id(bikeMan.get().getId())
                    .bikeNumber(bikeMan.get().getBikeNumber())
                    .bankName(bikeMan.get().getBankName())
                    .accountNumber(bikeMan.get().getAccountNumber())
                    .build();
            return Optional.ofNullable(riderProfileResponse);
    }
    else {
            throw new UnsupportedOperationException ("You are not authorized to perform this operation!");
        }
    }

    @Override
    public Optional<AllOrdersDetailsResponseDto> viewAnOrder(Long id) {
        Staff staff = appUtil.getLoggedInStaff();
        if(staff.getRole().equals(Role.ADMIN) || staff.getRole().equals(Role.SUPER_ADMIN)) {

        Optional<Orders> anOrder = orderRepository.findById(id);
        return Optional.ofNullable(orderDto(anOrder.get()));
    }
    else {
            throw new UnsupportedOperationException ("You are not authorized to perform this operation!");
        }
    }

    @Override
    public List<Bike> getAllBikes() {
        appUtil.getLoggedInStaff();
        return bikeRepository.findAll();
    }

    @Override
    public Optional<Bike> getABike(Long id) {
        appUtil.getLoggedInStaff();
        return bikeRepository.findById(id);
    }

    @Override
    public List<AllOrdersDetailsResponseDto> getAllOrders() {
        appUtil.getLoggedInStaff();
        List<Orders> allOrders = orderRepository.findAll();
        return allOrders.stream()
                .map(this::orderDto)
                .collect(Collectors.toList());
    }

    private AllOrdersDetailsResponseDto orderDto (Orders orders) {
        return AllOrdersDetailsResponseDto.builder()
                .id(orders.getId())
                .price(orders.getPrice())
                .itemQuantity(orders.getItemQuantity())
                .receiverPhoneNumber(orders.getReceiverPhoneNumber())
                .deliveryAddress(orders.getDeliveryAddress())
                .receiverName(orders.getReceiverName())
                .thirdPartyPickUp(orders.getThirdPartyPickUp())
                .itemType(orders.getItemType())
                .referenceNumber(orders.getReferenceNumber())
                .companyName(orders.getCompanyName())
                .orderStatus(orders.getOrderStatus())
                .pickUpAddress(orders.getPickUpAddress())
                .thirdPartyPhoneNumber(orders.getThirdPartyPhoneNumber())
                .reasonForOrderCancellation(orders.getReasonForOrderCancellation())
                .dispatchAdminNumber(orders.getDispatchAdminNumber())
                .customerFirstName(orders.getCustomerFirstName())
                .customerLastName(orders.getCustomerLastName())
                .clientCode(orders.getClientCode())
                .email(orders.getEmail())
                .riderName(orders.getRiderName())
                .bikeNumber(orders.getBikeNumber())
                .paymentType(orders.getPaymentType())
                .distance(orders.getDistance())
                .riderPhoneNumber(orders.getRiderPhoneNumber())
                .build();
    }


        @Override
    public Optional<GetRiderProfileResponse> viewRiderById(Long id) {
        Staff staff = appUtil.getLoggedInStaff();
        if(staff.getRole().equals(Role.ADMIN) || staff.getRole().equals(Role.SUPER_ADMIN)) {

        Optional<Staff> staff1 = staffRepository.findById(id);
        if(staff1.isEmpty()){
            throw new UserNotFoundException("Not found.");
        }
        else if(staff1.get().getRole() != Role.RIDER) {
            throw new UnsupportedOperationException("This user is not a Rider.");
        }
        GetRiderProfileResponse profileResponse = GetRiderProfileResponse.builder()
                .phoneNumber(staff1.get().getPhoneNumber())
                .id(staff1.get().getId())
                .firstName(staff1.get().getFirstName())
                .lastName(staff1.get().getLastName())
                .createdAt(staff1.get().getCreatedAt())
                .address(staff1.get().getAddress())
                .ridesCount(staff1.get().getRidesCount())
                .riderStatus(staff1.get().getRiderStatus())
                .averageRating(staff1.get().getAverageRating())
                .staffId(staff1.get().getStaffId())
                .role(staff1.get().getRole())
                .accountNumber(staff1.get().getAccountNumber())
                .bankName(staff1.get().getBankName())
                .bikeNumber(staff1.get().getBikeNumber())
                .build();
        return Optional.ofNullable(profileResponse);
    }
    else {
            throw new UnsupportedOperationException ("You are not authorized to perform this operation!");
        }
    }

    @Override
    public List<GetRiderProfileResponse> getAllRiders() {
        appUtil.getLoggedInStaff();
        List<Staff> ridersList = staffRepository.findByRole(Role.RIDER);
                return ridersList.stream()
                        .map(this::riderDto)
                        .collect(Collectors.toList());
            }

    @Override
    public long customersCount() {
        return customerRepository.countByRole(Role.CUSTOMER);
    }

    @Override
    public long adminCount() {
        return staffRepository.countByRole(Role.ADMIN);
    }

    @Override
    public long ridersCount() {
        return staffRepository.countByRole(Role.RIDER);
    }

    @Override
    public Optional<GetAllStaffProfileDto> viewAStaffDetails(Long id) {
        Staff staff = appUtil.getLoggedInStaff();
        if(staff.getRole().equals(Role.SUPER_ADMIN)) {
            Optional<Staff> user1 = staffRepository.findById(id);
            if(user1.get().getRole().equals(Role.SUPER_ADMIN)) {
                throw new ValidationException("You cannot perform this operation on this user!");}

            GetAllStaffProfileDto staffProfileDto = GetAllStaffProfileDto.builder()
                    .id(user1.get().getId())
                    .staffId(user1.get().getStaffId())
                    .firstName(user1.get().getFirstName())
                    .lastName(user1.get().getLastName())
                    .email(user1.get().getEmail())
                    .createdAt(user1.get().getCreatedAt())
                    .dob(user1.get().getDob())
                    .stateOfOrigin(user1.get().getStateOfOrigin())
                    .role(user1.get().getRole())
                    .address(user1.get().getAddress())
                    .role(user1.get().getRole())
                    .phoneNumber(user1.get().getPhoneNumber())
                    .bankName(user1.get().getBankName())
                    .accountNumber(user1.get().getAccountNumber())
                    .nextOfKinFirstName(user1.get().getNextOfKinFirstName())
                    .nextOfKinLastName(user1.get().getNextOfKinLastName())
                    .nextOfKinAddress(user1.get().getNextOfKinAddress())
                    .nextOfKinPhoneNumber(user1.get().getNextOfKinPhoneNumber())
                    .build();
            return Optional.ofNullable(staffProfileDto);
    }
    else {
            throw new ValidationException("You are not authorised to perform this operation!");
        }
    }


    private GetRiderProfileResponse riderDto (Staff staff) {
        return GetRiderProfileResponse.builder()
                .id(staff.getId())
                .staffId(staff.getStaffId())
                .address(staff.getAddress())
                .createdAt(staff.getCreatedAt())
                .firstName(staff.getFirstName())
                .lastName(staff.getLastName())
                .riderStatus(staff.getRiderStatus())
                .phoneNumber(staff.getPhoneNumber())
                .ridesCount(staff.getRidesCount())
                .averageRating(staff.getAverageRating())
                .bikeNumber(staff.getBikeNumber())
                .bankName(staff.getBankName())
                .accountNumber(staff.getAccountNumber())
                .role(staff.getRole())
                .build();
    }


    @Override
    public List<DistancePriceResponse> distanceList() {
        Staff staff = appUtil.getLoggedInStaff();
        if(!(staff.getRole().equals(Role.SUPER_ADMIN)) || (staff.getRole().equals(Role.ADMIN)))
            throw new ValidationException("You are not authorised to perform this operation!");
        return distancePriceService.viewDPrice()
                .stream()
                .map(this::dpResponseDto)
                .collect(Collectors.toList());
    }
    private DistancePriceResponse dpResponseDto (DPrice dPrice){
        return DistancePriceResponse.builder()
                .distanceRange(dPrice.getDistanceRange())
                .price(dPrice.getPrice())
                .build();
    }

    @Override
    public DPrice createDPMap(DistanceMapDto distanceMapDto) {
        Staff staff = appUtil.getLoggedInStaff();
        if(!(staff.getRole().equals(Role.SUPER_ADMIN)) || (staff.getRole().equals(Role.ADMIN)))
            throw new ValidationException("You are not authorised to perform this operation!");
        return (DPrice) distancePriceService.dpMap(distanceMapDto);
    }

}
