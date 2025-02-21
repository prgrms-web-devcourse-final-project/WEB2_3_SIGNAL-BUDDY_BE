package org.programmers.signalbuddyfinal.domain.feedback.entity.enums;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum FeedbackCategory {
    ETC("기타", "etc"), DELAY("신호 지연", "delay"),
    MALFUNCTION("오작동", "malfunction"), ADD_SIGNAL("신호등 추가", "add-signal");

    private final String message;
    private final String value;
}
