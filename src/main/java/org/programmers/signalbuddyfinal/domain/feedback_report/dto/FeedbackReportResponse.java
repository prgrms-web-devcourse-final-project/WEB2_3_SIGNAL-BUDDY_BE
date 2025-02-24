package org.programmers.signalbuddyfinal.domain.feedback_report.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.programmers.signalbuddyfinal.domain.feedback_report.entity.enums.FeedbackReportCategory;
import org.programmers.signalbuddyfinal.domain.member.dto.MemberResponse;

@Getter
@Builder
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class FeedbackReportResponse {

    private Long feedbackReportId;

    private String content;

    private FeedbackReportCategory category;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Long feedbackId;

    private MemberResponse member;
}
