package org.programmers.signalbuddyfinal.domain.crossroad.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.programmers.signalbuddyfinal.domain.crossroad.dto.CrossroadApiResponse;
import org.programmers.signalbuddyfinal.domain.crossroad.dto.CrossroadResponse;
import org.programmers.signalbuddyfinal.domain.crossroad.dto.CrossroadStateApiResponse;
import org.programmers.signalbuddyfinal.domain.crossroad.dto.CrossroadStateResponse;
import org.programmers.signalbuddyfinal.domain.crossroad.entity.Crossroad;
import org.programmers.signalbuddyfinal.domain.crossroad.exception.CrossroadErrorCode;
import org.programmers.signalbuddyfinal.domain.crossroad.mapper.CrossroadMapper;
import org.programmers.signalbuddyfinal.domain.crossroad.repository.CrossroadRepository;
import org.programmers.signalbuddyfinal.global.exception.BusinessException;
import org.programmers.signalbuddyfinal.global.monitoring.HttpRequestManager;
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

    private final CrossroadRepository crossroadRepository;
    private final CrossroadProvider crossroadProvider;
    private final HttpRequestManager httpRequestManager;
    private final RedisTemplate<Object, Object> redisTemplate;

    private static final String STATE_PREFIX = "crossroad-state:";

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

    public CrossroadStateResponse checkSignalState(Long crossroadId) {
        httpRequestManager.increase(crossroadId);

        CrossroadStateResponse cache = getStateCache(crossroadId);
        if (cache != null && cache.getTransTimestamp() != null) {
            return cache;
        }

        Crossroad crossroad = crossroadRepository.findByIdOrThrow(crossroadId);
        String crossroadApiId = crossroad.getCrossroadApiId();

        List<CrossroadStateApiResponse> apiResponses =
            crossroadProvider.requestCrossroadStateApi(crossroadApiId);

        if (apiResponses.isEmpty()) {
            throw new BusinessException(CrossroadErrorCode.CROSSROAD_API_REQUEST_FAILED);
        }

        CrossroadStateResponse response = CrossroadMapper.INSTANCE.toResponse(
            apiResponses.get(0), crossroadId
        );
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
        return crossroadRepository.findNearestCrossroads(
            lat, lng, radius);

    private void putStateCache(Long crossroadId, CrossroadStateResponse response) {
        ValueOperations<Object, Object> operations = redisTemplate.opsForValue();

        int minTimeLeft = response.minTimeLeft();
        minTimeLeft *= 100; // 1/10초 단위를 1/1000(ms)로 변환

        operations.set(
            STATE_PREFIX + crossroadId, response,
            minTimeLeft, TimeUnit.MILLISECONDS
        );
    }

    private CrossroadStateResponse getStateCache(Long crossroadId) {
        ValueOperations<Object, Object> operations = redisTemplate.opsForValue();
        return (CrossroadStateResponse) operations.get(STATE_PREFIX + crossroadId);
    }
}
