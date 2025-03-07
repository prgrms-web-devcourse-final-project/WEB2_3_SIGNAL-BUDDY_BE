package org.programmers.signalbuddyfinal.domain.feedback_summary.service;

import static org.mockito.BDDMockito.given;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.programmers.signalbuddyfinal.domain.feedback.entity.enums.FeedbackCategory;
import org.programmers.signalbuddyfinal.domain.feedback_summary.dto.FeedbackSummaryResponse;
import org.programmers.signalbuddyfinal.domain.feedback_summary.entity.CrossroadFeedbackCount;
import org.programmers.signalbuddyfinal.domain.feedback_summary.entity.FeedbackCategoryCount;
import org.programmers.signalbuddyfinal.domain.feedback_summary.entity.FeedbackSummary;
import org.programmers.signalbuddyfinal.domain.feedback_summary.repository.FeedbackSummaryRepository;
import org.programmers.signalbuddyfinal.global.support.ServiceTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

class FeedbackSummaryServiceTest extends ServiceTest {

    @Autowired
    private FeedbackSummaryService feedbackSummaryService;

    @MockitoBean
    private FeedbackSummaryRepository feedbackSummaryRepository;

    @DisplayName("")
    @Test
    void getFeedbackSummary() {
        // Given
        LocalDate date = LocalDate.now();
        Long todayCount = 6L;

        List<FeedbackCategoryCount> categoryRanks = new ArrayList<>();
        categoryRanks.add(
            FeedbackCategoryCount.builder()
                .category(FeedbackCategory.ETC).count(2L)
                .build()
        );
        categoryRanks.add(
            FeedbackCategoryCount.builder()
                .category(FeedbackCategory.DELAY).count(2L)
                .build()
        );
        categoryRanks.add(
            FeedbackCategoryCount.builder()
                .category(FeedbackCategory.ADD_SIGNAL).count(2L)
                .build()
        );
        List<CrossroadFeedbackCount> crossroadRanks = new ArrayList<>();
        for (int i = 1; i < 3; i++) {
            crossroadRanks.add(
                CrossroadFeedbackCount.builder()
                    .crossroadId((long) i).name("00 사거리 - " + i).count((long) i)
                    .build()
            );
        }

        FeedbackSummary entity = FeedbackSummary.create()
            .todayCount(todayCount).categoryRanks(categoryRanks).crossroadRanks(crossroadRanks)
            .build();

        given(feedbackSummaryRepository.findByIdOrThrow(date)).willReturn(entity);

        // When
        FeedbackSummaryResponse response = feedbackSummaryService.getFeedbackSummary(date);

        // Then
        SoftAssertions.assertSoftly(softAssertions -> {
            softAssertions.assertThat(response.getDate()).isEqualTo(date);
            softAssertions.assertThat(response.getTodayCount()).isEqualTo(todayCount);
            softAssertions.assertThat(response.getCategoryRanks()).isEqualTo(categoryRanks);
            softAssertions.assertThat(response.getCrossroadRanks()).isEqualTo(crossroadRanks);
        });
    }
}