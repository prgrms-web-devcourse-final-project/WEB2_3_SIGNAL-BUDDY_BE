package org.programmers.signalbuddyfinal.domain.feedback_report.repository;

import static org.programmers.signalbuddyfinal.domain.feedback_report.entity.QFeedbackReport.feedbackReport;
import static org.programmers.signalbuddyfinal.domain.member.entity.QMember.member;
import static org.programmers.signalbuddyfinal.global.util.QueryDslUtils.betweenDates;
import static org.programmers.signalbuddyfinal.global.util.QueryDslUtils.fulltextSearch;
import static org.programmers.signalbuddyfinal.global.util.QueryDslUtils.getOrderSpecifiers;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.QBean;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.programmers.signalbuddyfinal.domain.feedback_report.dto.FeedbackReportResponse;
import org.programmers.signalbuddyfinal.domain.feedback_report.entity.enums.FeedbackReportCategory;
import org.programmers.signalbuddyfinal.domain.feedback_report.entity.enums.FeedbackReportStatus;
import org.programmers.signalbuddyfinal.domain.member.dto.MemberResponse;
import org.programmers.signalbuddyfinal.global.constant.SearchTarget;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CustomFeedbackReportRepositoryImpl implements CustomFeedbackReportRepository {

    private static final QBean<MemberResponse> memberResponseDto = Projections.fields(
        MemberResponse.class, member.memberId, member.email, member.nickname,
        member.profileImageUrl, member.role, member.memberStatus
    );

    private static final QBean<FeedbackReportResponse> reportResponseDto = Projections.fields(
        FeedbackReportResponse.class, feedbackReport.feedbackReportId, feedbackReport.content,
        feedbackReport.category, feedbackReport.status, feedbackReport.processedAt,
        feedbackReport.createdAt, feedbackReport.updatedAt, feedbackReport.feedback.feedbackId,
        memberResponseDto.as("member")
    );

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Page<FeedbackReportResponse> findAllByFilter(
        Pageable pageable, SearchTarget target, String keyword,
        Set<FeedbackReportCategory> categories,
        Set<FeedbackReportStatus> statuses,
        LocalDate startDate, LocalDate endDate
    ) {
        BooleanExpression searchCondition = searchCondition(target, keyword);
        BooleanExpression betweenDates = betweenDates(feedbackReport.createdAt, startDate, endDate);
        BooleanExpression categoryCondition = categoryCondition(categories);
        BooleanExpression statusCondition = statusCondition(statuses);

        List<FeedbackReportResponse> results = jpaQueryFactory
            .select(reportResponseDto)
            .from(feedbackReport)
            .join(member).on(feedbackReport.member.eq(member)).fetchJoin()
            .where(
                searchCondition
                    .and(betweenDates)
                    .and(categoryCondition)
                    .and(statusCondition)
            )
            .offset(pageable.getOffset()).limit(pageable.getPageSize())
            .orderBy(getOrderSpecifiers(pageable, feedbackReport.getType(), "feedbackReport"))
            .fetch();

        long count = Optional.ofNullable(
            jpaQueryFactory
                .select(feedbackReport.count())
                .from(feedbackReport)
                .join(member).on(feedbackReport.member.eq(member)).fetchJoin()
                .where(
                    searchCondition
                        .and(betweenDates)
                        .and(categoryCondition)
                        .and(statusCondition)
                ).fetchOne()
        ).orElse(0L);

        return new PageImpl<>(results, pageable, count);
    }

    private BooleanExpression searchCondition(SearchTarget target, String keyword) {
        BooleanExpression fulltextSearch = Expressions.TRUE;
        if (SearchTarget.WRITER.equals(target)) {
            fulltextSearch = fulltextSearch(keyword, member.nickname);
        } else if (SearchTarget.SUBJECT_OR_CONTENT.equals(target)) {
            fulltextSearch = fulltextSearch(keyword, feedbackReport.content);
        }
        return fulltextSearch;
    }

    private BooleanExpression categoryCondition(Set<FeedbackReportCategory> categories) {
        if (categories == null || categories.isEmpty()) {
            return Expressions.TRUE;
        }

        BooleanExpression expression = null;
        for (FeedbackReportCategory category : categories) {
            if (expression == null) {
                expression = feedbackReport.category.eq(category);
            } else {
                expression = expression.or(feedbackReport.category.eq(category));
            }
        }
        return expression;
    }

    private BooleanExpression statusCondition(Set<FeedbackReportStatus> statuses) {
        if (statuses == null || statuses.isEmpty()) {
            return Expressions.TRUE;
        }

        BooleanExpression expression = null;
        for (FeedbackReportStatus status : statuses) {
            if (expression == null) {
                expression = feedbackReport.status.eq(status);
            } else {
                expression = expression.or(feedbackReport.status.eq(status));
            }
        }
        return expression;
    }
}
