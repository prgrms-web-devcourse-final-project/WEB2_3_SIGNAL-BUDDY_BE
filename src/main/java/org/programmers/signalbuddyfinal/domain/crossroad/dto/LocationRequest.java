package org.programmers.signalbuddyfinal.domain.crossroad.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class LocationRequest {

    private double lat;
    private double lng;
    private int radius;
}
