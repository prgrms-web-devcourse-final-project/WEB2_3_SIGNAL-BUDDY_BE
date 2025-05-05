package org.programmers.signalbuddyfinal.domain.trafficSignal.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.programmers.signalbuddyfinal.domain.trafficSignal.dto.TrafficResponse;
import org.programmers.signalbuddyfinal.domain.trafficSignal.exception.TrafficErrorCode;
import org.programmers.signalbuddyfinal.domain.trafficSignal.repository.CustomTrafficRepositoryImpl;
import org.programmers.signalbuddyfinal.domain.trafficSignal.repository.TrafficRepository;
import org.programmers.signalbuddyfinal.global.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.programmers.signalbuddyfinal.domain.trafficSignal.repository.TrafficRedisRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TrafficService {

    private final CustomTrafficRepositoryImpl customTrafficRepository;
    private final TrafficRedisRepository trafficRedisRepository;
    private final TrafficRepository trafficRepository;

    public List<TrafficResponse> searchAndSaveTraffic(Double lat, Double lng, int radius){

        log.debug("주변 보행등 정보 - lat = {}, lng = {}, radius = {}", lat, lng, radius);
        List<TrafficResponse> responseDB;

        if (trafficRedisRepository.isExist()) {
            double kiloRadius = (double) radius/1000;
            List<TrafficResponse> responseRedis = trafficRedisRepository.findNearbyTraffics(lat, lng, kiloRadius);

            log.debug("redis 주변 보행등 데이터 : redis data 갯수 = {} ", responseRedis.size());
            return responseRedis;
        }

        try {
            responseDB = customTrafficRepository.findNearestTraffics(lat, lng, radius);

            log.debug("주변 보행등 정보 캐싱 : DB data 갯수 = {} ", responseDB.size());
            for (TrafficResponse response : responseDB) {
                trafficRedisRepository.save(response);
            }

            log.debug("DB 주변 보행등 데이터 캐싱 성공");
            return responseDB;

        } catch (Exception e) {
            log.error("주변 보행등 조회 실패 : lat = {}, lng = {}, radius = {}, error = {}", lat, lng, radius, e.getMessage());
            throw new BusinessException(TrafficErrorCode.NOT_FOUND_TRAFFIC);
        }
    }

    public TrafficResponse trafficFindById(Long id) {

        log.debug("보행등 세부정보 찾기 - id = {}", id);

        TrafficResponse responseRedis = trafficRedisRepository.findById( id );

        if(responseRedis != null) {
            log.info("redis 보행등 세부정보 : redis data = {} ", responseRedis);
            return responseRedis;
        }

        try{

            TrafficResponse responseDB = new TrafficResponse(trafficRepository.findByTrafficSignalId(id));

            log.info("보행등 세부정보 : DB data = {} ", responseDB);

            trafficRedisRepository.save(responseDB);

            log.info("보행등 세부정보 캐싱 성공");
            return responseDB;

        } catch (Exception e) {
            log.error("보행등 세부정보 조회 실패 : {}", e.getMessage(), e);
            throw new BusinessException(TrafficErrorCode.NOT_FOUND_TRAFFIC);
        }

    }
}
