package org.programmers.signalbuddyfinal.domain.air_quality.service;

import java.time.Duration;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.proj4j.ProjCoordinate;
import org.programmers.signalbuddyfinal.domain.air_quality.dto.*;
import org.programmers.signalbuddyfinal.domain.air_quality.exception.AirQualityErrorCode;
import org.programmers.signalbuddyfinal.global.exception.BusinessException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AirQualityService {

    private final SeoulAirQualityProvider airQualityProvider;
    private final RegionAirQualityProvider regionAirQualityProvider;
    private final ObservatoryProvider observatoryProvider;

    private final RedisTemplate<Object, Object> redisTemplate;
    private final CoordinateConverter coordinateConverter;

    private static final String key = "air-quality:";
    private static final Duration TTL = Duration.ofHours(2);

    public AirQualityResponse getAirQuality(double lat, double lng) {
        ObservatoryResponse observatoryResponse = getObservatory(lat, lng).orElseThrow(
            () -> new BusinessException(AirQualityErrorCode.AIR_QUALITY_SERVICE_UNAVAILABLE));

        if (isSeoul(observatoryResponse)) {
            return getCachedAirQuality("seoul").orElseGet(this::updateAriQuality);
        }
        return getCachedAirQuality(observatoryResponse.getStationCode()).orElseGet(
            () -> updateRegionAriQuality(observatoryResponse));
    }

    public AirQualityResponse updateAriQuality() {
        return requestAirQuality()
            .map(this::successfulResponse)
            .orElseGet(() -> failBackOrThrow("seoul"));
    }

    public AirQualityResponse updateRegionAriQuality(ObservatoryResponse observatoryResponse) {
        return requestAllAirQuality(observatoryResponse.getStationName())
            .map(newAirQuality -> successfulAllResponse(newAirQuality,
                observatoryResponse.getStationCode()))
            .orElseGet(() -> failBackOrThrow(observatoryResponse.getStationCode()));
    }


    private Optional<AirQualityResponse> getCachedAirQuality(String code) {
        return Optional.ofNullable(getCache(code))
            .filter(CachedAirQuality::isFresh)
            .map(CachedAirQuality::getData);
    }

    private AirQualityResponse successfulResponse(SeoulAirQuality newSeoulAirQuality) {
        AirQualityResponse response = createResponse(newSeoulAirQuality);
        saveToCache(response, true, "seoul");
        return response;
    }

    private AirQualityResponse successfulAllResponse(RegionAirQuality newAirQuality, String value) {
        AirQualityResponse response = createAllResponse(newAirQuality);
        saveToCache(response, true, value);
        return response;
    }

    private AirQualityResponse failBackOrThrow(String code) {
        return Optional.ofNullable(getCache(code))
            .map(cache -> {
                saveToCache(cache.getData(), false, code);
                return cache.getData();
            })
            .orElseThrow(
                () -> new BusinessException(AirQualityErrorCode.AIR_QUALITY_SERVICE_UNAVAILABLE));
    }

    private String convertKey(String code) {
        return key + code;
    }

    private void saveToCache(AirQualityResponse airQualityResponse, boolean fresh, String code) {
        redisTemplate.opsForValue()
            .set(convertKey(code), new CachedAirQuality(airQualityResponse, fresh), TTL);
    }

    private CachedAirQuality getCache(String code) {
        return (CachedAirQuality) redisTemplate.opsForValue().get(convertKey(code));
    }

    private Optional<SeoulAirQuality> requestAirQuality() {
        return airQualityProvider.getAirQuality();
    }

    private Optional<RegionAirQuality> requestAllAirQuality(String stationName) {
        return regionAirQualityProvider.geAllAirQuality(stationName);
    }

    private AirQualityResponse createResponse(SeoulAirQuality seoulAirQuality) {
        return AirQualityResponse.builder()
            .grade(seoulAirQuality.getRow().get(0).getGrade())
            .pm25(seoulAirQuality.getRow().get(0).getPm25())
            .pm10(seoulAirQuality.getRow().get(0).getPm10())
            .build();
    }

    private AirQualityResponse createAllResponse(RegionAirQuality regionAirQuality) {
        return AirQualityResponse.builder()
            .grade(convertGrade(regionAirQuality))
            .pm25(regionAirQuality.getResponse().getBody().getItems().get(0).getPm25Value())
            .pm10(regionAirQuality.getResponse().getBody().getItems().get(0).getPm10Value())
            .build();
    }

    private ProjCoordinate convertCoordinate(double lat, double lng) {
        return coordinateConverter.convert(lat, lng);
    }

    private Optional<ObservatoryResponse> getObservatory(double lat, double lng) {
        ProjCoordinate coordinate = convertCoordinate(lat, lng);
        return observatoryProvider.getObservatory(coordinate.x, coordinate.y);
    }

    private boolean isSeoul(ObservatoryResponse observatoryResponse) {
        return observatoryResponse.getAddr().startsWith("서울");
    }

    private String convertGrade(RegionAirQuality airQuality) {
        switch (airQuality.getResponse().getBody().getItems().get(0).getPm10Grade()) {
            case "1":
                return "좋음";
            case "2":
                return "보통";
            case "3":
                return "나쁨";
            case "4":
                return "매우 나쁨";
            default:
                return "-";
        }
    }
}
