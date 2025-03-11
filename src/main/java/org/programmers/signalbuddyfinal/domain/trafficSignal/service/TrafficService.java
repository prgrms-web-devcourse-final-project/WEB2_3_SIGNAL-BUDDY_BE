package org.programmers.signalbuddyfinal.domain.trafficSignal.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.programmers.signalbuddyfinal.domain.trafficSignal.dto.TrafficResponse;
import org.programmers.signalbuddyfinal.domain.trafficSignal.exception.TrafficErrorCode;
import org.programmers.signalbuddyfinal.domain.trafficSignal.repository.CustomTrafficRepositoryImpl;
import org.programmers.signalbuddyfinal.domain.trafficSignal.repository.TrafficRepository;
import org.programmers.signalbuddyfinal.global.exception.BusinessException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.programmers.signalbuddyfinal.domain.trafficSignal.repository.TrafficRedisRepository;

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
    private final RedisTemplate<Object, Object> redisTemplate;

    public List<TrafficResponse> searchAndSaveTraffic(Double lat, Double lng, int radius){

        List<TrafficResponse> responseDB;

        boolean exists = Boolean.TRUE.equals(redisTemplate.hasKey("traffic:info"));
        if (exists) {
            double kiloRadius = (double) radius/1000;
            return trafficRedisRepository.findNearbyTraffics(lat, lng, kiloRadius);
        }

        try {
            responseDB = customTrafficRepository.findNearestTraffics(lat, lng, radius);

            for (TrafficResponse response : responseDB) {
                trafficRedisRepository.save(response);
            }

            return responseDB;

        } catch (NullPointerException e) {
            log.error("❌ traffic Not Found : {}", e.getMessage(), e);
            throw new BusinessException(TrafficErrorCode.NOT_FOUND_TRAFFIC);
        }
    }

    public TrafficResponse trafficFindById(Long id) {

        TrafficResponse responseRedis = trafficRedisRepository.findById( id );

        if(responseRedis != null) {
            return responseRedis;
        }

        try{

            TrafficResponse responseDB = new TrafficResponse(trafficRepository.findByTrafficSignalId(id));
            trafficRedisRepository.save(responseDB);

            return responseDB;

        } catch (NullPointerException e) {
            log.error("❌ traffic Not Found : {}", e.getMessage(), e);
            throw new BusinessException(TrafficErrorCode.NOT_FOUND_TRAFFIC);
        }

    }
}
