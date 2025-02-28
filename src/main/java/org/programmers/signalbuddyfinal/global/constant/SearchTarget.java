package org.programmers.signalbuddyfinal.global.constant;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 검색 타겟(범위) 설정
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum SearchTarget {

    SUBJECT_OR_CONTENT("내용 (+ 제목)", "content"),
    WRITER("작성자", "writer");

    private final String message;
    private final String value;
}
