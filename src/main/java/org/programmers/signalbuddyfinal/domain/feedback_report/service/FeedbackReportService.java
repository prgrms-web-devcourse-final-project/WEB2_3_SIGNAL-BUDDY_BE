package org.programmers.signalbuddyfinal.domain.feedback_report.service;

import lombok.RequiredArgsConstructor;
import org.programmers.signalbuddyfinal.domain.feedback.entity.Feedback;
import org.programmers.signalbuddyfinal.domain.feedback.repository.FeedbackRepository;
import org.programmers.signalbuddyfinal.domain.feedback_report.dto.FeedbackReportRequest;
import org.programmers.signalbuddyfinal.domain.feedback_report.dto.FeedbackReportResponse;
import org.programmers.signalbuddyfinal.domain.feedback_report.dto.FeedbackReportUpdateRequest;
import org.programmers.signalbuddyfinal.domain.feedback_report.entity.FeedbackReport;
import org.programmers.signalbuddyfinal.domain.feedback_report.exception.FeedbackReportErrorCode;
import org.programmers.signalbuddyfinal.domain.feedback_report.mapper.FeedbackReportMapper;
import org.programmers.signalbuddyfinal.domain.feedback_report.repository.FeedbackReportRepository;
import org.programmers.signalbuddyfinal.domain.member.entity.Member;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberRole;
import org.programmers.signalbuddyfinal.domain.member.repository.MemberRepository;
import org.programmers.signalbuddyfinal.global.dto.CustomUser2Member;
import org.programmers.signalbuddyfinal.global.exception.BusinessException;
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

    @Transactional
    public void updateFeedbackReport(
        Long feedbackId, Long reportId,
        FeedbackReportUpdateRequest request,
        CustomUser2Member user
    ) {
        // 관리자만 복구 가능
        isNotAdmin(user);

        FeedbackReport report = reportRepository.findByIdOrThrow(reportId);
        verifyFeedbackAndReport(feedbackId, report);

        report.updateStatus(request.getStatus());
    }

    @Transactional
    public void deleteFeedbackReport(
        Long feedbackId, Long reportId,
        CustomUser2Member user
    ) {
        // 관리자만 삭제 가능
        isNotAdmin(user);

        FeedbackReport report = reportRepository.findByIdOrThrow(reportId);
        verifyFeedbackAndReport(feedbackId, report);

        reportRepository.deleteById(reportId);
    }

    // 관리자만 접근 가능
    private void isNotAdmin(CustomUser2Member user) {
        if (!MemberRole.ADMIN.equals(user.getRole())) {
            throw new BusinessException(
                FeedbackReportErrorCode.REQUEST_NOT_AUTHORIZED
            );
        }
    }

    // 요청 들어온 신고와 피드백이 잘못 매칭됐는지 검사
    private void verifyFeedbackAndReport(Long feedbackId, FeedbackReport report) {
        if (!feedbackId.equals(report.getFeedback().getFeedbackId())) {
            throw new BusinessException(FeedbackReportErrorCode.FEEDBACK_REPORT_MISMATCH);
        }
    }
}
