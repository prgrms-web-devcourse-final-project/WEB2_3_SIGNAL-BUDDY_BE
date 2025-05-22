package org.programmers.signalbuddyfinal.domain.air_quality.service;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.programmers.signalbuddyfinal.domain.air_quality.dto.AirQualityResponse;
import org.programmers.signalbuddyfinal.domain.air_quality.dto.CachedAirQuality;
import org.programmers.signalbuddyfinal.domain.air_quality.dto.ObservatoryResponse;
import org.programmers.signalbuddyfinal.domain.air_quality.dto.RegionAirQuality;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Qualifier("regionAirQualityStrategy")
public class RegionAirQualityService implements AirQualityStrategy {

    private final AirQualityCacheService airQualityCacheService;
    private final RegionAirQualityProvider regionAirQualityProvider;

    @Override
    public boolean supports(String addr) {
        return !addr.startsWith("서울");
    }

    @Override
    public Optional<AirQualityResponse> getCache(String code) {
        return Optional.ofNullable(airQualityCacheService.get(code))
            .filter(CachedAirQuality::isFresh)
            .map(CachedAirQuality::getData);
    }

    @Override
    public AirQualityResponse update(ObservatoryResponse observatoryResponse) {
        return requestAllAirQuality(observatoryResponse.getStationName())
            .map(newAirQuality -> successfulAllResponse(newAirQuality,
                observatoryResponse.getStationCode()))
            .orElseGet(
                () -> airQualityCacheService.failBackOrThrow(observatoryResponse.getStationCode()));
    }

    private Optional<RegionAirQuality> requestAllAirQuality(String stationName) {
        return regionAirQualityProvider.geAllAirQuality(stationName);
    }

    private AirQualityResponse successfulAllResponse(RegionAirQuality newAirQuality, String value) {
        AirQualityResponse response = createAllResponse(newAirQuality);
        airQualityCacheService.save(value, response, true);
        return response;
    }

    private AirQualityResponse createAllResponse(RegionAirQuality regionAirQuality) {
        return AirQualityResponse.builder()
            .grade(convertGrade(regionAirQuality))
            .pm25(regionAirQuality.getResponse().getBody().getItems().get(0).getPm25Value())
            .pm10(regionAirQuality.getResponse().getBody().getItems().get(0).getPm10Value())
            .build();
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
