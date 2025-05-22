package org.programmers.signalbuddyfinal.domain.air_quality.service;

import java.time.Duration;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.programmers.signalbuddyfinal.domain.air_quality.dto.AirQualityResponse;
import org.programmers.signalbuddyfinal.domain.air_quality.dto.CachedAirQuality;
import org.programmers.signalbuddyfinal.domain.air_quality.exception.AirQualityErrorCode;
import org.programmers.signalbuddyfinal.global.exception.BusinessException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AirQualityCacheService {

    private final RedisTemplate<Object, Object> redisTemplate;

    private static final String PREFIX = "air-quality:";
    private static final Duration TTL = Duration.ofHours(2);

    public CachedAirQuality get(String regionCode) {
        return (CachedAirQuality) redisTemplate.opsForValue().get(formatKey(regionCode));
    }

    public void save(String regionCode, AirQualityResponse airQualityResponse, boolean fresh) {
        redisTemplate.opsForValue()
            .set(formatKey(regionCode), new CachedAirQuality(airQualityResponse, fresh), TTL);
    }

    public AirQualityResponse failBackOrThrow(String regionCode) {
        return Optional.ofNullable(get(regionCode))
            .map(cache -> {
                save(regionCode, cache.getData(), false);
                return cache.getData();
            })
            .orElseThrow(
                () -> new BusinessException(AirQualityErrorCode.AIR_QUALITY_SERVICE_UNAVAILABLE));
    }

    private String formatKey(String regionCode) {
        return PREFIX + regionCode;
    }
}
