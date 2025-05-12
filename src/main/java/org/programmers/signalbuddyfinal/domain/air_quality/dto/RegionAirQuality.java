package org.programmers.signalbuddyfinal.domain.air_quality.dto;

import lombok.Builder;
import java.util.List;
import lombok.Getter;

@Builder
@Getter
public class RegionAirQuality {
    private Response response;

    @Builder
    @Getter
    public static class Response {
        private Body body;
        private Header header;
    }

    @Builder
    @Getter
    public static class Body {
        private int totalCount;
        private List<Item> items;
        private int pageNo;
        private int numOfRows;
    }

    @Builder
    @Getter
    public static class Item {
        private String so2Grade;
        private String coFlag;
        private String khaiValue;
        private String so2Value;
        private String coValue;
        private String pm25Flag;
        private String pm10Flag;
        private String pm10Value;
        private String o3Grade;
        private String khaiGrade;
        private String pm25Value;
        private String no2Flag;
        private String no2Grade;
        private String o3Flag;
        private String pm25Grade;
        private String so2Flag;
        private String dataTime;
        private String coGrade;
        private String no2Value;
        private String pm10Grade;
        private String o3Value;
    }

    @Builder
    @Getter
    public static class Header {
        private String resultMsg;
        private String resultCode;
    }
}
