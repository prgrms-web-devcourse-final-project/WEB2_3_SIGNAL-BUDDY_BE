package org.programmers.signalbuddyfinal.domain.air_quality.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;

@Getter
public class AirQuality {

    private int list_total_count;

    @JsonProperty("RESULT")
    private Result result;

    private List<AirQualityItems> row;
}
