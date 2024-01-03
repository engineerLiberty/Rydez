package com.example.demo.serviceImplementation;

import com.example.demo.configuration.EmailService;
import com.example.demo.configuration.security.CustomUserDetailService;
import com.example.demo.configuration.security.JwtUtils;
import com.example.demo.dto.request.CancelABookingDto;
import com.example.demo.dto.request.ChangePasswordDto;
import com.example.demo.dto.request.CompleteBusinessRegistrationDto;
import com.example.demo.dto.request.CompleteRegistrationDto;
import com.example.demo.dto.request.DirectDeliveryDto;
import com.example.demo.dto.request.FeedbackDto;
import com.example.demo.dto.request.ForgotPasswordDto;
import com.example.demo.dto.request.LoginDto;
import com.example.demo.dto.request.MailDto;
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
import com.example.demo.enums.CustomerType;
import com.example.demo.enums.Gender;
import com.example.demo.enums.OrderStatus;
import com.example.demo.enums.RiderStatus;
import com.example.demo.enums.Role;
import com.example.demo.exceptions.ResourceNotFoundException;
import com.example.demo.exceptions.UserNotFoundException;
import com.example.demo.exceptions.ValidationException;
import com.example.demo.model.Customer;
import com.example.demo.model.Orders;
import com.example.demo.model.Staff;
import com.example.demo.repository.CustomerRepository;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.StaffRepository;
import com.example.demo.service.CustomerService;
import com.example.demo.service.DistancePriceService;
import com.example.demo.service.FeedbackService;
import com.example.demo.utils.AppUtil;
import lombok.RequiredArgsConstructor;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import org.webjars.NotFoundException;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerServiceImplementation implements CustomerService {

    private final StaffRepository staffRepository;
    private final DistancePriceService distancePriceService;
    private final CustomerRepository customerRepository;
    private final EmailService emailService;
    private final FeedbackService feedbackService;
    private final AppUtil appUtil;
    private final OrderRepository orderRepository;
    private final CustomUserDetailService customUserDetailService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @Value("${map_secret}")
    private String map_secret_key;


    @Override
    public ResponseEntity<ApiResponse> signUp(SignUpDto signUpDto) throws ValidationException {

        if (!appUtil.isValidEmail(signUpDto.getEmail()))
            throw new ValidationException("Email is invalid");

        Boolean isUserExist = customerRepository.existsByEmail(signUpDto.getEmail());
        if (isUserExist)
            throw new ValidationException("User Already Exists!");

        Boolean alreadyExists = staffRepository.existsByEmail(signUpDto.getEmail());
        if(alreadyExists)
            throw new ValidationException("User Already Exists!");

        if((!appUtil.isValidPassword(signUpDto.getPassword())))
            throw new ValidationException("Password MUST be between 8 and 15 characters, and  must contain "+'\n'+
                    "an UPPERCASE, a lowercase, a number, and a special character");

        if(!(signUpDto.getConfirmPassword().equals(signUpDto.getPassword())))
            throw new InputMismatchException("Confirm password and Password do not match!");

        Boolean registerAsACompany = signUpDto.getRegisterAsACompany();

        Customer user = new Customer();

        if(registerAsACompany.equals(false) && signUpDto.getEmail().equals("engrLee2003@gmail.com")
                || signUpDto.getEmail().equals("libertyinobi@gmail.com@gmail.com")
                || signUpDto.getEmail().equals("mrLee@gmail.com")){
            Staff staff = new Staff();
            staff.setRole(Role.SUPER_ADMIN);
            staff.setFirstName(signUpDto.getFirstName());
            staff.setLastName(signUpDto.getLastName());
            staff.setEmail(signUpDto.getEmail());
            staff.setGender(Gender.Male);
            staff.setPassword(passwordEncoder.encode(signUpDto.getPassword()));
            String token = jwtUtils.generateSignUpConfirmationToken(signUpDto.getEmail());
            staff.setConfirmationToken(token);
            staff.setActive(true);
            //The super admin does not need to activate his account
            // following the link. It has been activated here already.
            staffRepository.save(staff);
            return ResponseEntity.ok(new ApiResponse<>("Successful", "Super Admin signup successful.", null));}

        else if(registerAsACompany.equals(false)){
            user.setFirstName(signUpDto.getFirstName());
            user.setLastName(signUpDto.getLastName());
            user.setEmail(signUpDto.getEmail());
            user.setPassword(passwordEncoder.encode(signUpDto.getPassword()));
            user.setRole((Role.CUSTOMER));
            user.setDiscount(false);
            user.setCustomerType(CustomerType.Individual);
            String token = jwtUtils.generateSignUpConfirmationToken(signUpDto.getEmail());
            user.setConfirmationToken(token);
            customerRepository.save(user);
            String URL = "http://127.0.0.1:5174/signup/complete_registration/register_as_individual/?token=" + token;
            String content = "<h4>Hello " + signUpDto.getFirstName() + ",</h4>" + '\n'
                    + "<p> Click <a href=" + URL +">Activate</a> to activate your account</p>";
            MailDto mailDto = MailDto.builder()
                    .to(signUpDto.getEmail())
                    .subject("AriXpress: Verify Your Account")
                    .message(content)
                    .build();
            emailService.sendEmail(mailDto);}
        else{
            user.setFirstName(signUpDto.getFirstName());
            user.setLastName(signUpDto.getLastName());
            user.setEmail(signUpDto.getEmail());
            user.setPassword(passwordEncoder.encode(signUpDto.getPassword()));
            user.setRole((Role.CUSTOMER));
            user.setCustomerType(CustomerType.Corporate);
            String token = jwtUtils.generateSignUpConfirmationToken(signUpDto.getEmail());
            user.setConfirmationToken(token);
            customerRepository.save(user);
            String url = "http://127.0.0.1:5174/signup/complete_registration/register_as_business/?token=" + token;
            String content = "<h4>Hello " + signUpDto.getFirstName() + ",</h4>" + '\n'
                            + "<p> Click <a href=" + url +">Activate</a> to activate your account</p>";
            MailDto mailDto = MailDto.builder()
                    .to(signUpDto.getEmail())
                    .subject("AriXpress: Verify Your Account")
                    .message(content)
                    .build();
            emailService.sendEmail(mailDto);
        }
        return ResponseEntity.ok(new ApiResponse<>("Successful", "SignUp Successful. Check your mail to activate your account", null));}


    @Override
    public ResponseEntity<ApiResponse> completeRegistration(String token, CompleteRegistrationDto completeRegistrationDto) {
        Long clientCode = appUtil.generateRandomCode();
        Optional<Customer> existingUser = customerRepository.findByConfirmationToken(token);
        if (existingUser.isPresent()){
            existingUser.get().setPhoneNumber(completeRegistrationDto.getPhoneNumber());
            existingUser.get().setDob(completeRegistrationDto.getDob());
            existingUser.get().setGender(completeRegistrationDto.getGender());
            existingUser.get().setAddress(completeRegistrationDto.getAddress());
            existingUser.get().setState(completeRegistrationDto.getState());
            existingUser.get().setActive(true);
            existingUser.get().setClientCode(clientCode);
            existingUser.get().setConfirmationToken(null);
            customerRepository.save(existingUser.get());
            return ResponseEntity.ok(new ApiResponse<>("Success", "Account activation successful.", "Your unique customer number is "+clientCode));
            }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>("Failed", "User does not exist. Please sign up", null));
    }

    @Override
    public ResponseEntity<ApiResponse> completeBusinessRegistration(String token, CompleteBusinessRegistrationDto completeBusinessRegistrationDto) {
        Optional<Customer> existingUser = Optional.ofNullable(customerRepository.findByConfirmationToken(token)
                .orElseThrow(() -> new UserNotFoundException("User does not exist!")));
        Long clientCode = appUtil.generateRandomCode();
        existingUser.get().setCompanyName(completeBusinessRegistrationDto.getCompanyName());
        existingUser.get().setAddress(completeBusinessRegistrationDto.getAddress());
        existingUser.get().setPaymentType(completeBusinessRegistrationDto.getPaymentType());
        existingUser.get().setPaymentInterval(completeBusinessRegistrationDto.getPaymentInterval());
        existingUser.get().setPhoneNumber(completeBusinessRegistrationDto.getPhoneNumber());
        existingUser.get().setState(completeBusinessRegistrationDto.getState());
        existingUser.get().setClientCode(clientCode);
        existingUser.get().setActive(true);
        existingUser.get().setConfirmationToken(null);
        customerRepository.save(existingUser.get());
        return ResponseEntity.ok(new ApiResponse("Successful", "Corporate Client Registration Successful.", "You unique code is "+ clientCode ));}


    @Override
    public ResponseEntity<String> login(LoginDto loginDto) {

        Optional<Customer> customer = customerRepository.findByEmail(loginDto.getEmail());
        Optional<Staff> staff = staffRepository.findByEmail(loginDto.getEmail());

        if(customer.isEmpty() && staff.isEmpty())
            throw new UserNotFoundException("User does not exist");

        if (customer.isPresent() && !customer.get().isActive() || staff.isPresent() && !staff.get().isActive())
        throw new ValidationException("User Not Active. Kindly complete your registration.");

        if (customer.isPresent() && !passwordEncoder.matches(loginDto.getPassword(), (customer.get().getPassword())))
            throw new ValidationException("Password is Incorrect!");

        else {if (staff.isPresent() && !passwordEncoder.matches(loginDto.getPassword(), staff.get().getPassword()))
            throw new ValidationException("Password is Incorrect!");}

        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginDto.getEmail(), loginDto.getPassword()));
        UserDetails user = customUserDetailService.loadUserByUsername(loginDto.getEmail());
        if (user != null) {return ResponseEntity.ok(jwtUtils.generateToken(user));}

        else {return ResponseEntity.status(400).body("Some error occurred");}
    }

    @Override
    public ResponseEntity<ApiResponse> forgotPassword(ForgotPasswordDto forgotPasswordDto) {
        Optional<Customer> customer = customerRepository.findByEmail(forgotPasswordDto.getEmail());
        Optional<Staff> staff = staffRepository.findByEmail(forgotPasswordDto.getEmail());

        if (customer.isPresent()) {
            String token = jwtUtils.resetPasswordToken(forgotPasswordDto.getEmail());
            customer.get().setConfirmationToken(token);
            customerRepository.save(customer.get());
            String url = "http:/127.0.0.1:5174/reset-password/?token=" + token;
            String content =
                    "<h4>Hello, " + customer.get().getFirstName() + ",</h4>" + '\n'
                            + "<p>Click <a href=" + url + ">Reset Password</a> to reset your password</p> " + '\n';
            MailDto mailDto = MailDto.builder()
                    .to(forgotPasswordDto.getEmail())
                    .subject("AriXpress: Reset your password")
                    .message(content)
                    .build();
            emailService.sendEmail(mailDto);
            return ResponseEntity.ok(new ApiResponse<>("Sent", "Check your email to reset your password", null));

        } else if (staff.isPresent()) {
            String token = jwtUtils.resetPasswordToken(forgotPasswordDto.getEmail());
            staff.get().setConfirmationToken(token);
            staffRepository.save(staff.get());
            String url = "http:/127.0.0.1:5174/forgotpassword/reset-password/?token=" + token;
            String content =
                    "<h4>Hello, " + staff.get().getFirstName() + ",</h4>" + '\n'
                            + "<p>Click <a href=" + url + ">Reset Password</a> to reset your password</p> " + '\n';
            MailDto mailDto = MailDto.builder()
                    .to(forgotPasswordDto.getEmail())
                    .subject("AriXpress: Reset your password")
                    .message(content)
                    .build();
            emailService.sendEmail(mailDto);
            return ResponseEntity.ok(new ApiResponse<>("Sent", "Check your email to reset your password", null));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>("Not Found", "User does not exist", null));}
    }

    @Override
    public ResponseEntity<ApiResponse> resetPassword(String token, ResetPasswordDto resetPasswordDto) {
        Optional<Customer> customer = customerRepository.findByConfirmationToken(token);
        Optional<Staff> staff = staffRepository.findByConfirmationToken(token);

        if (!appUtil.isValidPassword(resetPasswordDto.getNewPassword()))
            throw new ValidationException("Password MUST be between 6 and 15 characters, and  must contain an UPPERCASE, a lowercase, a number, and a special character");
        if (!resetPasswordDto.getConfirmNewPassword().equals(resetPasswordDto.getNewPassword()))
            throw new InputMismatchException("Passwords do not match!");
        if (customer.isPresent()) {
            customer.get().setPassword(passwordEncoder.encode(resetPasswordDto.getNewPassword()));
            customer.get().setConfirmationToken(null);
            customerRepository.save(customer.get());
            return ResponseEntity.ok(new ApiResponse<>("Success", "Password reset successful.", null));
        } else if (staff.isPresent()) {
            staff.get().setPassword(passwordEncoder.encode(resetPasswordDto.getNewPassword()));
            staff.get().setConfirmationToken(null);
            staffRepository.save(staff.get());
            return ResponseEntity.ok(new ApiResponse<>("Success", "Password reset successful.", null));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>("Failed", "Token is incorrect or User does not exist!", null));}
    }

    @Override
    public ResponseEntity<ApiResponse> changePassword(ChangePasswordDto changePasswordDto) {
        Customer customer = appUtil.getLoggedInCustomer();
        if(!(changePasswordDto.getConfirmNewPassword().equals(changePasswordDto.getNewPassword()))){
            throw new InputMismatchException("Confirm password and Password do not match!");}
        customer.setPassword(passwordEncoder.encode(changePasswordDto.getNewPassword()));
        customerRepository.save(customer);
        return ResponseEntity.ok(new ApiResponse<>("Success", "Password change successful", null));}

    @Override
    public ResponseEntity<ApiResponse> bookADelivery(OrderPriceResponse orderPriceResponse) throws IOException, ParseException, InterruptedException {
        Customer customer = appUtil.getLoggedInCustomer();
        Long id = customer.getClientCode();
        String adminEmail = "arixpressng@gmail.com";

        Optional<Orders> orders = orderRepository.findByClientCodeAndReferenceNumber(id, orderPriceResponse.getReferenceNumber());

        if (orders.isPresent()) {
            String url = UriComponentsBuilder.fromUriString("http://localhost:8080/api/v1/auth/staff/dispatch-order/")
                    .queryParam("referenceNumber", orders.get().getReferenceNumber())
                    .build()
                    .toUriString();
            String content = "<h4>Hello,</h4>" + '\n'
                    + "<p>Click <a href=" + url + ">Dispatch</a> to dispatch a new order</p> " + '\n';

            MailDto mailDto = MailDto.builder()
                    .to(adminEmail)
                    .subject("AriXpress: A new order")
                    .message(content)
                    .build();
            emailService.sendEmail(mailDto);
            return ResponseEntity.ok(new ApiResponse<>("Success", "Delivery booked successfully. Details to be sent to you shortly.", null));}
        else {
            return ResponseEntity.ok(new ApiResponse<>("Failed", "Delivery booking not successful", "Order not found!"));
        }
    }

    @Override
    public ResponseEntity<ApiResponse> cancelABooking(String referenceNumber, CancelABookingDto cancelABookingDto) {
        Customer customer = appUtil.getLoggedInCustomer();
        Long clientCode = customer.getClientCode();
        String adminEmail = "arixpressng@gmail.com";
        Optional<Orders> order = Optional.ofNullable(orderRepository.findByClientCodeAndReferenceNumber(clientCode, referenceNumber)
                .orElseThrow(() -> new ResourceNotFoundException("You have no order with the reference number " + referenceNumber)));
        if(order.get().getOrderStatus().equals(OrderStatus.INPROGRESS)){
            return ResponseEntity.ok(new ApiResponse("Forbidden", "A rider has been dispatched to the address already.", null));}
        if(order.get().getOrderStatus().equals(OrderStatus.COMPLETED)){
            return ResponseEntity.ok(new ApiResponse<>("Failed", "This order has been completed!.", null));}
        order.get().setOrderStatus(OrderStatus.CANCELLED);
        order.get().setReasonForOrderCancellation(cancelABookingDto.getReasonForOrderCancellation());
        orderRepository.save(order.get());

        String url = UriComponentsBuilder.fromUriString("http://localhost:8080/api/v1/auth/staff/view-an-order/")
                .queryParam("referenceNumber", order.get().getReferenceNumber())
                .build()
                .toUriString();
        String content = "<h4>Hello,</h4>" + '\n' +
                         "<p>An order has been cancelled. Click <a href="+ url +">Cancelled Order</a> to see details</p> ";
        MailDto mailDto = MailDto.builder()
                .to(adminEmail)
                .subject("Cancelled Order")
                .message(content)
                .build();
        emailService.sendEmail(mailDto);
        return ResponseEntity.ok(new ApiResponse("Success", "Your booking has been cancelled", null));}


    @Override
    public String confirmDelivery(String referenceNumber, RiderFeedbackDto riderFeedbackDto) {
        Customer customer = appUtil.getLoggedInCustomer();
        Long clientCode = customer.getClientCode();
        Optional<Orders> order = Optional.ofNullable(orderRepository.findByClientCodeAndReferenceNumber(clientCode, referenceNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order with Id " + referenceNumber + " was not found")));
        Optional<Staff> rider = staffRepository.findByStaffId(order.get().getRiderId());
        if(rider.get().getRiderStatus().equals(RiderStatus.TwiceEngaged)){
            rider.get().setRiderStatus(RiderStatus.Engaged);}
        else {rider.get().setRiderStatus(RiderStatus.Free);}

        feedbackService.rateARider(referenceNumber,riderFeedbackDto);
        order.get().setOrderStatus(OrderStatus.COMPLETED);
        orderRepository.save(order.get());

        customer.setOrderCount(customer.getOrderCount()+1);
        customerRepository.save(customer);

        int x = customer.getOrderCount();
        if(customer.getCustomerType().equals(CustomerType.Individual) && x % 5 == 0){
            customer.setDiscount(true);
            customerRepository.save(customer);
        return "Congratulations, " + customer.getFirstName() + ". " + "You have qualified for a price discount on your next order.";}
        return "Thank you for your patronage.";}


    @Override
    public String giveFeedback(FeedbackDto feedbackDto) {
        appUtil.getLoggedInCustomer();
        feedbackService.provideFeedback(feedbackDto);
        return "Thank you so much. We hope to continue to serve you better.";}



    @Override
    public List<Orders> weeklyOrderSummary(WeeklyOrderSummaryDto weeklyOrderSummaryDto) {
        Customer customer = appUtil.getLoggedInCustomer();
        Long clientCode = customer.getClientCode();
        if(!(customer.getRole().equals(Role.CUSTOMER)))
            throw new ValidationException("You are not authorised to perform this action");
        if(!(weeklyOrderSummaryDto.getClientCode().equals(clientCode)))
            throw new ValidationException("Client code is wrong!");
        return new ArrayList<>(orderRepository.findAllByClientCodeAndCreatedAtBetween(weeklyOrderSummaryDto.getClientCode(),
                weeklyOrderSummaryDto.getStartDate(), weeklyOrderSummaryDto.getEndDate()));}


    @Override
    public ResponseEntity<ApiResponse> trackingRequest(TrackingDto trackingDto) {
        Customer customer = appUtil.getLoggedInCustomer();
        String email = customer.getEmail();
        MailDto mailDto = MailDto.builder()
                .to("arixpressng@gmail.com")
                .subject("Tracking Request")
                .message("I wish to track the order with trackingNumber: " + trackingDto.getTrackingNumber()+". " + email + " is my email address")
                .build();
        emailService.sendEmail(mailDto);
        return ResponseEntity.ok(new ApiResponse<>("Success", "Tracking request sent.", null));
    }


    @Override
    public ResponseEntity<ApiResponse> updateCustomerDetails(UpdateCustomerDetailsDto updateCustomerDetailsDto) {
        Customer customer = appUtil.getLoggedInCustomer();
        String email = customer.getEmail();
        customerRepository.findByEmail(email);
        if(customer.isActive())
            customer.setFirstName(updateCustomerDetailsDto.getFirstName());
        customer.setLastName(updateCustomerDetailsDto.getLastName());
        customer.setPhoneNumber(updateCustomerDetailsDto.getPhoneNumber());
        customer.setDob(updateCustomerDetailsDto.getDob());
        customer.setAddress(updateCustomerDetailsDto.getAddress());
        customerRepository.save(customer);
        return ResponseEntity.ok(new ApiResponse("Success", "Your details have been updated!", null));}


    @Override
    public List<CustomerProfileResponse> viewAll() {
        appUtil.getLoggedInStaff();
        List<Customer> customerList = customerRepository.findAll();
        return customerList.stream()
                .map(this::customerDto)
                .collect(Collectors.toList());
    }

    private CustomerProfileResponse customerDto (Customer customer) {
        return CustomerProfileResponse.builder()
                .lastName(customer.getLastName())
                .firstName(customer.getFirstName())
                .gender(customer.getGender())
                .clientCode(customer.getClientCode())
                .phoneNumber(customer.getPhoneNumber())
                .email(customer.getEmail())
                .address(customer.getAddress())
                .isActive(customer.isActive())
                .orderCount(customer.getOrderCount())
                .discount(customer.getDiscount())
                .customerType(customer.getCustomerType())
                .id(customer.getId())
                .role(customer.getRole())
                .build();
    }

    @Override
    public long updateCustomersCount() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SS");
        Date now = new Date();
        dateFormat.format(now);
        return customerRepository.count();
    }

    @Override
    public long countAllOrders() {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SS");
            Date now = new Date();
            dateFormat.format(now);
            return orderRepository.count();
        }

    @Override
    public ResponseEntity<LoggedInUserProfileResponse> displayUserInformation() {
        Customer customer = appUtil.getLoggedInCustomer();
        LoggedInUserProfileResponse response = LoggedInUserProfileResponse.builder()
                .customerType(customer.getCustomerType())
                .address(customer.getAddress())
                .clientCode(customer.getClientCode())
                .orderCount(customer.getOrderCount())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .email(customer.getEmail())
                .phoneNumber(customer.getPhoneNumber())
                .role(Role.CUSTOMER)
                .build();
        return ResponseEntity.ok(response);
    }

    @Override
    public OrderPriceResponse directDeliveryDistanceCalculator(DirectDeliveryDto directDeliveryDto) throws IOException, InterruptedException, ParseException {
        Customer customer = appUtil.getLoggedInCustomer();
        Long cid = customer.getClientCode();
        String email = appUtil.getLoggedInCustomer().getEmail();
        String refNumber = appUtil.generateSerialNumber("AXL-");

            String encodedOrigin = URLEncoder.encode(directDeliveryDto.getPickUpAddress(), StandardCharsets.UTF_8);
            String replacedOriginText = encodedOrigin.replace("+", "%20");

            String encodedDestination = URLEncoder.encode(directDeliveryDto.getDeliveryAddress(), StandardCharsets.UTF_8);
            String replacedDestinationText = encodedDestination.replace("+", "%20");

            String apiUrl = UriComponentsBuilder.fromUriString("https://maps.googleapis.com/maps/api/distancematrix/json")
                    .queryParam("origins", replacedOriginText)
                    .queryParam("destinations", replacedDestinationText)
                    .queryParam("key", map_secret_key)
                    .build()
                    .toUriString();

            var request = HttpRequest.newBuilder().GET().uri(URI.create(apiUrl)).build();
            var client = HttpClient.newBuilder().build();
            var response = client.send(request, HttpResponse.BodyHandlers.ofString()).body();

        String distance;
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject = (JSONObject) jsonParser.parse(response);
        JSONArray jsonArray=(JSONArray) jsonObject.get("rows");
        jsonObject = (JSONObject) jsonArray.get(0);
        jsonArray = (JSONArray) jsonObject.get("elements");
        jsonObject = (JSONObject) jsonArray.get(0);
        JSONObject  je = (JSONObject) jsonObject.get("distance");
        distance = (String) je.get("text");
        String [] arr = distance.split(" ");
        double x = Double.parseDouble(arr[0]);
        double price = distancePriceService.getPriceForDistance(x);

        Orders orders = new Orders();

        if (customer.getCustomerType().equals(CustomerType.Corporate)) {
            if(customer.getCustomerType().equals(CustomerType.Corporate)){
                orders.setEmail(email);
                orders.setClientCode(cid);
                orders.setReferenceNumber(refNumber);
                orders.setPickUpAddress(directDeliveryDto.getPickUpAddress());
                orders.setDeliveryAddress(directDeliveryDto.getDeliveryAddress());
                orders.setThirdPartyPickUp(false);
                orders.setCustomerFirstName(customer.getFirstName());
                orders.setCustomerLastName(customer.getLastName());
                orders.setCustomerPhoneNumber(customer.getPhoneNumber());
                orders.setCustomerType(CustomerType.Corporate);
                orders.setItemType(directDeliveryDto.getItemType());
                orders.setItemQuantity(directDeliveryDto.getItemQuantity());
                orders.setReceiverName(directDeliveryDto.getReceiverName());
                orders.setReceiverPhoneNumber(directDeliveryDto.getReceiverPhoneNumber());
                orders.setPaymentType(directDeliveryDto.getPaymentType());
                orders.setOrderStatus(OrderStatus.PENDING);
                orders.setDistance(x);
                orders.setPrice(price);
                orders.setPrice(price);
                orderRepository.save(orders);}}
        else {
                Orders orders1 = new Orders();
                orders1.setClientCode(cid);
                orders1.setThirdPartyPickUp(false);
                orders1.setReferenceNumber(refNumber);
                orders1.setCustomerFirstName(customer.getFirstName());
                orders1.setCustomerLastName(customer.getLastName());
                orders1.setCustomerPhoneNumber(customer.getPhoneNumber());
                orders1.setCustomerType(CustomerType.Individual);
                orders1.setItemType(directDeliveryDto.getItemType());
                orders1.setItemQuantity(directDeliveryDto.getItemQuantity());
                orders1.setDeliveryAddress(directDeliveryDto.getDeliveryAddress());
                orders1.setPickUpAddress(directDeliveryDto.getPickUpAddress());
                orders1.setReceiverName(directDeliveryDto.getReceiverName());
                orders1.setReceiverPhoneNumber(directDeliveryDto.getReceiverPhoneNumber());
                orders1.setPaymentType(directDeliveryDto.getPaymentType());
                orders1.setOrderStatus(OrderStatus.PENDING);
                orders1.setDistance(x);
                if (customer.getDiscount().equals(true)) {
                    orders1.setPrice(price * .85);
                    customer.setDiscount(false);
                    customerRepository.save(customer);}
                else {
                    orders1.setPrice(price);
                }
                orders1.setEmail(email);
                orderRepository.save(orders1);}
        OrderPriceResponse response1 = new OrderPriceResponse();
        response1.setPrice(price);
        response1.setReferenceNumber(refNumber);
        return response1;
    }

    @Override
    public OrderPriceResponse indirectDeliveryDistanceCalculator(ThirdPartySenderDto thirdPartySenderDto) throws IOException, InterruptedException, ParseException {
        Customer customer = appUtil.getLoggedInCustomer();
        String email = customer.getEmail();
        Long cid = customer.getClientCode();
        String refNumber = appUtil.generateSerialNumber("AXL-");

        String encodedOrigin = URLEncoder.encode(thirdPartySenderDto.getThirdPartyAddress(), StandardCharsets.UTF_8);
        String replacedOriginText = encodedOrigin.replace("+", "%20");

        String encodedDestination = URLEncoder.encode(thirdPartySenderDto.getDeliveryAddress(), StandardCharsets.UTF_8);
        String replacedDestinationText = encodedDestination.replace("+", "%20");

        String apiUrl = UriComponentsBuilder.fromUriString("https://maps.googleapis.com/maps/api/distancematrix/json")
                .queryParam("origins", replacedOriginText)
                .queryParam("destinations", replacedDestinationText)
                .queryParam("key", map_secret_key)
                .build()
                .toUriString();

            var request = HttpRequest.newBuilder().GET().uri(URI.create(apiUrl)).build();
            var client = HttpClient.newBuilder().build();
            var response = client.send(request, HttpResponse.BodyHandlers.ofString()).body();

            String distance;
            JSONParser jsonParser = new JSONParser();
            JSONObject jsonObject = (JSONObject) jsonParser.parse(response);
            JSONArray jsonArray = (JSONArray) jsonObject.get("rows");
            jsonObject = (JSONObject) jsonArray.get(0);
            jsonArray = (JSONArray) jsonObject.get("elements");
            jsonObject = (JSONObject) jsonArray.get(0);
            JSONObject je = (JSONObject) jsonObject.get("distance");
            distance = (String) je.get("text");
            String[] arr = distance.split(" ");
            double x = Double.parseDouble(arr[0]);
            double price = distancePriceService.getPriceForDistance(x);

            Orders order = new Orders();
            order.setThirdPartyPickUp(true);
            order.setClientCode(cid);
            order.setReferenceNumber(refNumber);
            order.setCustomerFirstName(customer.getFirstName());
            order.setCustomerLastName(customer.getLastName());
            order.setCustomerPhoneNumber(customer.getPhoneNumber());
            order.setThirdPartyName(thirdPartySenderDto.getThirdPartyName());
            order.setThirdPartyAddress(thirdPartySenderDto.getThirdPartyAddress());
            order.setThirdPartyPhoneNumber(thirdPartySenderDto.getThirdPartyPhoneNumber());
            order.setCustomerType(CustomerType.Individual);
            order.setOrderStatus(OrderStatus.PENDING);
            order.setItemQuantity(thirdPartySenderDto.getItemQuantity());
            order.setItemType(thirdPartySenderDto.getItemType());
            order.setReceiverName(thirdPartySenderDto.getReceiverName());
            order.setReceiverPhoneNumber(thirdPartySenderDto.getReceiverPhoneNumber());
            order.setDeliveryAddress(thirdPartySenderDto.getDeliveryAddress());
            order.setPaymentType(thirdPartySenderDto.getPaymentType());
            order.setEmail(email);
            order.setPaymentType(thirdPartySenderDto.getPaymentType());
            order.setDistance(x);
            if (customer.getDiscount().equals(true)) {
                order.setPrice(price * 0.85);
                customer.setDiscount(false);
                customerRepository.save(customer);
            } else {
                order.setPrice(price);
            }
            orderRepository.save(order);

            OrderPriceResponse response1 = new OrderPriceResponse();
            response1.setPrice(price);
            response1.setReferenceNumber(refNumber);
            return response1;

    }

    @Override
    public void abort(OrderPriceResponse orderPriceResponse) {
        Optional<Orders> orders = orderRepository.findByReferenceNumber(orderPriceResponse.getReferenceNumber());
        if (orders.isEmpty()) {
            throw new NotFoundException("Order not found or does not exist!");
        }
        orders.get().setOrderStatus(OrderStatus.ABORTED);
        orderRepository.save(orders.get());
        }

    @Override
    public List<OrderDetailsResponse> allMyOrders(Long clientCode) {
        Customer customer = appUtil.getLoggedInCustomer();
        Long cid = customer.getClientCode();
        List<Orders> ordersList =  orderRepository.findAllOrdersByClientCode(cid);
        return ordersList.stream()
                .map(this::orderDetailsResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<Orders> sortedCustomersOrders(Long clientCode) {
        return null;
    }

    @Override
    public List<Orders> sortedCustomersOrdersById(Long clientCode) {
        return null;
    }

    @Override
    public Optional<OrderDetailsResponse> returnById(Long id) {
        Customer customer = appUtil.getLoggedInCustomer();
        Long cid = customer.getId();
        Optional<Orders> optionalOrders = orderRepository.findById(cid);
        return Optional.ofNullable(orderDetailsResponse(optionalOrders.get()));
    }

    private OrderDetailsResponse orderDetailsResponse (Orders orders) {
        return OrderDetailsResponse.builder()
                .orderStatus(orders.getOrderStatus())
                .id(orders.getId())
                .distance(orders.getDistance())
                .pickUpAddress(orders.getPickUpAddress())
                .deliveryAddress(orders.getDeliveryAddress())
                .itemType(orders.getItemType())
                .phoneNumber(orders.getCustomerPhoneNumber())
                .thirdPartyPickUp(orders.getThirdPartyPickUp())
                .thirdPartyPhoneNumber(orders.getThirdPartyPhoneNumber())
                .thirdPartyAddress(orders.getThirdPartyAddress())
                .thirdPartyName(orders.getThirdPartyName())
                .receiverName(orders.getReceiverName())
                .receiverPhoneNumber(orders.getReceiverPhoneNumber())
                .referenceNumber(orders.getReferenceNumber())
                .orderStatus(orders.getOrderStatus())
                .itemQuantity(orders.getItemQuantity())
                .build();
    }

}





