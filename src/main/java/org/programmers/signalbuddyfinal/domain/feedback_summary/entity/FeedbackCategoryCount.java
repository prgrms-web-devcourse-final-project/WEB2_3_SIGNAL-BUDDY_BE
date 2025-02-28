package org.programmers.signalbuddyfinal.domain.feedback_summary.entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.programmers.signalbuddyfinal.domain.feedback.entity.enums.FeedbackCategory;

@Getter
@Builder
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class FeedbackCategoryCount {

    private FeedbackCategory category;

    private Long count;
}
