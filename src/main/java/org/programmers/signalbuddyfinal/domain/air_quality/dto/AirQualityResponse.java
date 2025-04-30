package org.programmers.signalbuddyfinal.domain.air_quality.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AirQualityResponse implements Serializable {
    private String grade;
    private String pm10;
    private String pm25;
}
