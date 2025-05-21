package org.programmers.signalbuddyfinal.domain.air_quality.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SeoulAirQuality {

    @JsonProperty("list_total_count")
    private int totalCount;

    @JsonProperty("RESULT")
    private Result result;

    private List<Item> row;

    @Getter
    @Builder
    @JacksonXmlRootElement(localName = "RESULT")
    public static class Result {

        @JsonProperty("CODE")
        @JacksonXmlProperty(localName = "CODE")
        private String code;

        @JsonProperty("MESSAGE")
        @JacksonXmlProperty(localName = "MESSAGE")
        private String message;
    }

    @Getter
    @Builder
    public static class Item{

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
}
