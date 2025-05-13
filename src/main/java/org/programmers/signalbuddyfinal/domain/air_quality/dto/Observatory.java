package org.programmers.signalbuddyfinal.domain.air_quality.dto;

import lombok.Data;
import java.util.List;


public class Observatory {

    @Data
    public static class Response {
        private Body body;
        private Header header;
    }

    @Data
    public static class Body {
        private int totalCount;
        private List<Item> items;
        private int pageNo;
        private int numOfRows;
    }

    @Data
    public static class Item {
        private String stationCode;
        private double tm;
        private String addr;
        private String stationName;
    }

    @Data
    public static class Header {
        private String resultMsg;
        private String resultCode;
    }
}
