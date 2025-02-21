package org.programmers.signalbuddyfinal.domain.crossroad.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CrossroadResponse {

    private Long crossroadId;

    private String name;

    private Double lat;

    private Double lng;

    private String status;
}
