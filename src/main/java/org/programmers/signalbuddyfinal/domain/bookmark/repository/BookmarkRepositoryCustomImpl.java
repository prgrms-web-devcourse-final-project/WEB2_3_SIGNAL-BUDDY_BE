package org.programmers.signalbuddyfinal.domain.bookmark.repository;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.QBean;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.programmers.signalbuddyfinal.domain.bookmark.dto.AdminBookmarkResponse;
import org.programmers.signalbuddyfinal.domain.bookmark.dto.BookmarkResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

import static org.programmers.signalbuddyfinal.domain.bookmark.entity.QBookmark.bookmark;
import static org.programmers.signalbuddyfinal.domain.member.entity.QMember.member;

@Repository
@RequiredArgsConstructor
public class BookmarkRepositoryCustomImpl implements BookmarkRepositoryCustom {

    private static final QBean<BookmarkResponse> pageBookmarkDto = Projections.fields(
        BookmarkResponse.class, bookmark.bookmarkId, bookmark.address, bookmark.name, bookmark.sequence,
        Expressions.numberTemplate(Double.class, "ST_X({0})", bookmark.coordinate).as("lng"),
        Expressions.numberTemplate(Double.class, "ST_Y({0})", bookmark.coordinate).as("lat"));

    private final QBean<AdminBookmarkResponse> adminBookmarkDto = Projections.fields(
        AdminBookmarkResponse.class, bookmark.bookmarkId, bookmark.address
    );

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<BookmarkResponse> findPagedByMember(Pageable pageable, Long memberId) {
        final List<BookmarkResponse> responses = queryFactory.select(pageBookmarkDto).from(bookmark)
            .join(member)
            .on(bookmark.member.eq(member).and(member.memberId.eq(memberId)))
            .where(bookmark.deletedAt.isNull())
            .offset(pageable.getOffset()).limit(pageable.getPageSize())
            .orderBy(new OrderSpecifier<>(Order.ASC, bookmark.sequence)).fetch();

        final Long count = queryFactory.select(bookmark.count()).from(bookmark).join(member)
            .on(bookmark.member.eq(member).and(member.memberId.eq(memberId)))
            .fetchOne();
        return new PageImpl<>(responses, pageable, count != null ? count : 0);
    }

    @Override
    public List<AdminBookmarkResponse> findBookmarkByMember(Long memberId) {
        final List<AdminBookmarkResponse> responses = queryFactory.select(adminBookmarkDto).from(bookmark)
            .join(member)
            .on(bookmark.member.eq(member).and(member.memberId.eq(memberId)))
            .orderBy(new OrderSpecifier<>(Order.ASC, bookmark.createdAt)).fetch();
        return responses;
    }
}
