package org.programmers.signalbuddyfinal.domain.air_quality.service;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.programmers.signalbuddyfinal.domain.air_quality.dto.AirQualityResponse;
import org.programmers.signalbuddyfinal.domain.air_quality.dto.CachedAirQuality;
import org.programmers.signalbuddyfinal.domain.air_quality.dto.ObservatoryResponse;
import org.programmers.signalbuddyfinal.domain.air_quality.dto.SeoulAirQuality;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Qualifier("seoulAirQualityStrategy")
public class SeoulAirQualityService implements AirQualityStrategy {

    private final AirQualityCacheService airQualityCacheService;
    private final SeoulAirQualityProvider airQualityProvider;
    private final String REGION_CODE = "seoul";

    @Override
    public boolean supports(String addr) {
        return addr.startsWith("서울");
    }

    @Override
    public Optional<AirQualityResponse> getCache(String code) {
        return Optional.ofNullable(airQualityCacheService.get(REGION_CODE))
            .filter(CachedAirQuality::isFresh)
            .map(CachedAirQuality::getData);
    }

    public AirQualityResponse update() {
        return requestAirQuality()
            .map(this::successfulResponse)
            .orElseGet(() -> airQualityCacheService.failBackOrThrow(REGION_CODE));
    }

    @Override
    public AirQualityResponse update(ObservatoryResponse obs) {
        return update();
    }

    private AirQualityResponse successfulResponse(SeoulAirQuality newSeoulAirQuality) {
        AirQualityResponse response = createResponse(newSeoulAirQuality);
        airQualityCacheService.save(REGION_CODE, response, true);
        return response;
    }

    private Optional<SeoulAirQuality> requestAirQuality() {
        return airQualityProvider.getAirQuality();
    }

    private AirQualityResponse createResponse(SeoulAirQuality seoulAirQuality) {
        return AirQualityResponse.builder()
            .grade(seoulAirQuality.getRow().get(0).getGrade())
            .pm25(seoulAirQuality.getRow().get(0).getPm25())
            .pm10(seoulAirQuality.getRow().get(0).getPm10())
            .build();
    }
}
