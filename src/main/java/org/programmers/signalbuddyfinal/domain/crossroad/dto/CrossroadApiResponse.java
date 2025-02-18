package org.programmers.signalbuddyfinal.domain.crossroad.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.locationtech.jts.geom.Point;
import org.programmers.signalbuddyfinal.domain.crossroad.entity.Crossroad;
import org.programmers.signalbuddyfinal.domain.crossroad.service.PointUtil;

@Getter
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CrossroadApiResponse {

    @JsonProperty("itstId")
    private String crossroadApiId;

    @JsonProperty("itstNm")
    private String name;

    @JsonProperty("mapCtptIntLat")
    private Double lat; // 위도

    @JsonProperty("mapCtptIntLot")
    private Double lng; // 경도

    public Point toPoint() {
        return PointUtil.toPoint(this.lat, this.lng);
    }

    public CrossroadApiResponse(Crossroad crossroad) {
        this.crossroadApiId = crossroad.getCrossroadApiId();
        this.name = crossroad.getName();
        this.lat = crossroad.getCoordinate().getY();
        this.lng = crossroad.getCoordinate().getX();
    }
}
