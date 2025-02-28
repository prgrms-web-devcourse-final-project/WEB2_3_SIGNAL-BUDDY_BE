package org.programmers.signalbuddyfinal.domain.feedback.repository;

import static org.programmers.signalbuddyfinal.domain.crossroad.entity.QCrossroad.crossroad;
import static org.programmers.signalbuddyfinal.domain.feedback.entity.QFeedback.feedback;
import static org.programmers.signalbuddyfinal.domain.member.entity.QMember.member;
import static org.programmers.signalbuddyfinal.global.util.QueryDSLUtil.betweenDates;
import static org.programmers.signalbuddyfinal.global.util.QueryDSLUtil.fulltextSearch;
import static org.programmers.signalbuddyfinal.global.util.QueryDSLUtil.getOrderSpecifiers;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
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
import org.programmers.signalbuddyfinal.domain.feedback.dto.FeedbackResponse;
import org.programmers.signalbuddyfinal.domain.feedback.entity.Feedback;
import org.programmers.signalbuddyfinal.domain.feedback.entity.enums.AnswerStatus;
import org.programmers.signalbuddyfinal.domain.feedback.entity.enums.FeedbackCategory;
import org.programmers.signalbuddyfinal.domain.feedback.exception.FeedbackErrorCode;
import org.programmers.signalbuddyfinal.domain.member.dto.MemberResponse;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberStatus;
import org.programmers.signalbuddyfinal.global.constant.SearchTarget;
import org.programmers.signalbuddyfinal.global.exception.BusinessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CustomFeedbackRepositoryImpl implements CustomFeedbackRepository {

    private static final QBean<MemberResponse> memberResponseDto = Projections.fields(
        MemberResponse.class, member.memberId, member.email, member.nickname,
        member.profileImageUrl, member.role, member.memberStatus);

    private static final QBean<FeedbackResponse> feedbackResponseDto = Projections.fields(
        FeedbackResponse.class, feedback.feedbackId, feedback.subject, feedback.content,
        feedback.category, feedback.imageUrl, feedback.likeCount, feedback.secret,
        feedback.answerStatus, memberResponseDto.as("member"),
        feedback.createdAt, feedback.updatedAt, feedback.deletedAt);

    private static final QBean<FeedbackResponse> feedbackNoMemberDto = Projections.fields(
        FeedbackResponse.class, feedback.feedbackId, feedback.subject, feedback.content,
        feedback.category, feedback.imageUrl, feedback.likeCount, feedback.secret,
        feedback.createdAt, feedback.updatedAt);

    private final JPAQueryFactory jpaQueryFactory;

    private final BooleanExpression isNotDeletedFeedback = feedback.deletedAt.isNull();

    @Override
    public Page<FeedbackResponse> findAllByActiveMembers(
        Pageable pageable,
        SearchTarget target,
        AnswerStatus answerStatus, Set<FeedbackCategory> categories,
        Long crossroadId, String keyword
    ) {
        BooleanExpression searchCondition = searchCondition(target, keyword);
        BooleanExpression activityMember = member.memberStatus.eq(MemberStatus.ACTIVITY);
        BooleanExpression answerStatusCondition = answerStatusCondition(answerStatus);
        BooleanExpression categoriesCondition = categoriesCondition(categories);
        BooleanExpression crossroadIdCondition = crossroadIdCondition(crossroadId);

        List<FeedbackResponse> results = jpaQueryFactory
            .select(feedbackResponseDto)
            .from(feedback)
            .join(member).on(feedback.member.eq(member)).fetchJoin()
            .where(
                searchCondition
                    .and(answerStatusCondition)
                    .and(categoriesCondition)
                    .and(crossroadIdCondition)
                    .and(activityMember)
                    .and(isNotDeletedFeedback)
            )
            .offset(pageable.getOffset()).limit(pageable.getPageSize())
            .orderBy(new OrderSpecifier<>(Order.DESC, feedback.createdAt))
            .fetch();

        long count = Optional.ofNullable(
            jpaQueryFactory
                .select(feedback.count())
                .from(feedback)
                .join(member).on(feedback.member.eq(member)).fetchJoin()
                .where(
                    searchCondition
                        .and(answerStatusCondition)
                        .and(categoriesCondition)
                        .and(crossroadIdCondition)
                        .and(activityMember)
                        .and(isNotDeletedFeedback)
                ).fetchOne()
        ).orElse(0L);

        return new PageImpl<>(results, pageable, count);
    }

    @Override
    public Page<FeedbackResponse> findPagedExcludingMember(Long memberId, Pageable pageable) {
        final List<FeedbackResponse> responses = jpaQueryFactory.select(feedbackNoMemberDto)
            .from(feedback).join(member)
            .on(feedback.member.eq(member).and(member.memberId.eq(memberId)))
            .where(isNotDeletedFeedback)
            .offset(pageable.getOffset()).limit(pageable.getPageSize())
            .orderBy(new OrderSpecifier<>(Order.DESC, feedback.createdAt)).fetch();

        final Long count = jpaQueryFactory.select(feedback.count()).from(feedback).join(member)
            .on(feedback.member.eq(member).and(member.memberId.eq(memberId)))
            .where(isNotDeletedFeedback).fetchOne();
        return new PageImpl<>(responses, pageable, count != null ? count : 0);
    }

    @Override
    public Page<FeedbackResponse> findAllByFilter(
        Pageable pageable, SearchTarget target,
        String keyword, AnswerStatus answerStatus,
        Set<FeedbackCategory> categories,
        LocalDate startDate, LocalDate endDate,
        Boolean deleted
    ) {
        BooleanExpression searchCondition = searchCondition(target, keyword);
        BooleanExpression answerStatusCondition = answerStatusCondition(answerStatus);
        BooleanExpression categoriesCondition = categoriesCondition(categories);
        BooleanExpression betweenDates = betweenDates(feedback.createdAt, startDate, endDate);
        BooleanExpression deletedCondition = deletedCondition(deleted);

        List<FeedbackResponse> results = jpaQueryFactory
            .select(feedbackResponseDto)
            .from(feedback)
            .join(member).on(feedback.member.eq(member)).fetchJoin()
            .where(
                searchCondition
                    .and(answerStatusCondition)
                    .and(deletedCondition)
                    .and(categoriesCondition)
                    .and(betweenDates)
            )
            .offset(pageable.getOffset()).limit(pageable.getPageSize())
            .orderBy(getOrderSpecifiers(pageable, feedback.getType(), "feedback")).fetch();

        long count = Optional.ofNullable(
            jpaQueryFactory
                .select(feedback.count())
                .from(feedback)
                .join(member).on(feedback.member.eq(member)).fetchJoin()
                .where(
                    searchCondition
                        .and(answerStatusCondition)
                        .and(deletedCondition)
                        .and(categoriesCondition)
                        .and(betweenDates)
                ).fetchOne()
        ).orElse(0L);

        return new PageImpl<>(results, pageable, count);
    }

    @Override
    public Feedback findByIdOrThrow(Long id ) {
        return Optional.ofNullable(
            jpaQueryFactory
                .selectFrom(feedback)
                .join(member).on(feedback.member.eq(member))
                .join(crossroad).on(feedback.crossroad.eq(crossroad))
                .where(feedback.feedbackId.eq(id).and(isNotDeletedFeedback))
                .fetchOne()
        ).orElseThrow(() -> new BusinessException(FeedbackErrorCode.NOT_FOUND_FEEDBACK));
    }

    private BooleanExpression answerStatusCondition(AnswerStatus answerStatus) {
        if (answerStatus == null) {
            return Expressions.TRUE;
        }
        return feedback.answerStatus.eq(answerStatus);
    }

    private BooleanExpression crossroadIdCondition(Long crossroadId) {
        if (crossroadId == null) {
            return Expressions.TRUE;
        }
        return feedback.crossroad.crossroadId.eq(crossroadId);
    }

    private BooleanExpression categoriesCondition(Set<FeedbackCategory> categories) {
        if (categories == null || categories.isEmpty()) {
            return Expressions.TRUE;
        }

        BooleanExpression expression = null;
        for (FeedbackCategory category : categories) {
            if (expression == null) {
                expression = feedback.category.eq(category);
            } else {
                expression = expression.or(feedback.category.eq(category));
            }
        }
        return expression;
    }

    private BooleanExpression searchCondition(SearchTarget target, String keyword) {
        BooleanExpression fulltextSearch = Expressions.TRUE;
        if (SearchTarget.WRITER.equals(target)) {
            fulltextSearch = fulltextSearch(keyword, member.nickname);
        } else if (SearchTarget.SUBJECT_OR_CONTENT.equals(target)) {
            fulltextSearch = fulltextSearch(keyword, feedback.subject, feedback.content);
        }
        return fulltextSearch;
    }

    private BooleanExpression deletedCondition(Boolean deleted) {
        if (deleted == null) {
            return Expressions.TRUE;
        }

        if (Boolean.TRUE.equals(deleted)) {
            return feedback.deletedAt.isNotNull();
        }
        return feedback.deletedAt.isNull();
    }
}
