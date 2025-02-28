package org.programmers.signalbuddyfinal.domain.feedback_summary.repository;

import static org.programmers.signalbuddyfinal.domain.crossroad.entity.QCrossroad.crossroad;
import static org.programmers.signalbuddyfinal.domain.feedback.entity.QFeedback.feedback;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.QBean;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.programmers.signalbuddyfinal.domain.feedback_summary.entity.CrossroadFeedbackCount;
import org.programmers.signalbuddyfinal.domain.feedback_summary.entity.FeedbackCategoryCount;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CustomFeedbackSummaryRepositoryImpl implements CustomFeedbackSummaryRepository {

    private static final QBean<CrossroadFeedbackCount> CROSSROAD_FEEDBACK_COUNT = Projections.fields(
        CrossroadFeedbackCount.class, crossroad.crossroadId, crossroad.name,
        feedback.count().as("count")
    );

    private static final QBean<FeedbackCategoryCount> FEEDBACK_CATEGORY_COUNT = Projections.fields(
        FeedbackCategoryCount.class, feedback.category, feedback.count().as("count")
    );

    private static final int MAX_RANK =  20;

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<CrossroadFeedbackCount> countFeedbackOnCrossroadByDate(LocalDate date) {
        return jpaQueryFactory
            .select(CROSSROAD_FEEDBACK_COUNT)
            .from(feedback)
            .join(crossroad).on(feedback.crossroad.eq(crossroad))
            .where(dateCondition(date))
            .groupBy(crossroad.crossroadId)
            .having(feedback.count().goe(1))
            .orderBy(feedback.count().desc())
            .limit(MAX_RANK)
            .fetch();
    }

    @Override
    public List<FeedbackCategoryCount> countFeedbackCategoryByDate(LocalDate date) {
        return jpaQueryFactory
            .select(FEEDBACK_CATEGORY_COUNT)
            .from(feedback)
            .where(dateCondition(date))
            .groupBy(feedback.category)
            .having(feedback.count().goe(1))
            .orderBy(feedback.count().desc())
            .limit(MAX_RANK)
            .fetch();
    }

    @Override
    public long countFeedbackByDate(LocalDate date) {
        return Optional.ofNullable(jpaQueryFactory
            .select(feedback.count())
            .from(feedback)
            .where(dateCondition(date))
            .fetchOne()
        ).orElse(0L);
    }

    /**
     * 피드백 생성일에서 특정 날짜만 설정
     * 
     * @param date 특정 날짜
     * @return BooleanExpression
     */
    private BooleanExpression dateCondition(LocalDate date) {
        if (date == null) {
            return feedback.createdAt.goe(LocalDate.now().atStartOfDay())
                .and(feedback.createdAt.lt(LocalDate.now().plusDays(1).atStartOfDay()));
        }
        return feedback.createdAt.goe(date.atStartOfDay())
            .and(feedback.createdAt.lt(date.plusDays(1).atStartOfDay()));
    }
}
