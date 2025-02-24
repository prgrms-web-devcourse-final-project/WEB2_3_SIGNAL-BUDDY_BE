package org.programmers.signalbuddyfinal.domain.feedback_report.service;

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
import org.programmers.signalbuddyfinal.domain.feedback_report.entity.enums.FeedbackReportCategory;
import org.programmers.signalbuddyfinal.domain.feedback_report.repository.FeedbackReportRepository;
import org.programmers.signalbuddyfinal.domain.member.entity.Member;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberRole;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberStatus;
import org.programmers.signalbuddyfinal.domain.member.repository.MemberRepository;
import org.programmers.signalbuddyfinal.global.dto.CustomUser2Member;
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
    private Feedback feedback;

    @BeforeEach
    void setup() {
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

    private Member saveMember(String email, String nickname) {
        return memberRepository.save(
            Member.builder().email(email).password("123456").role(MemberRole.USER)
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

    private CustomUser2Member getCurrentMember(Long id, MemberRole role) {
        return new CustomUser2Member(
            new CustomUserDetails(id, "", "",
                "", "", role, MemberStatus.ACTIVITY));
    }
}