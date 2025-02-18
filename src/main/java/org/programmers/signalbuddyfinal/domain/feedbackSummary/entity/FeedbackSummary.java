package org.programmers.signalbuddyfinal.domain.feedbackSummary.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity(name="feedback_summary")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FeedbackSummary {

    @Id
    @Column(nullable = false)
    private LocalDate date;  // 집계 날짜 (PK)

    @Column(nullable = false)
    private Long todayCount;  // 당일 피드백 건수

    @Column(columnDefinition = "TEXT")
    private String categoryCount;  // 피드백 유형별 건수 (JSON)

    @Column(columnDefinition = "TEXT")
    private String rank;  // 가장 많은 피드백을 받은 교차로 순위 (JSON)
}