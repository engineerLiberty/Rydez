package com.example.demo.serviceImplementation;

import com.example.demo.dto.request.ChangeDistanceDto;
import com.example.demo.dto.request.ChangePriceDto;
import com.example.demo.dto.request.CreateDistancePriceDto;
import com.example.demo.dto.request.DistanceMapDto;
import com.example.demo.dto.response.DistancePriceResponseDto;
import com.example.demo.model.DPrice;
import com.example.demo.model.DistancePrice;
import com.example.demo.repository.DPriceRepository;
import com.example.demo.repository.DistancePriceRepository;
import com.example.demo.service.DistancePriceService;
import com.example.demo.utils.AppUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DistancePriceServiceImplementation implements DistancePriceService {
    private final AppUtil appUtil;
    private final DistancePriceRepository distancePriceRepository;

    private final DPriceRepository dPriceRepository;


    @Override
    public void createDistancePriceList(CreateDistancePriceDto  distancePriceDto){
        appUtil.getLoggedInStaff();
        DistancePrice distancePrice = new DistancePrice();
        distancePrice.setDistanceStart(distancePriceDto.getDistanceStart());
        distancePrice.setDistanceEnd(distancePriceDto.getDistanceEnd());
        distancePrice.setPrice(distancePriceDto.getPrice());
        distancePriceRepository.save(distancePrice);}


    @Override
    public Double getPriceForDistance(Double distance){
        DistancePrice distancePrice = distancePriceRepository.findPriceByDistanceRange(distance);
        if(distancePrice != null) {
            return distancePrice.getPrice();}
        return 10000.00;}


    @Override
    public void changePrice(ChangePriceDto changePriceDto){
        DistancePrice distancePrice = distancePriceRepository.findPriceByDistanceRange(changePriceDto.getDistance());
        if(distancePrice != null){
            distancePrice.setPrice(changePriceDto.getNewPrice());
            distancePriceRepository.save(distancePrice);}}


    @Override
    public void changeDistance(ChangeDistanceDto changeDistanceDto){
        DistancePrice distancePrice = distancePriceRepository.findByPrice(changeDistanceDto.getPrice());
        if (distancePrice != null) {
            distancePrice.setDistanceStart(changeDistanceDto.getNewDistanceStart());
            distancePrice.setDistanceEnd(changeDistanceDto.getNewDistanceEnd());
            distancePriceRepository.save(distancePrice);}}

    @Override
    public List<DistancePriceResponseDto> viewDistancePriceList() {
        return distancePriceRepository.findAll()
                .stream()
                .map(this::responseDto)
                .collect(Collectors.toList());
    }

    private DistancePriceResponseDto responseDto (DistancePrice dPrice){
        return DistancePriceResponseDto.builder()
                .distanceStart(dPrice.getDistanceStart())
                .distanceEnd(dPrice.getDistanceEnd())
                .price(dPrice.getPrice())
                .build();
    }

    @Override
    public List<DPrice> viewDPrice() {
        return dPriceRepository.findAll();
    }


    @Override
    public void deleteDistancePrice(Double distance){
        DistancePrice distancePrice = distancePriceRepository.findPriceByDistanceRange(distance);
        if(distancePrice != null){distancePriceRepository.delete(distancePrice);}}

    @Override
    public Map<String, Double> dpMap(DistanceMapDto distanceMapDto) {
        Map<String, Double> priceList = new HashMap<>();
        priceList.put(distanceMapDto.getDistanceRange(), distanceMapDto.getPrice());
        saveMap(priceList);
        return priceList;
    }

    @Override
    public List<DPrice> createDPMap(DistanceMapDto distanceMapDto) {
        return null;
    }

    @Override
    public void saveMap(Map<String, Double> dataMap) {
        dataMap.forEach((key, value) -> {
            DPrice keyValueEntity = new DPrice();
            keyValueEntity.setDistanceRange(key);
            keyValueEntity.setPrice(value);
            dPriceRepository.save(keyValueEntity);
        });
    }
}
