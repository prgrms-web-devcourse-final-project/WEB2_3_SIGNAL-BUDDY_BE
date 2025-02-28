package org.programmers.signalbuddyfinal.domain.member.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.QBean;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.programmers.signalbuddyfinal.domain.admin.dto.AdminMemberResponse;
import org.programmers.signalbuddyfinal.domain.admin.dto.MemberFilterRequest;
import org.programmers.signalbuddyfinal.domain.admin.dto.WithdrawalMemberResponse;
import org.programmers.signalbuddyfinal.domain.member.entity.QMember;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberRole;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberStatus;
import org.programmers.signalbuddyfinal.global.dto.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import java.util.List;
import static org.programmers.signalbuddyfinal.domain.member.entity.QMember.member;
import static org.programmers.signalbuddyfinal.domain.social.entity.QSocialProvider.socialProvider;

@Slf4j
@Repository
@RequiredArgsConstructor
public class CustomMemberRepositoryImpl implements CustomMemberRepository {

    private static final QBean<WithdrawalMemberResponse> withdrawalMemberResponseDto = Projections.fields(
        WithdrawalMemberResponse.class, member.memberId, member.email, member.nickname,
        member.profileImageUrl, member.role, member.memberStatus,
        member.createdAt, member.updatedAt);

    private static final QBean<AdminMemberResponse> adminMemberResponseDto = Projections.fields(
        AdminMemberResponse.class, member.memberId.as("memberId"), member.email, member.nickname,
        member.role,
        member.memberStatus.as("status"), member.createdAt,
        socialProvider.oauthProvider.as("oauthProvider")
    );

    private static final QMember qmember = QMember.member;

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public PageResponse<AdminMemberResponse> findAllMembers(Pageable pageable) {
        List<AdminMemberResponse> members = jpaQueryFactory
            .select(adminMemberResponseDto)
            .from(member)
            .leftJoin(socialProvider).on(socialProvider.member.memberId.eq(member.memberId))
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .orderBy(member.email.asc())
            .fetch();

        long total = jpaQueryFactory
            .select(adminMemberResponseDto)
            .from(member)
            .fetchCount();

        return new PageResponse<>(new PageImpl<>(members, pageable, total));
    }
    @Override
    public Page<WithdrawalMemberResponse> findAllWithdrawMembers(Pageable pageable) {
        List<WithdrawalMemberResponse> members = jpaQueryFactory
            .select(withdrawalMemberResponseDto)
            .from(member)
            .where(member.memberStatus.eq(MemberStatus.WITHDRAWAL))
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .orderBy(member.email.asc())
            .fetch();

        long total = jpaQueryFactory
            .select(withdrawalMemberResponseDto)
            .from(member)
            .where(member.memberStatus.eq(MemberStatus.WITHDRAWAL))
            .fetchCount();

        return new PageImpl<>(members, pageable, total);
    }

    @Override
    public PageResponse<AdminMemberResponse> findAllMemberWithFilter(Pageable pageable,
        MemberFilterRequest filter) {

        List<AdminMemberResponse> members = jpaQueryFactory
            .select(adminMemberResponseDto)
            .from(member)
            .leftJoin(socialProvider).on(socialProvider.member.memberId.eq(member.memberId))
            .where(
                eqSearch(filter.getSearch()),
                eqStatus(filter.getStatus()),
                eqRole(filter.getRole()),
                eqOAuthProvider(filter.getOAuthProvider()),
                betweenCreatedAt(filter.getStartDate(), filter.getEndDate())
            )
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .orderBy(member.email.asc())
            .fetch();

        long total = jpaQueryFactory
            .select(adminMemberResponseDto)
            .from(member)
            .leftJoin(socialProvider).on(socialProvider.member.memberId.eq(member.memberId))
            .where(
                eqSearch(filter.getSearch()),
                eqStatus(filter.getStatus()),
                eqRole(filter.getRole()),
                eqOAuthProvider(filter.getOAuthProvider()),
                betweenCreatedAt(filter.getStartDate(), filter.getEndDate())
            )
            .fetchCount();

        return new PageResponse<>(new PageImpl<>(members, pageable, total));
    }

    private BooleanExpression eqStatus(MemberStatus status) {
        return (status != null ? member.memberStatus.eq(status) : Expressions.TRUE);
    }

    private BooleanExpression eqRole(MemberRole role) {
        return (role != null ? member.role.eq(role) : Expressions.TRUE);
    }

    private BooleanExpression eqOAuthProvider(String oauthProvider) {
        return ((oauthProvider != null && !oauthProvider.isEmpty())
            ? socialProvider.oauthProvider.eq(oauthProvider) : Expressions.TRUE);
    }

    // 검색
    private BooleanExpression eqSearch(String search) {
        return ((search != null && !search.isEmpty()) ? member.email.eq(search)
            .or(member.nickname.eq(search)) : Expressions.TRUE);
    }

    // 기간 조회
    private BooleanExpression betweenCreatedAt(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate != null && endDate != null)
            return member.createdAt.between(startDate, endDate);
        return Expressions.TRUE;
    }
}
