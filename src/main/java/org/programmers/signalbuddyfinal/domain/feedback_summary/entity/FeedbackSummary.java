package org.programmers.signalbuddyfinal.domain.feedback_summary.entity;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.programmers.signalbuddyfinal.domain.basetime.BaseTimeEntity;

@Entity(name="feedback_summary")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FeedbackSummary extends BaseTimeEntity {

    @Id
    private LocalDate date;  // 집계 날짜 (PK)

    @Column(nullable = false)
    private Long todayCount;  // 당일 피드백 건수

    @Type(JsonType.class)
    @Column(columnDefinition = "json")
    private List<FeedbackCategoryCount> categoryRanks;  // 피드백 유형별 건수 순위

    @Type(JsonType.class)
    @Column(columnDefinition = "json")
    private List<CrossroadFeedbackCount> crossroadRanks;  // 가장 많은 피드백을 받은 교차로 순위

    @Builder(builderMethodName = "create")
    private FeedbackSummary(
        final Long todayCount, final List<FeedbackCategoryCount> categoryRanks,
        final List<CrossroadFeedbackCount> crossroadRanks
    ) {
        this.date = LocalDate.now();
        this.todayCount = Objects.requireNonNull(todayCount);
        this.categoryRanks = categoryRanks;
        this.crossroadRanks = crossroadRanks;
    }

    public void updateTodayCount(Long todayCount) {
        if (!this.todayCount.equals(todayCount)) {
            this.todayCount = todayCount;
        }
    }

    public void updateCategoryRanks(List<FeedbackCategoryCount> categoryRanks) {
        if (!Objects.equals(this.categoryRanks, categoryRanks)) {
            this.categoryRanks = new ArrayList<>(categoryRanks);
        }
    }

    public void updateCrossroadRanks(List<CrossroadFeedbackCount> crossroadRanks) {
        if (!Objects.equals(this.crossroadRanks, crossroadRanks)) {
            this.crossroadRanks = new ArrayList<>(crossroadRanks);
        }
    }
}