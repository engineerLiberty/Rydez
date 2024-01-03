package com.example.demo.service;

import com.example.demo.dto.request.ChangeDistanceDto;
import com.example.demo.dto.request.ChangePriceDto;
import com.example.demo.dto.request.CreateDistancePriceDto;
import com.example.demo.dto.request.DistanceMapDto;
import com.example.demo.dto.response.DistancePriceResponse;
import com.example.demo.dto.response.DistancePriceResponseDto;
import com.example.demo.model.DPrice;
import com.example.demo.model.DistancePrice;

import java.util.List;
import java.util.Map;

public interface DistancePriceService {

    void createDistancePriceList(CreateDistancePriceDto distancePriceDto);

    Double getPriceForDistance(Double distance);

    void deleteDistancePrice(Double distance);

    void changePrice(ChangePriceDto changePriceDto);

    void changeDistance(ChangeDistanceDto changeDistanceDto);

    List<DistancePriceResponseDto> viewDistancePriceList();

    List<DPrice> viewDPrice();

    Map<String, Double> dpMap(DistanceMapDto distanceMapDto);

    List<DPrice> createDPMap(DistanceMapDto distanceMapDto);

    void saveMap(Map<String, Double> dataMap);

}
