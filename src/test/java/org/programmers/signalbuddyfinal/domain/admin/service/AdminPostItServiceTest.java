package org.programmers.signalbuddyfinal.domain.admin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import groovy.util.logging.Slf4j;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Point;
import org.programmers.signalbuddyfinal.domain.admin.dto.PostItFilterRequest;
import org.programmers.signalbuddyfinal.domain.admin.dto.enums.Deleted;
import org.programmers.signalbuddyfinal.domain.admin.dto.enums.Periods;
import org.programmers.signalbuddyfinal.domain.crossroad.service.PointUtil;
import org.programmers.signalbuddyfinal.domain.member.entity.Member;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberRole;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberStatus;
import org.programmers.signalbuddyfinal.domain.member.repository.MemberRepository;
import org.programmers.signalbuddyfinal.domain.postit.entity.Danger;
import org.programmers.signalbuddyfinal.domain.postit.entity.Postit;
import org.programmers.signalbuddyfinal.domain.postit.repository.PostItRepository;
import org.programmers.signalbuddyfinal.global.exception.BusinessException;
import org.programmers.signalbuddyfinal.global.support.ServiceTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
public class AdminPostItServiceTest extends ServiceTest {

    private static final Logger log = LoggerFactory.getLogger(AdminPostItServiceTest.class);
    List<Member> member;
    Pageable pageable;
    Postit unsolvedPostit;

    @Autowired
    PostItRepository postItRepository;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    AdminPostItService adminPostItService;

    @BeforeEach
    public void setUp() {
        member = new ArrayList<>();

        for (int i = 0; i < 20; i++) {
            member.add(createMember("user" + i + "@gmail.com", "user" + i));
            createPostIt(Danger.WARNING, PointUtil.toPoint(1.0203, 1.3048), "제목1", "내용1",
                "https://image1.com/imageUrl",
                LocalDateTime.of(2025, 1, 2, 1, 30), LocalDateTime.of(2025, 1, 8, 1, 30),
                member.get(i));
        }

        unsolvedPostit = createPostIt(Danger.WARNING, PointUtil.toPoint(1.0203, 1.3048), "제목1",
            "내용1",
            "https://image1.com/imageUrl",
            LocalDateTime.of(2025, 1, 2, 1, 30), null,
            member.get(0));
        pageable = PageRequest.of(0, 10);
    }

    @DisplayName("포스트잇 전체 조회 성공 테스트")
    @Test
    public void getAllPostItSuccessTest() {

        assertThat(adminPostItService.getAllPostIt(pageable).getTotalElements()).isEqualTo(21);
        assertThat(adminPostItService.getAllPostIt(pageable).getPageSize()).isEqualTo(10);
    }

    @Test
    @DisplayName("관리자 포스트잇 해결 상태 변경 성공 테스트")
    @Transactional
    public void adminCompletePostItSuccessTest() {
        adminPostItService.completePostIt(unsolvedPostit.getPostitId());
        assertThat(unsolvedPostit.getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("관리자 이미 해결된 포스트잇 변경 예외 발생 테스트")
    public void adminAlreadyCompletePostItExceptionTest() {

        assertThrows(BusinessException.class, () -> adminPostItService.completePostIt(1L));
    }

    @DisplayName("포스트잇 기간별 조회 중복 사용 예외 테스트")
    @Test
    public void 기간별_조회_중복_사용_테스트() {

        PostItFilterRequest duplicatedFilter = createFilter(
            LocalDateTime.of(2024, 1, 25, 0, 0, 0),
            LocalDateTime.of(2025, 1, 25, 0, 0, 0), Periods.MONTH, null, null, null);

        assertThrows(
            BusinessException.class,
            () -> adminPostItService.getAllPostItWithFilter(pageable, duplicatedFilter));
    }

    @DisplayName("포스트잇 기간별 조회 시작일 미지정 예외 테스트")
    @Test
    public void 기간별_조회_시작일_미지정_테스트() {

        PostItFilterRequest noStartDateFilter = createFilter(null,
            LocalDateTime.of(2025, 1, 25, 0, 0, 0), null, null, null, null);

        assertThrows(
            BusinessException.class,
            () -> adminPostItService.getAllPostItWithFilter(pageable, noStartDateFilter));
    }

    @DisplayName("포스트잇 기간별 조회 시작일 > 종료일 예외 테스트")
    @Test
    public void 기간별_조회_시작일_종료일_비교_테스트() {

        PostItFilterRequest afterStartDateFilter = createFilter(
            LocalDateTime.of(2025, 1, 25, 0, 0, 0),
            LocalDateTime.of(2024, 1, 25, 0, 0, 0), null, null, null, null);

        assertThrows(
            BusinessException.class,
            () -> adminPostItService.getAllPostItWithFilter(pageable, afterStartDateFilter));
    }

    private Postit createPostIt(Danger danger, Point coordinate, String subject,
        String content, String imageURl, LocalDateTime expiryDate, LocalDateTime deleteAt,
        Member member) {

        Postit postit = Postit.builder()
            .danger(danger)
            .coordinate(coordinate)
            .subject(subject)
            .content(content)
            .imageUrl(imageURl)
            .expiryDate(expiryDate)
            .deletedAt(deleteAt)
            .member(member)
            .build();
        return postItRepository.save(postit);
    }

    private Member createMember(String email, String nickName) {
        return memberRepository.save(Member.builder()
            .email(email)
            .password("password1")
            .nickname(nickName)
            .profileImageUrl("url")
            .role(MemberRole.USER)
            .memberStatus(MemberStatus.ACTIVITY)
            .build());
    }

    private PostItFilterRequest createFilter(LocalDateTime startDate, LocalDateTime endDate,
        Periods period, Danger danger, Deleted deleted,
        String search) {
        return PostItFilterRequest.builder()
            .startDate(startDate)
            .endDate(endDate)
            .periods(period)
            .search(search)
            .danger(danger)
            .deleted(deleted)
            .build();
    }

}
