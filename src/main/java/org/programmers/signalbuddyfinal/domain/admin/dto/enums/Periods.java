package org.programmers.signalbuddyfinal.domain.admin.dto.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Periods {

    TODAY("오늘"),THREE_DAYS("3일"),WEEK("일주일"),MONTH("1개월"),THREE_MONTH("3개월");

    private final String label;


}
