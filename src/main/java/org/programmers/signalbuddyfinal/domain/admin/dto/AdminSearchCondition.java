package org.programmers.signalbuddyfinal.domain.admin.dto;

import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AdminSearchCondition {

    private final LocalDate startDate;

    private final LocalDate endDate;

    private final Boolean deleted;
}
