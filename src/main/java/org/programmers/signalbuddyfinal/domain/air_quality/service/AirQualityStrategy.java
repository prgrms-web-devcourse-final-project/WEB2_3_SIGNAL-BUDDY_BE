package org.programmers.signalbuddyfinal.domain.air_quality.service;

import java.util.Optional;
import org.programmers.signalbuddyfinal.domain.air_quality.dto.AirQualityResponse;
import org.programmers.signalbuddyfinal.domain.air_quality.dto.ObservatoryResponse;

public interface AirQualityStrategy {

    boolean supports(String address);

    Optional<AirQualityResponse> getCache(String code);

    AirQualityResponse update(ObservatoryResponse obs);

}
