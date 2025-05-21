package org.programmers.signalbuddyfinal.domain.admin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.programmers.signalbuddyfinal.domain.admin.dto.AdminMemberRequest;
import org.programmers.signalbuddyfinal.domain.admin.dto.MemberFilterRequest;
import org.programmers.signalbuddyfinal.domain.bookmark.dto.BookmarkRequest;
import org.programmers.signalbuddyfinal.domain.bookmark.entity.Bookmark;
import org.programmers.signalbuddyfinal.domain.bookmark.service.BookmarkService;
import org.programmers.signalbuddyfinal.domain.member.entity.Member;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberRole;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberStatus;
import org.programmers.signalbuddyfinal.domain.member.repository.MemberRepository;
import org.programmers.signalbuddyfinal.domain.social.entity.Provider;
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
    AdminMemberService adminService;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    SocialProviderRepository socialProviderRepository;

    @Autowired
    BookmarkService bookmarkService;

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

    @DisplayName("회원 전체 조회 성공 테스트")
    @Test
    public void successGetAllMember() {
        assertThat(adminService.getAllMembers(pageable).getTotalElements()).isEqualTo(9);
    }

    @DisplayName("회원 조회 성공 테스트")
    @Test
    public void successGetMember() {
        Member member = findMemberByEmail("user1@test.com");
        assertThat(adminService.getMember(member.getMemberId()).getEmail()).isEqualTo(
            "user1@test.com");
    }

    @DisplayName("회원 조회 실패 예외 발생 테스트")
    @Test
    public void failGetMember() {
        assertThrows(BusinessException.class, () -> {
            adminService.getMember(20L);
        });
    }

    @DisplayName("회원별 북마크 조회 성공 테스트")
    @Test
    public void successGetBookmarkTest() {
        bookmarkService.createBookmark(createBookmarkRequest((long) 111.111, "우리집"), 1L);
        bookmarkService.createBookmark(createBookmarkRequest((long) 222.222, "남의집"), 1L);
        int count = adminService.getMember(1L).getBookmarkCount();
        assertThat(count).isEqualTo(2);
    }

    @DisplayName("회원 필터링 조회 성공 테스트")
    @Test
    public void successGetAllMemberWithFilterTest() {
        MemberFilterRequest request = createFilter(null, null, null, null,
            null, null);
        assertThat(
            adminService.getAllMemberWithFilter(pageable, request).getTotalElements()).isEqualTo(9);
    }

    @DisplayName("기간별 조회 시작일 미지정 예외 테스트")
    @Test
    public void 기간별_조회_시작일_미지정_테스트() {

        MemberFilterRequest noStartDateFilter = createFilter(null, null, null, null,
            LocalDateTime.of(2025, 1, 25, 0, 0, 0), null);

        assertThrows(
            BusinessException.class,
            () -> adminService.getAllMemberWithFilter(pageable, noStartDateFilter));
    }

    @DisplayName("기간별 조회 종료일 미지정 예외 테스트")
    @Test
    public void 기간별_조회_종료일_미지정_테스트() {
        MemberFilterRequest noEndDateFilter = createFilter(null, null, null,
            LocalDateTime.of(2025, 1, 25, 0, 0, 0),
            null, null);

        assertThrows(
            BusinessException.class,
            () -> adminService.getAllMemberWithFilter(pageable, noEndDateFilter));
    }

    @DisplayName("기간별 조회 시작일 > 종료일 예외 테스트")
    @Test
    public void 기간별_조회_시작일_종료일_비교_테스트() {

        MemberFilterRequest afterStartDateFilter = createFilter(null, null, null,
            LocalDateTime.of(2025, 1, 25, 0, 0, 0),
            LocalDateTime.of(2024, 1, 25, 0, 0, 0), null);

        assertThrows(
            BusinessException.class,
            () -> adminService.getAllMemberWithFilter(pageable, afterStartDateFilter));
    }

    @DisplayName("기간별 조회 성공 테스트")
    @Test
    public void 기간별_조회_성공_테스트() {

        MemberFilterRequest filter = createFilter(null, null, null,
            LocalDateTime.of(2024, 1, 25, 0, 0, 0),
            LocalDateTime.of(2025, 6, 1, 0, 0, 0), null);

        assertThat(adminService.getAllMemberWithFilter(pageable, filter)
            .getTotalElements()).isEqualTo(9);
    }

    @DisplayName("기간별 조회 성공 테스트")
    @Test
    public void 기간별_조회_미지정_성공_테스트() {

        MemberFilterRequest filter = createFilter(null, null, null,
            null, null, null);

        assertThat(adminService.getAllMemberWithFilter(pageable, filter)
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
                .oauthProvider(Provider.NAVER)
                .member(member)
                .build();
            socialProviderRepository.save(socialProvider);
        }
    }

    private MemberFilterRequest createFilter(MemberStatus status, MemberRole role,
        Provider oAuthProvider, LocalDateTime startDate, LocalDateTime endDate, String search) {
        return MemberFilterRequest.builder()
            .role(role)
            .status(status)
            .oAuthProvider(oAuthProvider)
            .startDate(startDate)
            .endDate(endDate)
            .search(search)
            .build();
    }

    private Member findMemberByEmail(String email) {
        return memberRepository.findByEmail(email).get();
    }

    private BookmarkRequest createBookmarkRequest(Long lat, String name) {
        return BookmarkRequest.builder()
            .lng(123.456)
            .lat(lat)
            .address("서울시 어쩌구")
            .name(name)
            .build();
    }

}