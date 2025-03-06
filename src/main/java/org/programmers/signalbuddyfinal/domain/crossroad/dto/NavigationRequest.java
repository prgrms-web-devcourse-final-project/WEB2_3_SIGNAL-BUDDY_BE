package org.programmers.signalbuddyfinal.domain.crossroad.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class NavigationRequest {
    private Coordinate[] coordinates;
    private Boolean isFinished;
    private Integer radius;

    @Getter
    @AllArgsConstructor
    @ToString
    public static class Coordinate {
        private Double lat;
        private Double lng;
    }
}
