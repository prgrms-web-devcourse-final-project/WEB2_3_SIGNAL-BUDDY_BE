package org.programmers.signalbuddyfinal.domain.feedback_summary.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.programmers.signalbuddyfinal.domain.feedback_summary.entity.CrossroadFeedbackCount;
import org.programmers.signalbuddyfinal.domain.feedback_summary.entity.FeedbackCategoryCount;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class FeedbackSummaryResponse {

    private LocalDate date;

    private Long todayCount;

    private List<FeedbackCategoryCount> categoryRanks;

    private List<CrossroadFeedbackCount> crossroadRanks;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
