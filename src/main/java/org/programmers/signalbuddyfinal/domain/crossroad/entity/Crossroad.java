package org.programmers.signalbuddyfinal.domain.crossroad.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.SQLRestriction;
import org.locationtech.jts.geom.Point;
import org.programmers.signalbuddyfinal.domain.basetime.BaseTimeEntity;
import org.programmers.signalbuddyfinal.global.util.PointUtils;

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
    public Crossroad(String crossroadApiId, String name, Double lat, Double lng) {
        this.crossroadApiId = Objects.requireNonNull(crossroadApiId);
        this.name = Objects.requireNonNull(name);
        this.coordinate = PointUtils.toPoint(
            Objects.requireNonNull(lat),
            Objects.requireNonNull(lng)
        );
        this.status = Boolean.TRUE;
    }
}