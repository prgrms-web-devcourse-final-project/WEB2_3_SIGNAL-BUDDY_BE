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

    public List<TrafficResponse> searchAndSaveTraffic(Double lat, Double lng, Integer radius) {

        List<TrafficResponse> responseDB = new ArrayList<>();
        List<TrafficResponse> responseRedis = new ArrayList<>();
        boolean flag = false;

        try{
            responseDB.addAll(customTrafficRepository.findNearestTraffics(lat, lng, radius));

            if(trafficRedisRepository.findById(responseDB.get(0).getTrafficId())==null){
                flag = true;
            }

            for(TrafficResponse response : responseDB){
                responseRedis.add(trafficRedisRepository.findById(response.getTrafficId()));

                if (trafficRedisRepository.findById(response.getTrafficId())!=null) {
                    trafficRedisRepository.save(response);
                };
            }

            if(flag){
                return responseDB;
            } else {
                return responseRedis;
            }

        } catch (NullPointerException e) {
            log.error("❌ traffic Not Found : {}", e.getMessage(), e);
            throw new BusinessException(TrafficErrorCode.NOT_FOUND_TRAFFIC);
        }


    }

    public TrafficResponse trafficFindById(Long id) {

        TrafficResponse responseRedis = trafficRedisRepository.findById( id );
        TrafficResponse responseDB = new TrafficResponse(trafficRepository.findByTrafficSignalId(id));
        boolean flag = false;

        try{
            if (responseRedis == null) {
                flag = true;
                trafficRedisRepository.save(responseDB);
                responseRedis = trafficRedisRepository.findById((id));

            }

            if(flag){
                return responseDB;
            } else {
                return responseRedis;
            }

        } catch (NullPointerException e) {
            log.error("❌ traffic Not Found : {}", e.getMessage(), e);
            throw new BusinessException(TrafficErrorCode.NOT_FOUND_TRAFFIC);
        }
    }
}
