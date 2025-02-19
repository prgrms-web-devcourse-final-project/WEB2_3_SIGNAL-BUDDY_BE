package org.programmers.signalbuddyfinal.domain.feedback.entity.enums;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum FeedbackCategory {
    ETC("기타"), DELAY("신호 지연"), MALFUNCTION("오작동"), ADD_SIGNAL("신호등 추가");

    private final String category;
}
