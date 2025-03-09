package org.programmers.signalbuddyfinal.domain.like.repository;

import static org.programmers.signalbuddyfinal.domain.feedback.entity.QFeedback.feedback;
import static org.programmers.signalbuddyfinal.domain.like.entity.QLike.like;
import static org.programmers.signalbuddyfinal.domain.member.entity.QMember.member;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.QBean;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.programmers.signalbuddyfinal.domain.feedback.dto.FeedbackResponse;
import org.programmers.signalbuddyfinal.domain.member.dto.MemberResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CustomLikeRepositoryImpl implements CustomLikeRepository {

    private static final QBean<MemberResponse> memberResponseDto = Projections.fields(
        MemberResponse.class, member.memberId, member.email, member.nickname,
        member.profileImageUrl, member.role, member.memberStatus);

    private static final QBean<FeedbackResponse> feedbackDto = Projections.fields(
        FeedbackResponse.class, feedback.feedbackId, feedback.subject, feedback.content,
        feedback.category, feedback.imageUrl, feedback.likeCount, feedback.secret,
        feedback.answerStatus, memberResponseDto.as("member"), feedback.createdAt,
        feedback.updatedAt, feedback.deletedAt);

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<FeedbackResponse> findPagedLikedFeedbacks(Long memberId, Pageable pageable) {
        final List<FeedbackResponse> responses = queryFactory.select(feedbackDto).from(feedback)
            .join(like).on(like.feedback.feedbackId.eq(feedback.feedbackId)).join(member)
            .on(feedback.member.memberId.eq(member.memberId))
            .where(like.member.memberId.eq(memberId), feedback.deletedAt.isNull())
            .offset(pageable.getOffset()).limit(pageable.getPageSize())
            .orderBy(feedback.createdAt.desc()).fetch();

        final Long count = Optional.ofNullable(
                queryFactory.select(feedback.count()).from(feedback).join(like)
                    .on(like.feedback.feedbackId.eq(feedback.feedbackId)).join(member)
                    .on(feedback.member.memberId.eq(member.memberId))
                    .where(like.member.memberId.eq(memberId), feedback.deletedAt.isNull()).fetchOne())
            .orElse(0L);

        return new PageImpl<>(responses, pageable, count);
    }
}
