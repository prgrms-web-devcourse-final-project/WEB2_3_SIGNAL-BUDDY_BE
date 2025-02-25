package org.programmers.signalbuddyfinal.domain.feedback_report.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.programmers.signalbuddyfinal.domain.feedback_report.dto.FeedbackReportResponse;
import org.programmers.signalbuddyfinal.domain.feedback_report.entity.FeedbackReport;
import org.programmers.signalbuddyfinal.domain.member.dto.MemberResponse;
import org.programmers.signalbuddyfinal.domain.member.entity.Member;

@Mapper
public interface FeedbackReportMapper {

    FeedbackReportMapper INSTANCE = Mappers.getMapper(FeedbackReportMapper.class);

    MemberResponse toMemberResponse(Member member);

    @Mapping( target = "feedbackId", expression = "java(report.getFeedback().getFeedbackId())")
    FeedbackReportResponse toResponse(FeedbackReport report);
}
