package org.programmers.signalbuddyfinal.domain.air_quality.service;

import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.proj4j.ProjCoordinate;
import org.programmers.signalbuddyfinal.domain.air_quality.dto.*;
import org.programmers.signalbuddyfinal.domain.air_quality.exception.AirQualityErrorCode;
import org.programmers.signalbuddyfinal.global.exception.BusinessException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AirQualityService {

    private final ObservatoryProvider observatoryProvider;
    private final CoordinateConverter coordinateConverter;
    private final List<AirQualityStrategy> strategies;

    public AirQualityResponse getAirQuality(double lat, double lng) {
        ObservatoryResponse observatoryResponse = getObservatory(lat, lng).orElseThrow(
            () -> new BusinessException(AirQualityErrorCode.AIR_QUALITY_SERVICE_UNAVAILABLE));

        AirQualityStrategy strategy = strategies.stream()
            .filter(s -> s.supports(observatoryResponse.getAddr()))
            .findFirst()
            .orElseThrow(
                () -> new BusinessException(AirQualityErrorCode.AIR_QUALITY_SERVICE_UNAVAILABLE));

        return strategy.getCache(getRegionCode(observatoryResponse)).orElseGet(
            () -> strategy.update(observatoryResponse)
        );

    }

    private String getRegionCode(ObservatoryResponse observatoryResponse) {
        return observatoryResponse.getStationCode();
    }

    private ProjCoordinate convertCoordinate(double lat, double lng) {
        return coordinateConverter.convert(lat, lng);
    }

    private Optional<ObservatoryResponse> getObservatory(double lat, double lng) {
        ProjCoordinate coordinate = convertCoordinate(lat, lng);
        return observatoryProvider.getObservatory(coordinate.x, coordinate.y);
    }

}
