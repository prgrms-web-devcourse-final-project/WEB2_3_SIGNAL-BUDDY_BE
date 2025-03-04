package org.programmers.signalbuddyfinal.domain.crossroad.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.programmers.signalbuddyfinal.domain.crossroad.dto.CrossroadApiResponse;
import org.programmers.signalbuddyfinal.domain.crossroad.dto.CrossroadResponse;
import org.programmers.signalbuddyfinal.domain.crossroad.dto.CrossroadStateApiResponse;
import org.programmers.signalbuddyfinal.domain.crossroad.entity.Crossroad;
import org.programmers.signalbuddyfinal.domain.crossroad.exception.CrossroadErrorCode;
import org.programmers.signalbuddyfinal.domain.crossroad.repository.CrossroadRepository;
import org.programmers.signalbuddyfinal.global.exception.BusinessException;
import org.programmers.signalbuddyfinal.global.monitoring.HttpRequestManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CrossroadService {

    private final CrossroadRepository crossroadRepository;
    private final CrossroadProvider crossroadProvider;
    private final HttpRequestManager httpRequestManager;

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

    public List<CrossroadStateApiResponse> checkSignalState(Long id) { // id값으로 신호등의 상태를 검색

        httpRequestManager.increase(id);
        return crossroadProvider.requestCrossroadStateApi(id);
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
    }
}
