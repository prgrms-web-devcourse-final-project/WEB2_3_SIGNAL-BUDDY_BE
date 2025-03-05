package org.programmers.signalbuddyfinal.domain.crossroad.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class LocationRequest {

    private Double lat;
    private Double lng;
    private Integer radius;
}
