package org.programmers.signalbuddyfinal.domain.crossroad.entity;

import jakarta.persistence.*;
import lombok.*;
import org.locationtech.jts.geom.Point;
import org.programmers.signalbuddyfinal.domain.basetime.BaseTimeEntity;
import org.programmers.signalbuddyfinal.domain.crossroad.dto.CrossroadApiResponse;
import org.programmers.signalbuddyfinal.domain.crossroad.dto.CrossroadResponse;

@Entity(name = "crossroads")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@ToString
public class Crossroad extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long crossroadId;

    @Column(nullable = false, unique = true)
    private String crossroadApiId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Point coordinate;

    @Column(nullable = false)
    private String status;

    public Crossroad(CrossroadApiResponse response) {
        this.crossroadApiId = response.getCrossroadApiId();
        this.name = response.getName();
        this.coordinate = response.toPoint();
        this.status = "FALSE";
    }

    public Crossroad(CrossroadResponse response) {
        this.crossroadId = response.getCrossroadId();
        this.name = response.getName();
        this.coordinate = response.toPoint();
        this.status = "FALSE";
    }

}