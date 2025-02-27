package org.programmers.signalbuddyfinal.domain.feedback_summary.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
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
import org.programmers.signalbuddyfinal.domain.feedback_summary.entity.CrossroadFeedbackCount;
import org.programmers.signalbuddyfinal.domain.feedback_summary.entity.FeedbackCategoryCount;
import org.programmers.signalbuddyfinal.domain.member.entity.Member;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberRole;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberStatus;
import org.programmers.signalbuddyfinal.domain.member.repository.MemberRepository;
import org.programmers.signalbuddyfinal.global.support.ServiceTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class CustomFeedbackSummaryRepositoryImplTest extends ServiceTest {

    @Autowired
    private FeedbackSummaryRepository feedbackSummaryRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private CrossroadRepository crossroadRepository;

    @BeforeEach
    void setUp() {
        Member member = saveMember("test@test.com", "tester");
        Crossroad crossroad1 = saveCrossroad("1321", "00 사거리", 37.12123, 127.1231);
        Crossroad crossroad2 = saveCrossroad("132122", "001 사거리", 37.121123, 127.1251);
        Crossroad crossroad3 = saveCrossroad("133321", "002 사거리", 37.12323, 127.1221);

        for (int i = 1; i <= 6; i++) {
            if (i == 1) {
                saveFeedback(
                    "subject " + i, "content " + i, FeedbackCategory.ETC,
                    member, crossroad3
                );
                continue;
            }

            if (i % 3 == 0) {
                saveFeedback(
                    "subject " + i, "content " + i, FeedbackCategory.DELAY,
                    member, crossroad1
                );
            }

            if (i % 2 == 0) {
                saveFeedback(
                    "subject " + i, "content " + i, FeedbackCategory.ADD_SIGNAL,
                    member, crossroad2
                );
            }
        }
    }

    @DisplayName("특정 날짜의 교차로 별 피드백 개수 순위를 집계한다.")
    @Test
    void countFeedbackOnCrossroadByDate() {
        // When
        List<CrossroadFeedbackCount> actual = feedbackSummaryRepository.countFeedbackOnCrossroadByDate(LocalDate.now());

        // Then
        SoftAssertions.assertSoftly(softAssertions -> {
            softAssertions.assertThat(actual).hasSize(3);
            softAssertions.assertThat(actual.get(0).getCount()).isEqualTo(3);
            softAssertions.assertThat(actual.get(0).getCrossroadId()).isEqualTo(2L);
            softAssertions.assertThat(actual.get(0).getName()).isEqualTo("001 사거리");
            for (CrossroadFeedbackCount rank : actual) {
                System.out.println(rank);
            }
        });
    }

    @DisplayName("특정 날짜의 피드백 유형 별 개수 순위를 집계한다.")
    @Test
    void countFeedbackCategoryByDate() {
        // When
        List<FeedbackCategoryCount> actual = feedbackSummaryRepository.countFeedbackCategoryByDate(LocalDate.now());

        // Then
        SoftAssertions.assertSoftly(softAssertions -> {
            softAssertions.assertThat(actual).hasSize(3);
            softAssertions.assertThat(actual.get(0).getCount()).isEqualTo(3);
            softAssertions.assertThat(actual.get(0).getCategory()).isEqualTo(FeedbackCategory.ADD_SIGNAL);
            for (FeedbackCategoryCount rank : actual) {
                System.out.println(rank);
            }
        });
    }

    @DisplayName("특정 날짜의 피드백 개수를 가져온다.")
    @Test
    void countFeedbackByDate() {
        // When & Then
        assertThat(
            feedbackSummaryRepository.countFeedbackByDate(LocalDate.now())
        ).isEqualTo(6L);

    }

    private Member saveMember(String email, String nickname) {
        return memberRepository.saveAndFlush(
            Member.builder().email(email).password("123456").role(MemberRole.USER)
                .nickname(nickname).memberStatus(MemberStatus.ACTIVITY)
                .profileImageUrl("https://test-image.com/test-123131").build());
    }

    private Crossroad saveCrossroad(String apiId, String name, double lat, double lng) {
        return crossroadRepository.saveAndFlush(new Crossroad(
            CrossroadApiResponse.builder().crossroadApiId(apiId).name(name).lat(lat).lng(lng)
                .build()));
    }

    private Feedback saveFeedback(
        String subject, String content, FeedbackCategory category,
        Member member, Crossroad crossroad
    ) {
        return feedbackRepository.saveAndFlush(
            Feedback.create().subject(subject).content(content).secret(Boolean.FALSE)
                .category(category).member(member).crossroad(crossroad).build());
    }
}