package org.programmers.signalbuddyfinal.domain.trafficSignal.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.locationtech.jts.geom.Point;
import org.programmers.signalbuddyfinal.domain.trafficSignal.entity.TrafficSignal;
import org.programmers.signalbuddyfinal.global.util.PointUtil;

@Getter
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TrafficApiResponse {

    private Long serialNumber;
    private String district;
    private String signalType;
    private String address;
    private Double lat;
    private Double lng;

    public Point toPoint() { return PointUtil.toPoint(this.lat, this.lng); }

}
