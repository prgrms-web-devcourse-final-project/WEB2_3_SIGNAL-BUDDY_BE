package org.programmers.signalbuddyfinal.domain.comment.repository;

import java.util.ArrayList;
import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.programmers.signalbuddyfinal.domain.comment.dto.CommentResponse;
import org.programmers.signalbuddyfinal.domain.comment.entity.Comment;
import org.programmers.signalbuddyfinal.domain.crossroad.dto.CrossroadApiResponse;
import org.programmers.signalbuddyfinal.domain.crossroad.entity.Crossroad;
import org.programmers.signalbuddyfinal.domain.crossroad.repository.CrossroadRepository;
import org.programmers.signalbuddyfinal.domain.feedback.entity.Feedback;
import org.programmers.signalbuddyfinal.domain.feedback.entity.enums.FeedbackCategory;
import org.programmers.signalbuddyfinal.domain.feedback.repository.FeedbackRepository;
import org.programmers.signalbuddyfinal.domain.member.entity.Member;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberRole;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberStatus;
import org.programmers.signalbuddyfinal.domain.member.repository.MemberRepository;
import org.programmers.signalbuddyfinal.global.support.RepositoryTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

class CommentRepositoryTest extends RepositoryTest {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private CrossroadRepository crossroadRepository;

    private Member member;
    private Feedback feedback;

    @BeforeEach
    void setup() {
        member = Member.builder().email("test@test.com").password("123456").role(MemberRole.USER)
            .nickname("tester").memberStatus(MemberStatus.ACTIVITY)
            .profileImageUrl("https://test-image.com/test-123131").build();
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

        List<Comment> commentList = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            Comment comment = Comment.create()
                .content("test comment content")
                .feedback(feedback).member(member).build();
            commentList.add(comment);
        }
        commentRepository.saveAll(commentList);
    }

    @DisplayName("특정 피드백의 삭제되지 않은 댓글 목록을 가져온다.")
    @Test
    void findAllByFeedbackId() {
        // given
        Long feedbackId = feedback.getFeedbackId();

        // when
        Pageable pageable = PageRequest.of(2, 7);
        Page<CommentResponse> actual = commentRepository.findAllByFeedbackId(
            feedbackId, pageable);

        // then
        SoftAssertions.assertSoftly(softAssertions -> {
            softAssertions.assertThat(actual.getTotalElements()).isEqualTo(30);
            softAssertions.assertThat(actual.getTotalPages()).isEqualTo(5);
            softAssertions.assertThat(actual.getNumber()).isEqualTo(2);
            softAssertions.assertThat(actual.getContent().size()).isEqualTo(7);
            softAssertions.assertThat(actual.getContent().get(3).getCommentId()).isNotNull();
            softAssertions.assertThat(actual.getContent().get(3).getContent())
                .isEqualTo("test comment content");
            softAssertions.assertThat(actual.getContent().get(3).getMember().getMemberId())
                .isEqualTo(member.getMemberId());
        });
    }
}