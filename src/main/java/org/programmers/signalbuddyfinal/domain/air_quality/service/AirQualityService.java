package org.programmers.signalbuddyfinal.domain.air_quality.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.programmers.signalbuddyfinal.domain.air_quality.dto.AirQuality;
import org.programmers.signalbuddyfinal.domain.air_quality.dto.AirQualityResponse;
import org.programmers.signalbuddyfinal.domain.air_quality.dto.CachedAirQuality;
import org.programmers.signalbuddyfinal.domain.air_quality.exception.AirQualityErrorCode;
import org.programmers.signalbuddyfinal.global.exception.BusinessException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AirQualityService {

    private final AirQualityProvider airQualityProvider;
    private final RedisTemplate<String, CachedAirQuality> redisTemplate;

    private static final String key = "air-quality: ";
    private static final Duration TTL = Duration.ofHours(2);

    public AirQualityResponse getAirQuality() {
        CachedAirQuality cached = redisTemplate.opsForValue().get(key);
        if (cached != null && cached.isFresh()) {
            return cached.getData();
        }
        return updateAriQuality();
    }

    public AirQualityResponse updateAriQuality() {
        Optional<AirQuality> airQuality = airQualityProvider.getAirQuality();

        // 응답 성공
        if (airQuality.isPresent()) {
            AirQualityResponse response = createResponse(airQuality);
            saveToCache(response, true);
            return response;
        } else {
            // 응답 실패
            CachedAirQuality previous = redisTemplate.opsForValue().get(key);
            if (previous != null) {
                saveToCache(previous.getData(), false);
                return previous.getData();
            }
            throw new BusinessException(AirQualityErrorCode.AIR_QUALITY_SERVICE_UNAVAILABLE);
        }
    }

    private AirQualityResponse createResponse(Optional<AirQuality> airQuality) {
        return AirQualityResponse.builder()
            .grade(airQuality.get().getRow().get(0).getGrade())
            .pm25(airQuality.get().getRow().get(0).getPm25())
            .pm10(airQuality.get().getRow().get(0).getPm10())
            .build();
    }

    private void saveToCache(AirQualityResponse airQualityResponse, boolean fresh) {
        redisTemplate.opsForValue().set(key, new CachedAirQuality(airQualityResponse, fresh,
            LocalDateTime.now()), TTL);
    }
}
