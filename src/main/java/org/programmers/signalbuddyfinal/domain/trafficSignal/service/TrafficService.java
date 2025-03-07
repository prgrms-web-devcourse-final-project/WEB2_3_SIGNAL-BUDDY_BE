package org.programmers.signalbuddyfinal.domain.trafficSignal.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.programmers.signalbuddyfinal.domain.trafficSignal.dto.TrafficResponse;
import org.programmers.signalbuddyfinal.domain.trafficSignal.exception.TrafficErrorCode;
import org.programmers.signalbuddyfinal.domain.trafficSignal.repository.CustomTrafficRepositoryImpl;
import org.programmers.signalbuddyfinal.domain.trafficSignal.repository.TrafficRedisRepository;
import org.programmers.signalbuddyfinal.domain.trafficSignal.repository.TrafficRepository;
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
    private final TrafficRepository trafficRepository;

    public void searchAndSaveTraffic(Double lat, Double lng, Integer radius) {

        List<TrafficResponse> aroundTraffics = new ArrayList<>();

        try{
            aroundTraffics.addAll(customTrafficRepository.findNearestTraffics(lat, lng, radius));

            for(TrafficResponse response : aroundTraffics){
                if (response.getSerialNumber() != null
                    && trafficRedisRepository.findBySerial(String.valueOf(response.getSerialNumber()))!=null) {
                    trafficRedisRepository.save(response);
                }
            }
        } catch (NullPointerException e) {
            log.error("❌ traffic Not Found : {}", e.getMessage(), e);
            throw new BusinessException(TrafficErrorCode.NOT_FOUND_TRAFFIC);
        }
    }

    public TrafficResponse trafficFindById(Long serialNumber) {

        TrafficResponse responseRedis = trafficRedisRepository.findBySerial( String.valueOf(serialNumber) );

        try{

            if (responseRedis == null) {
                TrafficResponse responseDB = new TrafficResponse( trafficRepository.findBySerialNumber(serialNumber) );

                trafficRedisRepository.save(responseDB);
                responseRedis = trafficRedisRepository.findBySerial( String.valueOf(serialNumber) );
            }

        } catch (NullPointerException e) {
            log.error("❌ traffic Not Found : {}", e.getMessage(), e);
            throw new BusinessException(TrafficErrorCode.NOT_FOUND_TRAFFIC);
        }

        return responseRedis;

    }
}
