package org.programmers.signalbuddyfinal.domain.postitreport.service;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.programmers.signalbuddyfinal.domain.member.entity.Member;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberRole;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberStatus;
import org.programmers.signalbuddyfinal.domain.member.repository.MemberRepository;
import org.programmers.signalbuddyfinal.domain.postit.entity.Danger;
import org.programmers.signalbuddyfinal.domain.postit.entity.Postit;
import org.programmers.signalbuddyfinal.domain.postit.repository.PostItRepository;
import org.programmers.signalbuddyfinal.domain.postit_report.entity.PostItReport;
import org.programmers.signalbuddyfinal.domain.postit_report.repository.PostItReportRepository;
import org.programmers.signalbuddyfinal.domain.postit_report.service.PostItReportService;
import org.programmers.signalbuddyfinal.domain.postitsolve.repository.PostitSolveRepository;
import org.programmers.signalbuddyfinal.global.db.RedisTestContainer;
import org.programmers.signalbuddyfinal.global.dto.CustomUser2Member;
import org.programmers.signalbuddyfinal.global.exception.BusinessException;
import org.programmers.signalbuddyfinal.global.security.basic.CustomUserDetails;
import org.programmers.signalbuddyfinal.global.support.ServiceTest;
import org.programmers.signalbuddyfinal.global.util.PointUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

class PostItReportServiceTest extends ServiceTest implements RedisTestContainer {

    @Autowired
    PostItReportService postItReportService;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    PostItRepository postItRepository;
    @Autowired
    PostItReportRepository postItReportRepository;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    PostitSolveRepository postitSolveRepository;

    Postit postit1;
    Member member1;
    CustomUser2Member user1;
    Member member2;
    CustomUser2Member user2;
    private static final String USER_PREFIX = "postItReport:";
    private static final String COUNT_PREFIX = "postItReportCount:";

    @BeforeEach
    void setUp() {
        member1 = createMember("user1@gmail.com", "user1");
        user1 = createCustom2Member(1L, "user1@gmail.com", "user1");
        member2 = createMember("user2@gmail.com", "user2");
        user2 = createCustom2Member(2L, "user2@gmail.com", "user1");
        postit1 = createPostit(member1);
    }

    @Test
    @Transactional
    @DisplayName("최초 신고 성공 테스트")
    void addReport_FirstReport_Success() {

        String reportKey = USER_PREFIX +":"+ postit1.getPostitId();
        String countKey = COUNT_PREFIX +":"+ postit1.getPostitId();
        redisTemplate.delete(reportKey);
        redisTemplate.delete(countKey);

        postItReportService.addReport(postit1.getPostitId(), user1);
        PostItReport postItReport = postItReportRepository.findPostItReportByPostItIdAndMemberId(
            postit1.getPostitId(), member1.getMemberId());

        Assertions.assertThat(postit1.getReportCount()).isEqualTo(1L);
        Assertions.assertThat(postItReport).isNotNull();
        Assertions.assertThat(redisTemplate.opsForValue().get(countKey)).isNotNull();
        Assertions.assertThat(redisTemplate.opsForSet().isMember(reportKey, user1.getMemberId()))
            .isNotNull();

    }

    @Test
    @Transactional
    @DisplayName("추가 신고 성공 테스트")
    void addReport_SecondReport_Success() {
        String reportKey = USER_PREFIX +":"+ postit1.getPostitId();
        String countKey = COUNT_PREFIX +":"+ postit1.getPostitId();
        redisTemplate.delete(reportKey);
        redisTemplate.delete(countKey);

        postItReportService.addReport(postit1.getPostitId(), user1);
        postItReportService.addReport(postit1.getPostitId(), user2);
        PostItReport postItReport = postItReportRepository.findPostItReportByPostItIdAndMemberId(
            postit1.getPostitId(), member2.getMemberId());

        Assertions.assertThat(postit1.getReportCount()).isEqualTo(2);
        Assertions.assertThat(postItReport).isNotNull();
        Assertions.assertThat(redisTemplate.opsForValue().get(countKey)).isEqualTo(2L);
        Assertions.assertThat(redisTemplate.opsForSet().size(reportKey)).isEqualTo(2);

        redisTemplate.delete(reportKey);
        redisTemplate.delete(countKey);
    }

    @Test
    @DisplayName("이미 신고 내역이 있는경우 예외 발생 테스트")
    void already_report_exception_test() {

        PostItReport.create(member1, postit1);

        assertThrows(BusinessException.class,
            () -> postItReportService.addReport(postit1.getPostitId(), user1));
    }

    @Test
    @Transactional
    @DisplayName("신고 취소 성공 테스트")
    void cancelReport_sucess_test() {
        //given
        String reportKey = USER_PREFIX +":"+ postit1.getPostitId();
        String countKey = COUNT_PREFIX +":"+ postit1.getPostitId();

        redisTemplate.delete(reportKey);
        redisTemplate.delete(countKey);

        postItReportService.addReport(postit1.getPostitId(), user1);

        postItReportService.cancelReport(postit1.getPostitId(), user1);

        Assertions.assertThat(postit1.getReportCount()).isEqualTo(0L);
        Assertions.assertThat(postItReportRepository.findAll().size()).isEqualTo(0);
        Assertions.assertThat(redisTemplate.opsForValue().get(countKey)).isEqualTo(0L);
        Assertions.assertThat(redisTemplate.opsForSet().isMember(reportKey,user1.getMemberId())).isEqualTo(false);

        redisTemplate.delete(reportKey);
        redisTemplate.delete(countKey);
    }

    @Test
    @DisplayName("신고 내역이 없는 경우 신고 취소 예외 발생 테스트")
    void not_reported_exception_test() {

        assertThrows(BusinessException.class,
            () -> postItReportService.cancelReport(postit1.getPostitId(), user1));
    }

    @Test
    @Transactional
    @DisplayName("신고 누적 10회 포스트잇 비활성화 성공 테스트")
    void handleExcessive_report_success_test() {

        String reportKey = USER_PREFIX +":"+ postit1.getPostitId();
        String countKey = COUNT_PREFIX +":"+ postit1.getPostitId();

        redisTemplate.delete(reportKey);
        redisTemplate.delete(countKey);

        Postit postit = createPostit(member1);
        postit.updateReportCount(9L);

        postItReportService.addReport(postit.getPostitId(), user1);

        Assertions.assertThat(postit.getReportCount()).isEqualTo(10L);
        Assertions.assertThat(postitSolveRepository.findByPostItId(postit.getPostitId()))
            .isNotNull();
        Assertions.assertThat(redisTemplate.hasKey(countKey)).isEqualTo(false);
        Assertions.assertThat(redisTemplate.hasKey(reportKey)).isEqualTo(false);
    }

    private CustomUser2Member createCustom2Member(Long memberId, String email, String nickName) {
        CustomUserDetails customUserDetails = new CustomUserDetails(memberId, email, "password",
            "https://image1.com/imageUrl",
            nickName, MemberRole.USER, MemberStatus.ACTIVITY);
        return new CustomUser2Member(customUserDetails);
    }

    private Postit createPostit(Member member) {

        return postItRepository.save(Postit.creator()
            .danger(Danger.NOTICE)
            .coordinate(PointUtil.toPoint(1.394834, 2.9438))
            .subject("제목")
            .content("내용")
            .imageUrl("https://postItImg1.com/imageUrl")
            .expiryDate(LocalDateTime.now().plusDays(5))
            .member(member)
            .build());
    }

    private Member createMember(String email, String nickName) {
        return memberRepository.save(Member.builder()
            .email(email)
            .password("password")
            .nickname(nickName)
            .profileImageUrl("https://image1.com/imageUrl")
            .role(MemberRole.USER)
            .memberStatus(MemberStatus.ACTIVITY)
            .build());
    }
}
