package org.programmers.signalbuddyfinal.domain.trafficSignal.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.programmers.signalbuddyfinal.domain.crossroad.exception.CrossroadErrorCode;
import org.programmers.signalbuddyfinal.domain.trafficSignal.dto.TrafficResponse;
import org.programmers.signalbuddyfinal.domain.trafficSignal.entity.TrafficSignal;
import org.programmers.signalbuddyfinal.domain.trafficSignal.repository.CustomTrafficRepositoryImpl;
import org.programmers.signalbuddyfinal.domain.trafficSignal.repository.TrafficRedisRepository;
import org.programmers.signalbuddyfinal.global.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TrafficService {

    private final CustomTrafficRepositoryImpl customTrafficRepository;
    private final TrafficRedisRepository trafficRedisRepository;

    public void saveAroundTraffic(Double lat, Double lng){
        List<TrafficResponse> aroundTraffics = new ArrayList<>();

        aroundTraffics.addAll(customTrafficRepository.findAroundTraffics(lat, lng));

        for(TrafficResponse response : aroundTraffics){
            if (response.getSerialNumber() != null) {
                trafficRedisRepository.save(new TrafficSignal(response));
            }
        }
    }

    public TrafficResponse trafficFindById(Object id) {

        TrafficResponse response = new TrafficResponse(trafficRedisRepository.findById(id));

        if(response.getSerialNumber() == null){
            throw new BusinessException(CrossroadErrorCode.NOT_FOUND_CROSSROAD);
        }

        return response;

    }
}
