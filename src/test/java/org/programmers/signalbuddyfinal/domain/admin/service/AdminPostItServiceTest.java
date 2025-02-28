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
import org.programmers.signalbuddyfinal.domain.crossroad.service.PointUtil;
import org.programmers.signalbuddyfinal.domain.member.entity.Member;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberRole;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberStatus;
import org.programmers.signalbuddyfinal.domain.member.repository.MemberRepository;
import org.programmers.signalbuddyfinal.domain.postit.entity.Danger;
import org.programmers.signalbuddyfinal.domain.postit.entity.Postit;
import org.programmers.signalbuddyfinal.domain.postit.repository.PostItRepository;
import org.programmers.signalbuddyfinal.domain.postitsolve.entity.PostitSolve;
import org.programmers.signalbuddyfinal.domain.postitsolve.repository.PostitSolveRepository;
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
    Postit solvedPostit;

    @Autowired
    PostItRepository postItRepository;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    AdminPostItService adminPostItService;
    @Autowired
    PostitSolveRepository postitSolveRepository;

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

        solvedPostit = createPostIt(Danger.WARNING, PointUtil.toPoint(1.0203, 1.3048), "제목1",
            "내용1",
            "https://image1.com/imageUrl",
            LocalDateTime.now().plusDays(5), LocalDateTime.now().plusDays(5),
            member.get(0));
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

        assertThat(adminPostItService.getAllPostIt(pageable).getTotalElements()).isEqualTo(22);
        assertThat(adminPostItService.getAllPostIt(pageable).getPageSize()).isEqualTo(10);
    }

    @Test
    @DisplayName("관리자 포스트잇 미해결 -> 해결 변경 성공 테스트")
    @Transactional
    public void adminCompletePostItSuccessTest() {
        adminPostItService.completePostIt(unsolvedPostit.getPostitId(),
            LocalDateTime.now().plusDays(7));

        assertThat(unsolvedPostit.getDeletedAt()).isNotNull();
        assertThat(postitSolveRepository.findAll().size()).isEqualTo(1);
    }

    @Test
    @DisplayName("관리자 포스트잇 해결 - 미해결 포스트잇 변경 성공 테스트")
    public void adminAlreadyCompletePostItExceptionTest() {
        postitSolveRepository.save(PostitSolve.creator()
            .content("내용")
            .imageUrl("https://image1.com/imageUrl")
            .deletedAt(LocalDateTime.now().minusDays(3))
            .member(member.get(0))
            .postit(solvedPostit)
            .build());

        adminPostItService.completePostIt(solvedPostit.getPostitId(),
            LocalDateTime.now().plusDays(7));

        assertThat(solvedPostit.getDeletedAt()).isNotNull();
        assertThat(postitSolveRepository.findAll().size()).isEqualTo(0);
    }

    @Test
    @DisplayName("관리자 해결 -> 미해결 변경시 만료일이 현재일보다 이전인 경우")
    @Transactional
    public void expireDateExceptionTest() {

        assertThrows(BusinessException.class, () -> {
            adminPostItService.completePostIt(unsolvedPostit.getPostitId(),
                LocalDateTime.now().minusDays(7));
        });
    }

    @DisplayName("포스트잇 기간별 조회 시작일 미지정 예외 테스트")
    @Test
    public void 기간별_조회_시작일_미지정_테스트() {

        PostItFilterRequest noStartDateFilter = createFilter(null,
            LocalDateTime.of(2025, 1, 25, 0, 0, 0), null, null, null);

        assertThrows(
            BusinessException.class,
            () -> adminPostItService.getAllPostItWithFilter(pageable, noStartDateFilter));
    }

    @DisplayName("포스트잇 기간별 조회 시작일 > 종료일 예외 테스트")
    @Test
    public void 기간별_조회_시작일_종료일_비교_테스트() {

        PostItFilterRequest afterStartDateFilter = createFilter(
            LocalDateTime.of(2025, 1, 25, 0, 0, 0),
            LocalDateTime.of(2024, 1, 25, 0, 0, 0), null, null, null);

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
        Danger danger, Deleted deleted,
        String search) {
        return PostItFilterRequest.builder()
            .startDate(startDate)
            .endDate(endDate)
            .search(search)
            .danger(danger)
            .deleted(deleted)
            .build();
    }

}
