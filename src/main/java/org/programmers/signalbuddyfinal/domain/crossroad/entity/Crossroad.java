package org.programmers.signalbuddyfinal.domain.crossroad.entity;

import jakarta.persistence.*;
import java.util.Objects;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;
import org.locationtech.jts.geom.Point;
import org.programmers.signalbuddyfinal.domain.basetime.BaseTimeEntity;
import org.programmers.signalbuddyfinal.domain.crossroad.dto.CrossroadApiResponse;

@Entity(name = "crossroads")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
@SQLRestriction("status = true")
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
    private Boolean status;

    @Builder(builderMethodName = "create")
    public Crossroad(String crossroadApiId, String name, Point coordinate) {
        this.crossroadApiId = Objects.requireNonNull(crossroadApiId);
        this.name = Objects.requireNonNull(name);
        this.coordinate = Objects.requireNonNull(coordinate);
        this.status = Boolean.TRUE;
    }

    public Crossroad(CrossroadApiResponse response) {
        this.crossroadApiId = response.getCrossroadApiId();
        this.name = response.getName();
        this.coordinate = response.toPoint();
        this.status = Boolean.TRUE;
    }
}