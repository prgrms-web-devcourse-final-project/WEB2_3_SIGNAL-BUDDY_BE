package org.programmers.signalbuddyfinal.domain.trafficSignal.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.opencsv.bean.CsvBindByName;
import lombok.*;
import org.locationtech.jts.geom.Point;
import org.programmers.signalbuddyfinal.global.util.PointUtil;

@Getter
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TrafficFileResponse {

    @CsvBindByName
    private Long serialNumber;

    @CsvBindByName
    private String district;

    @CsvBindByName
    private String signalType;

    @CsvBindByName
    private Double lat;

    @CsvBindByName
    private Double lng;

    @CsvBindByName
    private String address;

    public Point toPoint() { return PointUtil.toPoint(this.lat, this.lng); }

}
