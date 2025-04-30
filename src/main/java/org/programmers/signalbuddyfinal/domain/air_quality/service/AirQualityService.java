package org.programmers.signalbuddyfinal.domain.air_quality.service;

import java.time.Duration;
import java.util.Optional;

import io.jsonwebtoken.security.Jwks;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.programmers.signalbuddyfinal.domain.air_quality.dto.AirQuality;
import org.programmers.signalbuddyfinal.domain.air_quality.dto.AirQualityResponse;
import org.programmers.signalbuddyfinal.domain.air_quality.dto.CachedAirQuality;
import org.programmers.signalbuddyfinal.domain.air_quality.exception.AirQualityErrorCode;
import org.programmers.signalbuddyfinal.global.exception.BusinessException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.swing.text.html.Option;

@Slf4j
@Service
@RequiredArgsConstructor
public class AirQualityService {

    private final AirQualityProvider airQualityProvider;
    private final RedisTemplate<Object, Object> redisTemplate;

    private static final String key = "air-quality: ";
    private static final Duration TTL = Duration.ofHours(2);

    public AirQualityResponse getAirQuality() {
        return getCachedAirQuality().orElseGet(this::updateAriQuality);
    }

    public AirQualityResponse updateAriQuality() {
        return requestAirQuality()
                .map(this::successfulResponse)
                .orElseGet(this::failBackOrThrow);
    }

    private AirQualityResponse createResponse(AirQuality airQuality) {
        return AirQualityResponse.builder()
                .grade(airQuality.getRow().get(0).getGrade())
                .pm25(airQuality.getRow().get(0).getPm25())
                .pm10(airQuality.getRow().get(0).getPm10())
                .build();
    }

    private void saveToCache(AirQualityResponse airQualityResponse, boolean fresh) {
        redisTemplate.opsForValue().set(key, new CachedAirQuality(airQualityResponse, fresh), TTL);
    }

    private Optional<AirQualityResponse> getCachedAirQuality() {
        return Optional.ofNullable(getCache())
                .filter(CachedAirQuality::isFresh)
                .map(CachedAirQuality::getData);
    }

    private AirQualityResponse successfulResponse(AirQuality newAirQuality) {
        AirQualityResponse response = createResponse(newAirQuality);
        saveToCache(response, true);
        return response;
    }

    private AirQualityResponse failBackOrThrow() {
        CachedAirQuality previous = getCache();
        if (previous != null) {
            saveToCache(previous.getData(), false);
            return previous.getData();
        }
        throw new BusinessException(AirQualityErrorCode.AIR_QUALITY_SERVICE_UNAVAILABLE);
    }

    private CachedAirQuality getCache() {
        return (CachedAirQuality) redisTemplate.opsForValue().get(key);
    }

    private Optional<AirQuality> requestAirQuality() {
        return airQualityProvider.getAirQuality();
    }
}
