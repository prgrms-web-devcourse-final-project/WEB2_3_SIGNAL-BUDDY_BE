package org.programmers.signalbuddyfinal.domain.feedback_report.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.programmers.signalbuddyfinal.domain.crossroad.dto.CrossroadApiResponse;
import org.programmers.signalbuddyfinal.domain.crossroad.entity.Crossroad;
import org.programmers.signalbuddyfinal.domain.crossroad.repository.CrossroadRepository;
import org.programmers.signalbuddyfinal.domain.feedback.entity.Feedback;
import org.programmers.signalbuddyfinal.domain.feedback.entity.enums.FeedbackCategory;
import org.programmers.signalbuddyfinal.domain.feedback.repository.FeedbackRepository;
import org.programmers.signalbuddyfinal.domain.feedback_report.dto.FeedbackReportRequest;
import org.programmers.signalbuddyfinal.domain.feedback_report.dto.FeedbackReportResponse;
import org.programmers.signalbuddyfinal.domain.feedback_report.dto.FeedbackReportUpdateRequest;
import org.programmers.signalbuddyfinal.domain.feedback_report.entity.FeedbackReport;
import org.programmers.signalbuddyfinal.domain.feedback_report.entity.enums.FeedbackReportCategory;
import org.programmers.signalbuddyfinal.domain.feedback_report.entity.enums.FeedbackReportStatus;
import org.programmers.signalbuddyfinal.domain.feedback_report.exception.FeedbackReportErrorCode;
import org.programmers.signalbuddyfinal.domain.feedback_report.repository.FeedbackReportRepository;
import org.programmers.signalbuddyfinal.domain.member.entity.Member;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberRole;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberStatus;
import org.programmers.signalbuddyfinal.domain.member.repository.MemberRepository;
import org.programmers.signalbuddyfinal.global.dto.CustomUser2Member;
import org.programmers.signalbuddyfinal.global.exception.BusinessException;
import org.programmers.signalbuddyfinal.global.security.basic.CustomUserDetails;
import org.programmers.signalbuddyfinal.global.support.ServiceTest;
import org.springframework.beans.factory.annotation.Autowired;

class FeedbackReportServiceTest extends ServiceTest {

    @Autowired
    private FeedbackReportService reportService;

    @Autowired
    private FeedbackReportRepository reportRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private CrossroadRepository crossroadRepository;

    private Member member;
    private Member admin;
    private Feedback feedback;

    @BeforeEach
    void setup() {
        admin = saveAdmin("admin@test.com", "admin");
        member = saveMember("test@test.com", "tester");
        Crossroad crossroad = saveCrossroad("12313", "00 사거리", 37.12, 127.12);
        feedback = saveFeedback("test", "test", member, crossroad);
    }

    @DisplayName("피드백 신고를 작성한다.")
    @Test
    void writeFeedbackReport() {
        // Given
        Long feedbackId = feedback.getFeedbackId();
        String content = "test report";
        FeedbackReportRequest request = FeedbackReportRequest.builder()
            .content(content).category(FeedbackReportCategory.ETC)
            .build();
        Member requestMember = saveMember("request@test.com", "request");
        CustomUser2Member user = getCurrentMember(requestMember.getMemberId(), MemberRole.USER);

        // When
        FeedbackReportResponse actual = reportService.writeFeedbackReport(feedbackId, request, user);

        // Then
        SoftAssertions.assertSoftly(softAssertions -> {
            softAssertions.assertThat(actual.getFeedbackReportId()).isNotNull();
            softAssertions.assertThat(actual.getContent()).isEqualTo(content);
            softAssertions.assertThat(actual.getFeedbackId()).isEqualTo(feedbackId);
            softAssertions.assertThat(actual.getMember().getMemberId())
                .isEqualTo(requestMember.getMemberId());
        });
    }

    @DisplayName("관리자가 피드백 신고를 처리 상태로 변경한다.")
    @Test
    void updateFeedbackReportByAdmin_Success() {
        // Given
        Long feedbackId = feedback.getFeedbackId();
        FeedbackReport report = saveFeedbackReport("test", member, feedback);
        Long reportId = report.getFeedbackReportId();

        FeedbackReportUpdateRequest request = FeedbackReportUpdateRequest.builder()
            .status(FeedbackReportStatus.PROCESSED).build();
        CustomUser2Member user = getCurrentMember(admin.getMemberId(), MemberRole.ADMIN);

        // When
        reportService.updateFeedbackReport(feedbackId, reportId, request, user);

        // Then
        FeedbackReport actual = reportRepository.findByIdOrThrow(reportId);
        SoftAssertions.assertSoftly(softAssertions -> {
            softAssertions.assertThat(actual.getFeedbackReportId()).isEqualTo(reportId);
            softAssertions.assertThat(actual.getFeedback().getFeedbackId()).isEqualTo(feedbackId);
            softAssertions.assertThat(actual.getStatus()).isEqualTo(FeedbackReportStatus.PROCESSED);
            softAssertions.assertThat(actual.getProcessedAt()).isNotNull();
        });
    }

    @DisplayName("관리자가 피드백 신고를 처리한다.")
    @Test
    void deleteFeedbackReportByAdmin_Success() {
        // Given
        Long feedbackId = feedback.getFeedbackId();
        FeedbackReport report = saveFeedbackReport("test", member, feedback);
        CustomUser2Member user = getCurrentMember(admin.getMemberId(), MemberRole.ADMIN);

        // When
        reportService.deleteFeedbackReport(feedbackId, report.getFeedbackReportId(), user);

        // Then
        assertThat(reportRepository.findById(report.getFeedbackReportId())).isEmpty();
    }

    @DisplayName("일반 사용자가 피드백 신고를 삭제 시도하여 실패한다.")
    @Test
    void deleteFeedbackReportByUser_Failure() {
        // Given
        Long feedbackId = feedback.getFeedbackId();
        FeedbackReport report = saveFeedbackReport("test", member, feedback);
        CustomUser2Member user = getCurrentMember(member.getMemberId(), MemberRole.USER);

        // When & Then
        try {
            reportService.deleteFeedbackReport(feedbackId, report.getFeedbackReportId(), user);
        } catch (BusinessException e) {
            assertThat(e.getErrorCode())
                .isEqualTo(FeedbackReportErrorCode.REQUEST_NOT_AUTHORIZED);
        }
    }

    @DisplayName("피드백 ID와 신고 ID가 잘못 매칭되어 요청이 들어와 실패한다.")
    @Test
    void deleteFeedbackReport_Failure() {
        // Given
        Long feedbackId = 99999999999L;
        FeedbackReport report = saveFeedbackReport("test", member, feedback);
        CustomUser2Member user = getCurrentMember(admin.getMemberId(), MemberRole.ADMIN);

        // When & Then
        try {
            reportService.deleteFeedbackReport(feedbackId, report.getFeedbackReportId(), user);
        } catch (BusinessException e) {
            assertThat(e.getErrorCode())
                .isEqualTo(FeedbackReportErrorCode.FEEDBACK_REPORT_MISMATCH);
        }
    }

    private Member saveMember(String email, String nickname) {
        return memberRepository.save(
            Member.builder().email(email).password("123456").role(MemberRole.USER)
                .nickname(nickname).memberStatus(MemberStatus.ACTIVITY)
                .profileImageUrl("https://test-image.com/test-123131").build());
    }

    private Member saveAdmin(String email, String nickname) {
        return memberRepository.save(
            Member.builder().email(email).password("123456").role(MemberRole.ADMIN)
                .nickname(nickname).memberStatus(MemberStatus.ACTIVITY)
                .profileImageUrl("https://test-image.com/test-123131").build());
    }

    private Crossroad saveCrossroad(String apiId, String name, double lat, double lng) {
        return crossroadRepository.save(new Crossroad(
            CrossroadApiResponse.builder().crossroadApiId(apiId).name(name).lat(lat).lng(lng)
                .build()));
    }

    private Feedback saveFeedback(String subject, String content, Member member, Crossroad crossroad) {
        return feedbackRepository.save(
            Feedback.create().subject(subject).content(content).secret(Boolean.FALSE)
                .category(FeedbackCategory.ETC).member(member).crossroad(crossroad).build());
    }

    private FeedbackReport saveFeedbackReport(String content, Member member, Feedback feedback) {
        return reportRepository.save(
            FeedbackReport.create().content(content).category(FeedbackReportCategory.ETC)
                .member(member).feedback(feedback).build());
    }

    private CustomUser2Member getCurrentMember(Long id, MemberRole role) {
        return new CustomUser2Member(
            new CustomUserDetails(id, "", "",
                "", "", role, MemberStatus.ACTIVITY));
    }
}