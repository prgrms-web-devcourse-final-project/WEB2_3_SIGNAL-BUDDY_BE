package org.programmers.signalbuddyfinal.domain.crossroad.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;
import org.programmers.signalbuddyfinal.domain.crossroad.entity.Crossroad;
import org.programmers.signalbuddyfinal.global.util.PointUtil;
import lombok.ToString;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CrossroadResponse {

    private Long crossroadId;

    private String crossroadApiId;

    private String name;

    private Double lat;

    private Double lng;

    private String status;

    public Point toPoint() {
        return PointUtil.toPoint(this.lat, this.lng);
    }

    public CrossroadResponse(Crossroad crossroad) {
        this.crossroadId = crossroad.getCrossroadId();
        this.crossroadApiId = crossroad.getCrossroadApiId();
        this.name = crossroad.getName();
        this.lat = crossroad.getCoordinate().getY();
        this.lng = crossroad.getCoordinate().getX();
        this.status = crossroad.getStatus();
    }
}
