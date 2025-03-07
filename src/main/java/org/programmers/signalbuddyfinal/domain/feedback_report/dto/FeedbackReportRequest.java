package org.programmers.signalbuddyfinal.domain.feedback_report.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.programmers.signalbuddyfinal.domain.feedback_report.entity.enums.FeedbackReportCategory;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class FeedbackReportRequest {

    @NotBlank(message = "피드백 신고 내용은 비어있을 수 없습니다.")
    private String content;

    @NotNull(message = "피드백 신고 유형은 비어있을 수 없습니다.")
    private FeedbackReportCategory category;
}
