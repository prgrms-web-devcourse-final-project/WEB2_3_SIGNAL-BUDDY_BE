package org.programmers.signalbuddyfinal.domain.trafficSignal.dto;

import lombok.*;
import org.locationtech.jts.geom.Point;
import org.programmers.signalbuddyfinal.domain.trafficSignal.entity.TrafficSignal;
import org.programmers.signalbuddyfinal.global.util.PointUtil;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
public class TrafficResponse {

    private Long trafficId;

    private Long serialNumber;

    private String district;

    private String signalType;

    private String address;

    private Double lat;

    private Double lng;

    public Point toPoint() { return PointUtil.toPoint(this.lat, this.lng); }

    public TrafficResponse(TrafficSignal trafficSignal) {
        this.trafficId = trafficSignal.getTrafficSignalId();
        this.serialNumber = trafficSignal.getSerialNumber();
        this.district = trafficSignal.getDistrict();
        this.signalType = trafficSignal.getSignalType();
        this.address = trafficSignal.getAddress();
        this.lat = trafficSignal.getCoordinate().getY();
        this.lng = trafficSignal.getCoordinate().getX();
    }
}
