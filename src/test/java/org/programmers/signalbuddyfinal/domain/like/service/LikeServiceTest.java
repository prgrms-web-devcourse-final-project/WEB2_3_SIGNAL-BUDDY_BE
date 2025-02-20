package org.programmers.signalbuddyfinal.domain.like.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.programmers.signalbuddyfinal.domain.like.service.LikeService.getLikeKeyPrefix;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.programmers.signalbuddyfinal.domain.crossroad.dto.CrossroadApiResponse;
import org.programmers.signalbuddyfinal.domain.crossroad.entity.Crossroad;
import org.programmers.signalbuddyfinal.domain.crossroad.repository.CrossroadRepository;
import org.programmers.signalbuddyfinal.domain.feedback.entity.Feedback;
import org.programmers.signalbuddyfinal.domain.feedback.entity.enums.FeedbackCategory;
import org.programmers.signalbuddyfinal.domain.feedback.repository.FeedbackRepository;
import org.programmers.signalbuddyfinal.domain.like.dto.LikeExistResponse;
import org.programmers.signalbuddyfinal.domain.like.entity.Like;
import org.programmers.signalbuddyfinal.domain.like.repository.LikeRepository;
import org.programmers.signalbuddyfinal.domain.member.entity.Member;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberRole;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberStatus;
import org.programmers.signalbuddyfinal.domain.member.repository.MemberRepository;
import org.programmers.signalbuddyfinal.global.db.RedisTestContainer;
import org.programmers.signalbuddyfinal.global.dto.CustomUser2Member;
import org.programmers.signalbuddyfinal.global.security.basic.CustomUserDetails;
import org.programmers.signalbuddyfinal.global.support.ServiceTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

class LikeServiceTest extends ServiceTest implements RedisTestContainer {

    @Autowired
    private LikeService likeService;

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private CrossroadRepository crossroadRepository;

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private Member member;
    private Feedback feedback;

    @BeforeEach
    void setup() {
        member = Member.builder()
            .email("test@test.com")
            .password("123456")
            .role(MemberRole.USER)
            .nickname("tester")
            .memberStatus(MemberStatus.ACTIVITY)
            .profileImageUrl("https://test-image.com/test-123131")
            .build();
        member = memberRepository.save(member);

        Crossroad crossroad = new Crossroad(CrossroadApiResponse.builder()
            .crossroadApiId("13214").name("00사거리")
            .lat(37.12222).lng(127.12132)
            .build());
        crossroad = crossroadRepository.save(crossroad);

        String subject = "test subject";
        String content = "test content";
        Feedback entity = Feedback.create()
            .subject(subject).content(content).secret(Boolean.FALSE)
            .category(FeedbackCategory.ETC)
            .member(member).crossroad(crossroad)
            .build();
        feedback = feedbackRepository.save(entity);
    }

    @DisplayName("좋아요 추가를 성공한다.")
    @Test
    void addLike() {
        // given
        CustomUser2Member user = new CustomUser2Member(
            new CustomUserDetails(member.getMemberId(), "", "",
                "", "", MemberRole.USER, MemberStatus.ACTIVITY));

        // when
        likeService.addLike(feedback.getFeedbackId(), user);

        // then
        String deleteLike = redisTemplate.opsForValue()
            .get(getLikeKeyPrefix() + feedback.getFeedbackId() + ":" + member.getMemberId());
        assertThat(deleteLike).isEqualTo("ADD");
        redisTemplate.delete(getLikeKeyPrefix()
            + feedback.getFeedbackId() + ":" + member.getMemberId());
    }

    @DisplayName("좋아요 취소를 성공한다.")
    @Test
    void deleteLike() {
        // given
        CustomUser2Member user = new CustomUser2Member(
            new CustomUserDetails(member.getMemberId(), "", "",
                "", "", MemberRole.USER, MemberStatus.ACTIVITY));

        // when
        likeRepository.save(Like.create(member, feedback));
        likeService.deleteLike(feedback.getFeedbackId(), user);

        // then
        String deleteLike = redisTemplate.opsForValue()
            .get(getLikeKeyPrefix() + feedback.getFeedbackId() + ":" + member.getMemberId());
        assertThat(deleteLike).isEqualTo("CANCEL");
        redisTemplate.delete(getLikeKeyPrefix()
            + feedback.getFeedbackId() + ":" + member.getMemberId());
    }

    @DisplayName("해당 좋아요가 존재한다.")
    @Test
    void existsLikeTrue() {
        // given
        Long feedbackId = feedback.getFeedbackId();
        CustomUser2Member user = new CustomUser2Member(
            new CustomUserDetails(member.getMemberId(), "", "",
                "", "", MemberRole.USER, MemberStatus.ACTIVITY));

        // when
        likeRepository.save(Like.create(member, feedback));
        LikeExistResponse actual = likeService.existsLike(feedbackId, user);

        // then
        assertThat(actual.getStatus()).isTrue();
    }

    @DisplayName("해당 좋아요가 존재하지 않는다.")
    @Test
    void existsLikeFalse() {
        // given
        Long feedbackId = feedback.getFeedbackId();
        CustomUser2Member user = new CustomUser2Member(
            new CustomUserDetails(member.getMemberId(), "", "",
                "", "", MemberRole.USER, MemberStatus.ACTIVITY));

        // when
        LikeExistResponse actual = likeService.existsLike(feedbackId, user);

        // then
        assertThat(actual.getStatus()).isFalse();
    }
}