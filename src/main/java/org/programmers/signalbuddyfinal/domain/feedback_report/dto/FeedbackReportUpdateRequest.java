package org.programmers.signalbuddyfinal.domain.feedback_report.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.programmers.signalbuddyfinal.domain.feedback_report.entity.enums.FeedbackReportStatus;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class FeedbackReportUpdateRequest {

    @NotNull(message = "피드백 신고의 상태 값은 비어있을 수 없습니다.")
    private FeedbackReportStatus status;
}
