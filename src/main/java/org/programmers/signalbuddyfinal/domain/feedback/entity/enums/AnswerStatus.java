package org.programmers.signalbuddyfinal.domain.feedback.entity.enums;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum AnswerStatus {

    BEFORE("답변 전", "before"),
    COMPLETION("답변 완료", "completion");

    private final String message;
    private final String value;
}
