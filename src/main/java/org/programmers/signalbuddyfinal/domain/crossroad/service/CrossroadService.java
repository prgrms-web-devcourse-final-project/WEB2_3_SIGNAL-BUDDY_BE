package org.programmers.signalbuddyfinal.domain.crossroad.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Point;
import org.programmers.signalbuddyfinal.domain.crossroad.dto.CrossroadApiResponse;
import org.programmers.signalbuddyfinal.domain.crossroad.dto.CrossroadResponse;
import org.programmers.signalbuddyfinal.domain.crossroad.dto.CrossroadStateApiResponse;
import org.programmers.signalbuddyfinal.domain.crossroad.dto.CrossroadStateResponse;
import org.programmers.signalbuddyfinal.domain.crossroad.dto.NavigationRequest.Coordinate;
import org.programmers.signalbuddyfinal.domain.crossroad.entity.Crossroad;
import org.programmers.signalbuddyfinal.domain.crossroad.exception.CrossroadErrorCode;
import org.programmers.signalbuddyfinal.domain.crossroad.mapper.CrossroadMapper;
import org.programmers.signalbuddyfinal.domain.crossroad.repository.CrossroadRedisRepository;
import org.programmers.signalbuddyfinal.domain.crossroad.repository.CrossroadRepository;
import org.programmers.signalbuddyfinal.domain.crossroad.repository.CustomCrossroadRepositoryImpl;
import org.programmers.signalbuddyfinal.domain.trafficSignal.dto.TrafficResponse;
import org.programmers.signalbuddyfinal.global.exception.BusinessException;
import org.programmers.signalbuddyfinal.global.monitoring.HttpRequestManager;
import org.programmers.signalbuddyfinal.global.util.PointUtil;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CrossroadService {

    private static final String STATE_PREFIX = "crossroad-state:";

    private final CrossroadRepository crossroadRepository;
    private final CrossroadRedisRepository crossroadRedisRepository;
    private final CustomCrossroadRepositoryImpl customCrossroadRepository;
    private final CrossroadProvider crossroadProvider;
    private final HttpRequestManager httpRequestManager;
    private final RedisTemplate<Object, Object> redisTemplate;

    @Transactional
    public void saveCrossroadDates(int page, int pageSize) {
        List<CrossroadApiResponse> responseList = crossroadProvider.requestCrossroadApi(page,
            pageSize);

        List<Crossroad> entityList = new ArrayList<>();
        for (CrossroadApiResponse response : responseList) {
            if (response.getLng() != null && response.getLat() != null) {
                entityList.add(new Crossroad(response));
            }
        }

        try {
            crossroadRepository.saveAll(entityList);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(CrossroadErrorCode.ALREADY_EXIST_CROSSROAD);
        }
    }

    public List<CrossroadResponse> searchAndSaveCrossroad(Double lat, Double lng, int radius){

        List<CrossroadResponse> responseDB = new ArrayList<>();
        List<CrossroadResponse> responseRedis = new ArrayList<>();
        boolean flag = false;

        try{
            responseDB.addAll(customCrossroadRepository.findNearestCrossroads(lat, lng, radius));

            if(crossroadRedisRepository.findById(responseDB.get(0).getCrossroadId())==null){
                flag = true;
            }

            for(CrossroadResponse response : responseDB){
                responseRedis.add(crossroadRedisRepository.findById(response.getCrossroadId()));

                if (crossroadRedisRepository.findById(response.getCrossroadId())!=null) {
                    crossroadRedisRepository.save(response);
                };
            }

            if(flag){
                return responseDB;
            } else {
                return responseRedis;
            }

        } catch (NullPointerException e) {
            log.error("❌ crossroad Not Found : {}", e.getMessage(), e);
            throw new BusinessException(CrossroadErrorCode.NOT_FOUND_CROSSROAD);
        }
    }

    public CrossroadResponse crossroadFindById(Long id) {

        CrossroadResponse responseRedis = crossroadRedisRepository.findById( id );
        CrossroadResponse responseDB = new CrossroadResponse(crossroadRepository.findByCrossroadId(id));
        boolean flag = false;

        try{
            if (responseRedis == null) {
                flag = true;
                crossroadRedisRepository.save(responseDB);
                responseRedis = crossroadRedisRepository.findById((id));

            }

            if(flag){
                return responseDB;
            } else {
                return responseRedis;
            }

        } catch (NullPointerException e) {
            log.error("❌ crossroad Not Found : {}", e.getMessage(), e);
            throw new BusinessException(CrossroadErrorCode.NOT_FOUND_CROSSROAD);
        }

    }

    public CrossroadStateResponse checkSignalState(Long crossroadId) {
        httpRequestManager.increase(crossroadId);

        CrossroadStateResponse cache = getStateCache(crossroadId);
        if (cache != null && cache.getTransTimestamp() != null) {
            return cache;
        }

        Crossroad crossroad = crossroadRepository.findByIdOrThrow(crossroadId);
        String crossroadApiId = crossroad.getCrossroadApiId();

        List<CrossroadStateApiResponse> apiResponses = crossroadProvider.requestCrossroadStateApi(
            crossroadApiId);

        if (apiResponses.isEmpty()) {
            throw new BusinessException(CrossroadErrorCode.CROSSROAD_API_REQUEST_FAILED);
        }

        CrossroadStateResponse response = CrossroadMapper.INSTANCE.toResponse(apiResponses.get(0),
            crossroadId);
        putStateCache(crossroadId, response);
        return response;
    }

    public List<CrossroadApiResponse> getAllMarkers() {
        List<Crossroad> crossroads = crossroadRepository.findAll();
        List<CrossroadApiResponse> responseList = new ArrayList<>();

        for (Crossroad crossroad : crossroads) {
            responseList.add(new CrossroadApiResponse(crossroad));
        }

        return responseList;
    }

    public List<CrossroadResponse> findNearestCrossroad(double lat, double lng, int radius) {
        return crossroadRepository.findNearestCrossroads(lat, lng, radius);
    }

    private void putStateCache(Long crossroadId, CrossroadStateResponse response) {
        ValueOperations<Object, Object> operations = redisTemplate.opsForValue();

        int minTimeLeft = response.minTimeLeft();
        minTimeLeft *= 100; // 1/10초 단위를 1/1000(ms)로 변환

        operations.set(STATE_PREFIX + crossroadId, response, minTimeLeft, TimeUnit.MILLISECONDS);
    }

    private CrossroadStateResponse getStateCache(Long crossroadId) {
        ValueOperations<Object, Object> operations = redisTemplate.opsForValue();
        return (CrossroadStateResponse) operations.get(STATE_PREFIX + crossroadId);
    }

    @Transactional(readOnly = true)
    public List<Long> getCrossroadIdsByCoordinates(Coordinate[] coordinates, int radius) {
        final List<Point> points = Arrays.stream(coordinates)
            .map(coordinate -> PointUtil.toPoint(coordinate.getLat(), coordinate.getLng()))
            .toList();
        return crossroadRepository.findByCoordinateInWithRadius(points, radius);
    }
}
