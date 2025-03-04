package org.programmers.signalbuddyfinal.domain.trafficSignal.dto;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvNumber;
import lombok.*;
import org.locationtech.jts.geom.Point;
import org.programmers.signalbuddyfinal.global.util.PointUtil;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
public class TrafficFileResponse {

    @CsvBindByName(column = "serial")
    @CsvNumber("#")
    private Long serial;

    @CsvBindByName(column = "district")
    private String district;

    @CsvBindByName(column = "signalType")
    private String signalType;

    @CsvBindByName(column = "lat")
    private Double lat;

    @CsvBindByName(column = "lng")
    private Double lng;

    @CsvBindByName(column = "address")
    private String address;


    public Point toPoint() { return PointUtil.toPoint(this.lat, this.lng); }
}
