package org.programmers.signalbuddyfinal.domain.crossroad.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.programmers.signalbuddyfinal.global.exception.BusinessException;
import org.programmers.signalbuddyfinal.global.exception.GlobalErrorCode;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum SignalState {
    GREEN("protected-Movement-Allowed",true),   // 신호등이 녹색, 이동 보장 상태
    YELLOW("permissive-Movement-Allowed",true), // 신호등이 황색, 이동 가능 상태
    RED("stop-And-Remain",false);               // 신호등이 적색, 정지 상태

    private final String state;
    private final boolean canCross;

    @JsonCreator // 데이터 역직렬화 JSON -> java
    public static SignalState fromState(String state) {
        for(SignalState signalState : SignalState.values()) {
            if(signalState.state.equals(state) || signalState.name().equals(state)) {
                return signalState;
            }
        }
        throw new BusinessException(GlobalErrorCode.BAD_REQUEST);
    }
}
