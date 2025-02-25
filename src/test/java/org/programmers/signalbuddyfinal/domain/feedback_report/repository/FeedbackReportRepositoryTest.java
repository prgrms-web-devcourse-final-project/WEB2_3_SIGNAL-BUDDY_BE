package org.programmers.signalbuddyfinal.domain.feedback_report.repository;

import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Set;
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
import org.programmers.signalbuddyfinal.domain.feedback_report.dto.FeedbackReportResponse;
import org.programmers.signalbuddyfinal.domain.feedback_report.entity.FeedbackReport;
import org.programmers.signalbuddyfinal.domain.feedback_report.entity.enums.FeedbackReportCategory;
import org.programmers.signalbuddyfinal.domain.feedback_report.entity.enums.FeedbackReportStatus;
import org.programmers.signalbuddyfinal.domain.member.entity.Member;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberRole;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberStatus;
import org.programmers.signalbuddyfinal.domain.member.repository.MemberRepository;
import org.programmers.signalbuddyfinal.global.support.RepositoryTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

class FeedbackReportRepositoryTest extends RepositoryTest {

    @MockitoSpyBean
    private FeedbackReportRepository reportRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private CrossroadRepository crossroadRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Member member;
    private Feedback feedback;

    @BeforeEach
    void setup() {
        member = saveMember("test@test.com", "tester");
        Crossroad crossroad = saveCrossroad("12313", "00 사거리", 37.12, 127.12);
        feedback = saveFeedback("test", "test", member, crossroad);

        for (int i = 1; i <= 12; i++) {
            saveFeedbackReport("test " + i, member, feedback);
        }

        createFulltextIndex();
    }

    @DisplayName("검색어를 이용해 피드백 신고 데이터들을 가져온다.")
    @Test
    void findAllByFilterByKeyword() {
        // Given
        Pageable pageable = PageRequest.of(0, 7,
            Direction.DESC, "createdAt");
        String keyword = "test";

        // When
        Page<FeedbackReportResponse> actual = reportRepository.findAllByFilter(
            pageable, keyword, null, Collections.emptySet(), null, null
        );

        // Then
        SoftAssertions.assertSoftly(softAssertions -> {
            softAssertions.assertThat(actual.getTotalElements()).isEqualTo(12);
            softAssertions.assertThat(actual.getNumber()).isEqualTo(pageable.getPageNumber());
            softAssertions.assertThat(actual.getSize()).isEqualTo(pageable.getPageSize());
            softAssertions.assertThat(actual.getContent().get(4).getContent()).contains("test");
            softAssertions.assertThat(actual.getContent().get(4).getFeedbackId())
                .isEqualTo(feedback.getFeedbackId());
            softAssertions.assertThat(actual.getContent().get(4).getMember().getMemberId())
                .isEqualTo(feedback.getMember().getMemberId());
        });
    }

    @DisplayName("검색어와 신고 유형, 처리 상태를 이용해 피드백 신고 데이터들을 가져온다.")
    @Test
    void findAllByFilterByKeywordAndCategoryAndStatus() {
        // Given
        Pageable pageable = PageRequest.of(0, 7,
            Direction.DESC, "createdAt");
        String keyword = "test";

        FeedbackReport feedbackReport1 = saveFeedbackReport(
            "test 13", FeedbackReportCategory.FALSE, member, feedback
        );
        feedbackReport1.updateStatus(FeedbackReportStatus.REJECTED);
        FeedbackReport feedbackReport2 = saveFeedbackReport(
            "test 14", FeedbackReportCategory.OFFENSIVE, member, feedback
        );
        feedbackReport2.updateStatus(FeedbackReportStatus.PROCESSED);
        reportRepository.save(feedbackReport1);
        reportRepository.save(feedbackReport2);

        Set<FeedbackReportCategory> categories = Set.of(
            FeedbackReportCategory.FALSE, FeedbackReportCategory.OFFENSIVE
        );
        Set<FeedbackReportStatus> statuses = Set.of(
            FeedbackReportStatus.REJECTED, FeedbackReportStatus.PROCESSED
        );

        createFulltextIndex();

        // When
        Page<FeedbackReportResponse> actual = reportRepository.findAllByFilter(
            pageable, keyword, categories, statuses, null, null
        );

        // Then
        SoftAssertions.assertSoftly(softAssertions -> {
            softAssertions.assertThat(actual.getTotalElements()).isEqualTo(2);
            softAssertions.assertThat(actual.getNumber()).isEqualTo(pageable.getPageNumber());
            softAssertions.assertThat(actual.getSize()).isEqualTo(pageable.getPageSize());

            for (FeedbackReportResponse response : actual.getContent()) {
                if (FeedbackReportCategory.FALSE.equals(response.getCategory())) {
                    softAssertions.assertThat(response.getStatus())
                        .isEqualTo(FeedbackReportStatus.REJECTED);
                } else {
                    softAssertions.assertThat(response.getStatus())
                        .isEqualTo(FeedbackReportStatus.PROCESSED);
                }
            }
        });
    }

    @DisplayName("검색어와 날짜 범위를 지정하여 피드백 신고 데이터들을 가져온다. (쿼리만 확인)")
    @Test
    void findAllByFilterByKeywordAndDate() {
        // Given
        Pageable pageable = PageRequest.of(0, 7,
            Direction.DESC, "createdAt");
        String keyword = "test";
        LocalDate startDate = LocalDate.of(2024, 3, 15);
        LocalDate endDate = LocalDate.of(2024, 7, 15);

        // When & Then
        reportRepository.findAllByFilter(
            pageable, keyword,
            Collections.emptySet(), null, startDate, endDate
        );
        verify(reportRepository)
            .findAllByFilter(pageable, keyword, Collections.emptySet(), null, startDate, endDate);
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

    private void saveFeedbackReport(String content, Member member, Feedback feedback) {
        reportRepository.save(
            FeedbackReport.create().content(content).category(FeedbackReportCategory.ETC)
                .member(member).feedback(feedback).build());
    }

    private FeedbackReport saveFeedbackReport(
        String content, FeedbackReportCategory category,
        Member member, Feedback feedback
    ) {
        return reportRepository.save(FeedbackReport.create().content(content).category(category)
                .member(member).feedback(feedback).build());
    }

    private void createFulltextIndex() {
        jdbcTemplate.execute(
            "CREATE FULLTEXT INDEX IF NOT EXISTS idx_content " +
                "ON feedback_reports (content)"
        );
    }
}