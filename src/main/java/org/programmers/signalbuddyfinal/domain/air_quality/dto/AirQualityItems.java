package org.programmers.signalbuddyfinal.domain.air_quality.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class AirQualityItems {

    @JsonProperty("GRADE")
    private String grade;

    @JsonProperty("IDEX_MVL")
    private String mvl;

    @JsonProperty("POLLUTANT")
    private String pollutant;

    @JsonProperty("NITROGEN")
    private String nitrogen;

    @JsonProperty("OZONE")
    private String ozone;

    @JsonProperty("CARBON")
    private String carbon;

    @JsonProperty("SULFUROUS")
    private String sulfurous;

    @JsonProperty("PM10")
    private String pm10;

    @JsonProperty("PM25")
    private String pm25;

}
