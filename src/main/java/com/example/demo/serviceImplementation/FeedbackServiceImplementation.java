package com.example.demo.serviceImplementation;

import com.example.demo.dto.request.FeedbackDto;
import com.example.demo.dto.request.RiderFeedbackDto;
import com.example.demo.enums.RiderRating;
import com.example.demo.exceptions.ResourceNotFoundException;
import com.example.demo.model.Customer;
import com.example.demo.model.Feedback;
import com.example.demo.model.Orders;
import com.example.demo.model.Staff;
import com.example.demo.repository.FeedbackRepository;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.StaffRepository;
import com.example.demo.service.FeedbackService;
import com.example.demo.utils.AppUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class FeedbackServiceImplementation implements FeedbackService {

    private final AppUtil appUtil;
    private final OrderRepository orderRepository;
    private final StaffRepository staffRepository;
    private final FeedbackRepository feedbackRepository;

    @Override
    public void provideFeedback(FeedbackDto feedbackDto) {
        Customer customer = appUtil.getLoggedInCustomer();
        String email = customer.getEmail();
        String name = customer.getFirstName();

        Feedback feedback = new Feedback();
        feedback.setEmail(email);
        feedback.setFirstName(name);
        feedback.setMessage(feedbackDto.getMessage());
        feedbackRepository.save(feedback);}


    @Override
    public Page<Feedback> findAll(Integer pageNumber, Integer pageSize) {
        Pageable pageable;
        pageable = PageRequest.of(pageNumber, pageSize);
        return feedbackRepository.findAll(pageable);}


    @Override
    public Page<Feedback> viewPeriodicFeedback(Integer pageNumber, Integer pageSize, LocalDate startDate, LocalDate endDate) {
        Pageable pageable;
        pageable = PageRequest.of(pageNumber, pageSize);
        return feedbackRepository.findByCreatedAtBetween(pageable, startDate, endDate);}


    @Override
    public void rateARider(String referenceNumber, RiderFeedbackDto riderFeedbackDto) {
        Customer customer = appUtil.getLoggedInCustomer();
        Long clientCode = customer.getClientCode();
        Optional<Orders> order = Optional.ofNullable(orderRepository.findByClientCodeAndReferenceNumber(clientCode, referenceNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order with Id " + referenceNumber + " was not found")));
        Optional<Staff> rider = staffRepository.findByStaffId(order.get().getRiderId());

        rider.get().setRatingsCount(rider.get().getRatingsCount()+1);

        if(riderFeedbackDto.getRiderRating().equals(RiderRating.Excellent)){
            rider.get().setRatingsSum(rider.get().getRatingsSum()+5);
        }else if(riderFeedbackDto.getRiderRating().equals(RiderRating.Good)){
            rider.get().setRatingsSum(rider.get().getRatingsSum()+4);
        }else if(riderFeedbackDto.getRiderRating().equals(RiderRating.Average)){
            rider.get().setRatingsSum(rider.get().getRatingsSum()+3);
        }else if(riderFeedbackDto.getRiderRating().equals(RiderRating.Poor)){
            rider.get().setRatingsSum(rider.get().getRatingsSum()+2);
        }else {
            rider.get().setRatingsSum(rider.get().getRatingsSum()+1);
        }
        rider.get().setAverageRating((float) rider.get().getRatingsSum() /rider.get().getRatingsCount());
        staffRepository.save(rider.get());

        Feedback feedback = new Feedback();
        feedback.setRiderRating(riderFeedbackDto.getRiderRating());
        feedback.setRiderReport(riderFeedbackDto.getRiderReport());
        feedback.setStaffId(rider.get().getStaffId());
        feedbackRepository.save(feedback);}

}
