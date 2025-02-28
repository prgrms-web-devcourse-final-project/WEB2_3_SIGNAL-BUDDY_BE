package org.programmers.signalbuddyfinal.domain.postit.repository;

import static org.programmers.signalbuddyfinal.domain.postit.entity.QPostit.postit;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.QBean;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.programmers.signalbuddyfinal.domain.admin.dto.AdminPostItResponse;
import org.programmers.signalbuddyfinal.domain.admin.dto.PostItFilterRequest;
import org.programmers.signalbuddyfinal.domain.admin.dto.enums.Deleted;
import org.programmers.signalbuddyfinal.domain.postit.entity.Danger;
import org.programmers.signalbuddyfinal.global.dto.PageResponse;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CustomPostItRepositoryImpl implements CustomPostItRepository {

    private static QBean<AdminPostItResponse> adminPostItResponseDto = Projections.fields(
        AdminPostItResponse.class, postit.danger, postit.subject, postit.content,
        postit.member.email.as("email"),
        postit.expiryDate
    );

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public PageResponse<AdminPostItResponse> findAllPostIt(Pageable pageable) {
        List<AdminPostItResponse> postIts = jpaQueryFactory
            .select(adminPostItResponseDto)
            .from(postit)
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .orderBy(postit.createdAt.desc())
            .fetch();

        long total = jpaQueryFactory
            .select(adminPostItResponseDto)
            .from(postit)
            .fetchCount();

        return new PageResponse<>(new PageImpl<>(postIts, pageable, total));
    }

    @Override
    public PageResponse<AdminPostItResponse> findAllPostItWithFilter(Pageable pageable,
        PostItFilterRequest filter) {

        List<AdminPostItResponse> postIts = jpaQueryFactory
            .select(adminPostItResponseDto)
            .from(postit)
            .where(
                eqSearch(filter.getSearch())
                    .and(eqDanger(filter.getDanger()))
                    .and(eqIsDeleted(filter.getDeleted()))
                    .and(betweenCreatedAt(filter.getStartDate(), filter.getEndDate()))
            )
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .orderBy(postit.createdAt.desc())
            .fetch();

        long total = jpaQueryFactory
            .select(adminPostItResponseDto)
            .from(postit)
            .where(
                eqSearch(filter.getSearch()),
                eqDanger(filter.getDanger()),
                eqIsDeleted(filter.getDeleted()),
                betweenCreatedAt(filter.getStartDate(), filter.getEndDate())
            )
            .fetchCount();

        return new PageResponse<>(new PageImpl<>(postIts, pageable, total));
    }

    private BooleanExpression eqDanger(Danger danger) {
        return (danger != null ? postit.danger.eq(danger) : Expressions.TRUE);
    }

    // 해결 조회
    private BooleanExpression eqIsDeleted(Deleted deleted) {
        if (deleted == null) {
            return null;
        }
        return deleted == Deleted.DELETED ? postit.deletedAt.isNotNull() : postit.deletedAt.isNull();
    }

    // 검색
    private BooleanExpression eqSearch(String search) {
        return ((search != null && !search.isEmpty()) ? postit.content.contains(search)
            .or(postit.subject.contains(search)) : Expressions.TRUE);
    }

    // 기간 조회
    private BooleanExpression betweenCreatedAt(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate != null && endDate != null) {
            return postit.expiryDate.between(startDate, endDate);
        }
        return Expressions.TRUE;
    }
}
