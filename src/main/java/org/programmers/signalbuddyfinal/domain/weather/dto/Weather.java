package org.programmers.signalbuddyfinal.domain.weather.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
public class Weather {

    public static final String T1H = "T1H";
    public static final String RN1 = "RN1";
    public static final String REH = "REH";
    public static final String PTY = "PTY";
    public static final String VEC = "VEC";
    public static final String WSD = "WSD";

    @JsonProperty("baseDate")
    private String baseDate;

    @JsonProperty("baseTime")
    private String baseTime;

    @JsonProperty("category")
    private String category;

    @JsonProperty("nx")
    private int nx;

    @JsonProperty("ny")
    private int ny;

    @JsonProperty("obsrValue")
    private String obsrValue;
}
