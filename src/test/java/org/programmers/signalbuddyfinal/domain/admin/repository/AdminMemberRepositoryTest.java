package org.programmers.signalbuddyfinal.domain.admin.repository;


import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.programmers.signalbuddyfinal.domain.admin.dto.MemberFilterRequest;
import org.programmers.signalbuddyfinal.domain.admin.dto.enums.Periods;
import org.programmers.signalbuddyfinal.domain.member.entity.Member;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberRole;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberStatus;
import org.programmers.signalbuddyfinal.domain.member.repository.MemberRepository;
import org.programmers.signalbuddyfinal.domain.social.entity.SocialProvider;
import org.programmers.signalbuddyfinal.domain.social.repository.SocialProviderRepository;
import org.programmers.signalbuddyfinal.global.support.RepositoryTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

class AdminMemberRepositoryTest extends RepositoryTest {

    private Pageable pageable;
    @Autowired
    MemberRepository memberRepository;

    @Autowired
    SocialProviderRepository socialProviderRepository;

    @BeforeEach
    void setUp() {
        createMember("user1@test.com", "user1", MemberRole.USER, MemberStatus.ACTIVITY, null);
        createMember("user2@test.com", "user2", MemberRole.USER, MemberStatus.ACTIVITY, null);
        createMember("user3@test.com", "user3", MemberRole.USER, MemberStatus.ACTIVITY, null);
        createMember("user4@test.com", "user4", MemberRole.USER, MemberStatus.ACTIVITY, "naver");
        createMember("user5@test.com", "user5", MemberRole.USER, MemberStatus.WITHDRAWAL, null);
        createMember("user6@test.com", "user6", MemberRole.USER, MemberStatus.WITHDRAWAL, null);
        createMember("user7@test.com", "user7", MemberRole.USER, MemberStatus.WITHDRAWAL, "naver");
        createMember("user8@test.com", "user8", MemberRole.USER, MemberStatus.WITHDRAWAL, "naver");
        createMember("admin@test.com", "amin", MemberRole.ADMIN, MemberStatus.ACTIVITY, null);
        pageable = PageRequest.of(0, 10);
    }

    @DisplayName("회원 전체 조회 테스트")
    @Test
    public void getAllMEmberTest() {

        assertThat(memberRepository.findAllMembers(pageable).getTotalElements()).isEqualTo(9);
    }


    @DisplayName("한개의 조건이 설정된 사용자 필더링 조회")
    @Test
    public void 한개의_조건이_설정된_사용자_필터링_조회() {
        MemberFilterRequest roleFilter = createFilter(null, MemberRole.USER, null, null, null,
            null, null);
        MemberFilterRequest statusFilter = createFilter(MemberStatus.ACTIVITY, null, null, null,
            null, null, null);
        MemberFilterRequest oAuthFilter = createFilter(null, null, "naver", null, null, null, null);

        assertThat(memberRepository.findAllMemberWithFilter(pageable, roleFilter)
            .getTotalElements()).isEqualTo(8);
        assertThat(memberRepository.findAllMemberWithFilter(pageable, statusFilter)
            .getTotalElements()).isEqualTo(5);
        assertThat(memberRepository.findAllMemberWithFilter(pageable, oAuthFilter)
            .getTotalElements()).isEqualTo(3);
    }

    @DisplayName("사용자 + 활성 + null 필터링 조회")
    @Test
    public void 사용자_활성_null_조회() {
        MemberFilterRequest roleAndActivityFilter = createFilter(MemberStatus.ACTIVITY,
            MemberRole.USER, null, null, null, null, null);

        assertThat(memberRepository.findAllMemberWithFilter(pageable, roleAndActivityFilter)
            .getTotalElements()).isEqualTo(4);
    }

    @DisplayName("사용자 + 비활성 + null 필터링 조회")
    @Test
    public void 사용자_비활성_null_조회() {

        MemberFilterRequest roleAndWithdrawalFilter = createFilter(MemberStatus.WITHDRAWAL,
            MemberRole.USER, null, null, null, null, null);

        assertThat(memberRepository.findAllMemberWithFilter(pageable, roleAndWithdrawalFilter)
            .getTotalElements()).isEqualTo(4);
    }

    @DisplayName("사용자 + 활성 + oAuth 필터링 조회")
    @Test
    public void 사용자_활성_oAuth_조회() {

        MemberFilterRequest roleAndActivityWithOAuthAFilter = createFilter(MemberStatus.ACTIVITY,
            MemberRole.USER, "naver", null, null, null, null);

        assertThat(
            memberRepository.findAllMemberWithFilter(pageable, roleAndActivityWithOAuthAFilter)
                .getTotalElements()).isEqualTo(1);
    }

    @DisplayName("필터가 전부 null 인 경우")
    @Test
    public void 전체_null_조회() {

        MemberFilterRequest roleAndActivityWithOAuthAFilter = createFilter(null, null, null, null,
            null, null, null);

        assertThat(
            memberRepository.findAllMemberWithFilter(pageable, roleAndActivityWithOAuthAFilter)
                .getTotalElements()).isEqualTo(9);
    }

    private void createMember(String email, String nickname, MemberRole role, MemberStatus status,
        String oAuthProvider) {
        Member member = Member.builder()
            .email(email)
            .password("12345")
            .nickname(nickname)
            .profileImageUrl("http://example.com/profile.jpg")
            .role(role)
            .memberStatus(status)
            .build();

        memberRepository.save(member);

        if (oAuthProvider != null) {
            SocialProvider socialProvider = SocialProvider.builder()
                .socialId("socialId")
                .oauthProvider("naver")
                .member(member)
                .build();
            socialProviderRepository.save(socialProvider);
        }
    }

    private MemberFilterRequest createFilter(MemberStatus status, MemberRole role,
        String oAuthProvider, LocalDateTime startDate, LocalDateTime endDate, Periods period,
        String search) {
        return MemberFilterRequest.builder()
            .role(role)
            .status(status)
            .oAuthProvider(oAuthProvider)
            .startDate(startDate)
            .endDate(endDate)
            .periods(period)
            .search(search)
            .build();
    }
}
