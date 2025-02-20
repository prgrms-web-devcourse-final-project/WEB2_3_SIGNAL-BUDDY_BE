package org.programmers.signalbuddyfinal.domain.feedback.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.programmers.signalbuddyfinal.domain.crossroad.dto.CrossroadApiResponse;
import org.programmers.signalbuddyfinal.domain.crossroad.entity.Crossroad;
import org.programmers.signalbuddyfinal.domain.crossroad.repository.CrossroadRepository;
import org.programmers.signalbuddyfinal.domain.feedback.dto.FeedbackResponse;
import org.programmers.signalbuddyfinal.domain.feedback.entity.Feedback;
import org.programmers.signalbuddyfinal.domain.feedback.entity.enums.FeedbackCategory;
import org.programmers.signalbuddyfinal.domain.feedback.repository.FeedbackRepository;
import org.programmers.signalbuddyfinal.domain.member.entity.Member;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberRole;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberStatus;
import org.programmers.signalbuddyfinal.domain.member.repository.MemberRepository;
import org.programmers.signalbuddyfinal.global.support.ServiceTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

class FeedbackServiceTest extends ServiceTest {

    @Autowired
    private FeedbackService feedbackService;

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private CrossroadRepository crossroadRepository;

    @Autowired
    private MemberRepository memberRepository;

    private Member member;

    @BeforeEach
    void setup() {
        member = saveMember("test@test.com", "tester");
        Crossroad crossroad = saveCrossroad("13214", "00사거리", 37.12222, 127.12132);

        saveFeedback("test subject", "test content", member, crossroad);
        saveFeedback("test subject2", "test content2", member, crossroad);
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

    private void saveFeedback(String subject, String content, Member member, Crossroad crossroad) {
        feedbackRepository.save(
            Feedback.create().subject(subject).content(content).secret(Boolean.FALSE)
                .category(FeedbackCategory.ETC).member(member).crossroad(crossroad).build());
    }

    @DisplayName("사용자가 작성한 피드백 목록 조회")
    @Test
    void getFeedbacksByMember() {
        // feedbackNoMemberDto
        final Page<FeedbackResponse> feedbacks = feedbackService.findPagedFeedbacksByMember(
            member.getMemberId(), Pageable.ofSize(10));

        assertThat(feedbacks).isNotNull();
        assertThat(feedbacks.getTotalElements()).isEqualTo(2);
        assertThat(feedbacks.getTotalPages()).isEqualTo(1);

        // member 필드가 null인지 확인
        assertThat(feedbacks.getContent()).isNotEmpty()
            .allSatisfy(feedback -> assertThat(feedback.getMember()).isNull());
    }
}