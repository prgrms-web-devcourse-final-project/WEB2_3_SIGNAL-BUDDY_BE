package org.programmers.signalbuddyfinal.domain.crossroad.dto;

import java.io.Serializable;
import java.util.Objects;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CrossroadStateResponse implements Serializable {

    private Long crossroadId;

    private String crossroadApiId;

    private Long transTimestamp;

    private Integer northTimeLeft;

    private Integer eastTimeLeft;

    private Integer southTimeLeft;

    private Integer westTimeLeft;

    private Integer northeastTimeLeft;

    private Integer northwestTimeLeft;

    private Integer southwestTimeLeft;

    private Integer southeastTimeLeft;

    private SignalState northState;

    private SignalState eastState;

    private SignalState westState;

    private SignalState southState;

    private SignalState northeastState;

    private SignalState northwestState;

    private SignalState southeastState;

    private SignalState southwestState;

    public int minTimeLeft() {
        return Stream.of(
            northTimeLeft, eastTimeLeft, southTimeLeft, westTimeLeft,
                northeastTimeLeft, northwestTimeLeft, southwestTimeLeft, southeastTimeLeft
            )
            .filter(Objects::nonNull)  // null 값 제거
            .min(Integer::compare)  // 최소값 구하기
            .orElse(0); // 모든 값이 null일 경우
    }
}
