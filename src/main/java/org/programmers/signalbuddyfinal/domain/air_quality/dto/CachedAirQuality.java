package org.programmers.signalbuddyfinal.domain.air_quality.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CachedAirQuality implements Serializable {

    private AirQualityResponse data;

    private boolean fresh;

    private LocalDateTime updatedAt;
}
