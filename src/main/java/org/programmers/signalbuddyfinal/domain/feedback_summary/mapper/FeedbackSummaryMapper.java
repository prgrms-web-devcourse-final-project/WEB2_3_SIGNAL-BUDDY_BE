package org.programmers.signalbuddyfinal.domain.feedback_summary.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.programmers.signalbuddyfinal.domain.feedback_summary.dto.FeedbackSummaryResponse;
import org.programmers.signalbuddyfinal.domain.feedback_summary.entity.FeedbackSummary;

@Mapper
public interface FeedbackSummaryMapper {

    FeedbackSummaryMapper INSTANCE = Mappers.getMapper(FeedbackSummaryMapper.class);

    FeedbackSummaryResponse toResponse(FeedbackSummary feedbackSummary);
}
