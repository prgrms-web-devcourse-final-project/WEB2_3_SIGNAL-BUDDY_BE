package org.programmers.signalbuddyfinal.domain.trafficSignal.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;

@Entity(name = "traffic_signals")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TrafficSignal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long trafficSignalId;

    @Column(unique = true, nullable = false)
    private Long serialNumber;

    @Column(nullable = false)
    private String district;

    @Column(nullable = false)
    private String signalType;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private Point coordinate;
}
