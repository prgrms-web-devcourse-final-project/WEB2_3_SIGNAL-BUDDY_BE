package org.programmers.signalbuddyfinal.domain.feedback_report.service;

import lombok.RequiredArgsConstructor;
import org.programmers.signalbuddyfinal.domain.feedback.entity.Feedback;
import org.programmers.signalbuddyfinal.domain.feedback.repository.FeedbackRepository;
import org.programmers.signalbuddyfinal.domain.feedback_report.dto.FeedbackReportRequest;
import org.programmers.signalbuddyfinal.domain.feedback_report.dto.FeedbackReportResponse;
import org.programmers.signalbuddyfinal.domain.feedback_report.entity.FeedbackReport;
import org.programmers.signalbuddyfinal.domain.feedback_report.mapper.FeedbackReportMapper;
import org.programmers.signalbuddyfinal.domain.feedback_report.repository.FeedbackReportRepository;
import org.programmers.signalbuddyfinal.domain.member.entity.Member;
import org.programmers.signalbuddyfinal.domain.member.repository.MemberRepository;
import org.programmers.signalbuddyfinal.global.dto.CustomUser2Member;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FeedbackReportService {

    private final FeedbackReportRepository reportRepository;
    private final FeedbackRepository feedbackRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public FeedbackReportResponse writeFeedbackReport(
        Long feedbackId, FeedbackReportRequest request,
        CustomUser2Member user
    ) {
        Feedback feedback = feedbackRepository.findByIdOrThrow(feedbackId);
        Member member = memberRepository.findByIdOrThrow(user.getMemberId());

        FeedbackReport report = FeedbackReport.create()
            .content(request.getContent()).category(request.getCategory())
            .feedback(feedback).member(member)
            .build();
        report = reportRepository.save(report);

        return FeedbackReportMapper.INSTANCE.toResponse(report);
    }
}
