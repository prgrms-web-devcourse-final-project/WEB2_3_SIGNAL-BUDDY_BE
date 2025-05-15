package org.programmers.signalbuddyfinal.domain.air_quality.service;

import org.programmers.signalbuddyfinal.domain.air_quality.dto.AirQualityResponse;
import org.programmers.signalbuddyfinal.domain.air_quality.dto.CachedAirQuality;

public interface AirQualityCache {

    CachedAirQuality get(String regionCode);
    void save(String key, AirQualityResponse response, boolean fresh);
    AirQualityResponse failBackOrThrow(String key);

}
