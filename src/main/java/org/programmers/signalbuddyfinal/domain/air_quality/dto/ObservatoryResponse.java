package org.programmers.signalbuddyfinal.domain.air_quality.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ObservatoryResponse {

    private String addr;

    private String stationName;

    private String stationCode;
}
