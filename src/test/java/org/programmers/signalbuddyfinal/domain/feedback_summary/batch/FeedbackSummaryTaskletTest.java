package org.programmers.signalbuddyfinal.domain.feedback_summary.batch;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
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
import org.programmers.signalbuddyfinal.domain.feedback_summary.entity.FeedbackSummary;
import org.programmers.signalbuddyfinal.domain.feedback_summary.repository.FeedbackSummaryRepository;
import org.programmers.signalbuddyfinal.domain.member.entity.Member;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberRole;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberStatus;
import org.programmers.signalbuddyfinal.domain.member.repository.MemberRepository;
import org.programmers.signalbuddyfinal.global.support.BatchTest;
import org.springframework.beans.factory.annotation.Autowired;

class FeedbackSummaryTaskletTest extends BatchTest {

    @Autowired
    private FeedbackSummaryTasklet feedbackSummaryTasklet;

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

    @DisplayName("FeedbackSummaryTasklet가 피드백 집계를 수행한다.")
    @Test
    void execute() {
        // When
        try {
            feedbackSummaryTasklet.execute(null, null);
        } catch (Exception e) {
            assertThat(e).isNull();
        }

        FeedbackSummary feedbackSummary = feedbackSummaryRepository.findByIdOrThrow(LocalDate.now());
        SoftAssertions.assertSoftly(softAssertions -> {
            softAssertions.assertThat(feedbackSummary.getDate()).isNotNull();
            softAssertions.assertThat(feedbackSummary.getTodayCount()).isEqualTo(6L);
            softAssertions.assertThat(feedbackSummary.getCategoryRanks().get(1).getCategory())
                .isEqualTo(FeedbackCategory.DELAY);
            softAssertions.assertThat(feedbackSummary.getCrossroadRanks().get(1).getCrossroadId())
                .isEqualTo(1L);
            softAssertions.assertThat(feedbackSummary.getCrossroadRanks().get(1).getCount())
                .isEqualTo(2L);
        });
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