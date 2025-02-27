package org.programmers.signalbuddyfinal.domain.postit.repository;

import static org.programmers.signalbuddyfinal.domain.postit.entity.QPostit.postit;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.QBean;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.programmers.signalbuddyfinal.domain.admin.dto.AdminPostItResponse;
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
}
