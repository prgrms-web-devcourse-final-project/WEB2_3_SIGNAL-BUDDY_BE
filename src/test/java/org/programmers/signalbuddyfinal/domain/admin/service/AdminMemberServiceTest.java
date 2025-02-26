package org.programmers.signalbuddyfinal.domain.admin.service;

import static org.junit.jupiter.api.Assertions.assertThrows;

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
import org.programmers.signalbuddyfinal.global.exception.BusinessException;
import org.programmers.signalbuddyfinal.global.support.ServiceTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public class AdminMemberServiceTest extends ServiceTest {

    private Pageable pageable;
    @Autowired
    AdminService adminService;

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

    @DisplayName("기간별 조회 중복 사용 예외 테스트")
    @Test
    public void 기간별_조회_중복_사용_테스트() {

        MemberFilterRequest duplicatedFilter = createFilter(null, null, null,
            LocalDateTime.of(2024, 1, 25, 0, 0, 0),
            LocalDateTime.of(2025, 1, 25, 0, 0, 0), Periods.TODAY, null);

        assertThrows(
            BusinessException.class,
            () -> adminService.getAllMemberWithFilter(pageable, duplicatedFilter));
    }

    @DisplayName("기간별 조회 시작일 미지정 예외 테스트")
    @Test
    public void 기간별_조회_시작일_미지정_테스트() {

        MemberFilterRequest noStartDateFilter = createFilter(null, null, null, null,
            LocalDateTime.of(2025, 1, 25, 0, 0, 0), null, null);

        assertThrows(
            BusinessException.class,
            () -> adminService.getAllMemberWithFilter(pageable, noStartDateFilter));
    }

    @DisplayName("기간별 조회 시작일 > 종료일 예외 테스트")
    @Test
    public void 기간별_조회_시작일_종료일_비교_테스트() {

        MemberFilterRequest afterStartDateFilter = createFilter(null, null, null,
            LocalDateTime.of(2025, 1, 25, 0, 0, 0),
            LocalDateTime.of(2024, 1, 25, 0, 0, 0), null, null);

        assertThrows(
            BusinessException.class,
            () -> adminService.getAllMemberWithFilter(pageable, afterStartDateFilter));
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