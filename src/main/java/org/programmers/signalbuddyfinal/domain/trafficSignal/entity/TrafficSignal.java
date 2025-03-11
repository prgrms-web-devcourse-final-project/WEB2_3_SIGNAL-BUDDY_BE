package org.programmers.signalbuddyfinal.domain.trafficSignal.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;
import org.programmers.signalbuddyfinal.domain.trafficSignal.dto.TrafficFileResponse;

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

    public TrafficSignal(TrafficFileResponse trafficSignal) {
        this.serialNumber = trafficSignal.getSerial();
        this.district = trafficSignal.getDistrict();
        this.signalType = trafficSignal.getSignalType();
        this.address = trafficSignal.getAddress();
        this.coordinate = trafficSignal.toPoint();
    }

}
