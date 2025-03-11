package org.programmers.signalbuddyfinal.domain.term.entity.enums;

import lombok.Getter;

@Getter
public enum TermCategory {
    USE("이용 약관"),
    PRIVACY("개인정보 처리방침");

    private final String name;

    TermCategory(String name) {
        this.name = name;
    }
}
