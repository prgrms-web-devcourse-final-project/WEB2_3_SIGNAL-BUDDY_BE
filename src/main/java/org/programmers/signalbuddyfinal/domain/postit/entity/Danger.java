package org.programmers.signalbuddyfinal.domain.postit.entity;

import lombok.Getter;

@Getter
public enum Danger {

    DANGER("위험"), WARNING("경고"), NOTICE("일반");

    private final String danger;

    Danger(String danger) {
        this.danger = danger;
    }
}
