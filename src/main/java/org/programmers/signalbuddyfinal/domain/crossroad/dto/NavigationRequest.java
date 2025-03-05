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
    private String[] crossroadApiIds;
    private Boolean isFinished;
}
