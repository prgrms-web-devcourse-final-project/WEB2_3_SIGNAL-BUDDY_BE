package org.programmers.signalbuddyfinal.domain.weather.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity(name = "grid_coordinates")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class GridCoordinate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String regionCode;

    @Column
    private String city;

    @Column
    private String district;

    @Column
    private String subdistrict;

    @Column
    private Double gridX;

    @Column
    private Double gridY;

    @Column
    private Double lng;

    @Column
    private Double lat;
}
