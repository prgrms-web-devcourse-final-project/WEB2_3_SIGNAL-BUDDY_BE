package org.programmers.signalbuddyfinal.domain.comment.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.programmers.signalbuddyfinal.domain.comment.dto.CommentRequest;
import org.programmers.signalbuddyfinal.domain.comment.entity.Comment;
import org.programmers.signalbuddyfinal.domain.comment.exception.CommentErrorCode;
import org.programmers.signalbuddyfinal.domain.comment.repository.CommentRepository;
import org.programmers.signalbuddyfinal.domain.crossroad.dto.CrossroadApiResponse;
import org.programmers.signalbuddyfinal.domain.crossroad.entity.Crossroad;
import org.programmers.signalbuddyfinal.domain.crossroad.repository.CrossroadRepository;
import org.programmers.signalbuddyfinal.domain.feedback.entity.Feedback;
import org.programmers.signalbuddyfinal.domain.feedback.entity.enums.AnswerStatus;
import org.programmers.signalbuddyfinal.domain.feedback.entity.enums.FeedbackCategory;
import org.programmers.signalbuddyfinal.domain.feedback.repository.FeedbackRepository;
import org.programmers.signalbuddyfinal.domain.member.entity.Member;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberRole;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberStatus;
import org.programmers.signalbuddyfinal.domain.member.repository.MemberRepository;
import org.programmers.signalbuddyfinal.global.dto.CustomUser2Member;
import org.programmers.signalbuddyfinal.global.exception.BusinessException;
import org.programmers.signalbuddyfinal.global.security.basic.CustomUserDetails;
import org.programmers.signalbuddyfinal.global.support.ServiceTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@TestMethodOrder(OrderAnnotation.class)
class CommentServiceTest extends ServiceTest {

    @Autowired
    private CommentService commentService;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private CrossroadRepository crossroadRepository;

    private Member member;
    private Member admin;
    private Feedback feedback;
    private Comment comment;

    @BeforeEach
    void setup() {
        member = Member.builder().email("test@test.com").password("123456").role(MemberRole.USER)
            .nickname("tester").memberStatus(MemberStatus.ACTIVITY)
            .profileImageUrl("https://test-image.com/test-123131").build();
        member = memberRepository.save(member);

        admin = Member.builder().email("admin@test.com").password("123456").role(MemberRole.ADMIN)
            .nickname("admin").memberStatus(MemberStatus.ACTIVITY)
            .profileImageUrl("https://test-image.com/test-123131").build();
        admin = memberRepository.save(admin);

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

        comment = Comment.create()
            .content("test comment content")
            .feedback(feedback).member(member).build();
        comment = commentRepository.save(comment);
    }

    @DisplayName("일반 사용자가 댓글을 작성한다.")
    @Test
    @Order(1)
    void writeComment() {
        // given
        Long feedbackId = feedback.getFeedbackId();
        String content = "test comment content";
        CommentRequest request = new CommentRequest(content);
        CustomUser2Member user = new CustomUser2Member(
            new CustomUserDetails(member.getMemberId(), "", "",
                "", "", MemberRole.USER, MemberStatus.ACTIVITY));

        // when
        commentService.writeComment(feedbackId, request, user);

        // then
        Optional<Comment> actual = commentRepository.findById(2L);
        SoftAssertions.assertSoftly(softAssertions -> {
            softAssertions.assertThat(actual).get().isNotNull();
            softAssertions.assertThat(actual.get().getCommentId()).isNotNull();
            softAssertions.assertThat(actual.get().getContent()).isEqualTo(content);
            softAssertions.assertThat(actual.get().getMember().getMemberId())
                .isEqualTo(user.getMemberId());
            softAssertions.assertThat(actual.get().getFeedback().getFeedbackId())
                .isEqualTo(feedbackId);
        });
    }

    @DisplayName("관리자가 댓글(답변)을 작성한다.")
    @Test
    @Order(2)
    void writeCommentByAdmin() {
        // given
        Long feedbackId = feedback.getFeedbackId();
        String content = "test admin comment content";
        CommentRequest request = new CommentRequest(content);
        CustomUser2Member user = new CustomUser2Member(
            new CustomUserDetails(admin.getMemberId(), "", "",
                "", "", MemberRole.ADMIN, MemberStatus.ACTIVITY));

        // when
        commentService.writeComment(feedbackId, request, user);

        // then
        Optional<Comment> actual = commentRepository.findById(2L);
        SoftAssertions.assertSoftly(softAssertions -> {
            softAssertions.assertThat(actual).get().isNotNull();
            softAssertions.assertThat(actual.get().getCommentId()).isNotNull();
            softAssertions.assertThat(actual.get().getContent()).isEqualTo(content);
            softAssertions.assertThat(actual.get().getMember().getMemberId())
                .isEqualTo(user.getMemberId());
            softAssertions.assertThat(actual.get().getFeedback().getFeedbackId())
                .isEqualTo(feedbackId);
            softAssertions.assertThat(actual.get().getFeedback().getAnswerStatus())
                .isEqualTo(AnswerStatus.COMPLETION);
        });
    }

    @DisplayName("본인의 댓글을 수정한다.")
    @Test
    @Order(3)
    void updateComment() {
        // given
        String updatedContent = "update comment content";
        CommentRequest request = new CommentRequest(updatedContent);
        CustomUser2Member user = new CustomUser2Member(
            new CustomUserDetails(member.getMemberId(), "", "",
                "", "", MemberRole.USER, MemberStatus.ACTIVITY));

        // when
        commentService.updateComment(comment.getCommentId(), request, user);

        // then
        Optional<Comment> actual = commentRepository.findById(comment.getCommentId());
        SoftAssertions.assertSoftly(softAssertions -> {
            softAssertions.assertThat(actual.get().getCommentId()).isNotNull();
            softAssertions.assertThat(actual.get().getContent()).isEqualTo(updatedContent);
            softAssertions.assertThat(actual.get().getMember().getMemberId())
                .isEqualTo(user.getMemberId());
        });
    }

    @DisplayName("댓글 작성자와 다른 사람이 수정 시, 실패한다.")
    @Test
    @Order(4)
    void updateCommentFailure() {
        // given
        String updatedContent = "update comment content";
        CommentRequest request = new CommentRequest(updatedContent);
        CustomUser2Member user = new CustomUser2Member(
            new CustomUserDetails(999999L, "", "",
                "", "", MemberRole.USER, MemberStatus.ACTIVITY));

        // when & then
        try {
            commentService.updateComment(comment.getCommentId(), request, user);
        } catch (BusinessException e) {
            assertThat(e.getErrorCode())
                .isEqualTo(CommentErrorCode.COMMENT_MODIFIER_NOT_AUTHORIZED);
        }
    }

    @DisplayName("일반 사용자가 본인 댓글을 삭제한다.")
    @Test
    @Order(5)
    void deleteComment() {
        // given
        CustomUser2Member user = new CustomUser2Member(
            new CustomUserDetails(member.getMemberId(), "", "",
                "", "", MemberRole.USER, MemberStatus.ACTIVITY));

        // when
        commentService.deleteComment(comment.getCommentId(), user);

        // then
        assertThat(commentRepository.existsById(comment.getCommentId())).isFalse();
    }

    @DisplayName("관리자가 일반 사용자의 댓글을 삭제한다.")
    @Test
    @Order(6)
    void deleteCommentByAdmin() {
        // given
        CustomUser2Member user = new CustomUser2Member(
            new CustomUserDetails(admin.getMemberId(), "", "",
                "", "", MemberRole.ADMIN, MemberStatus.ACTIVITY));

        // when
        commentService.deleteComment(comment.getCommentId(), user);

        // then
        assertThat(commentRepository.existsById(comment.getCommentId())).isFalse();
    }

    @DisplayName("댓글 작성자와 다른 사람이 삭제 시, 실패한다.")
    @Test
    @Order(7)
    void deleteCommentFailure() {
        // given
        CustomUser2Member user = new CustomUser2Member(
            new CustomUserDetails(999999L, "", "",
                "", "", MemberRole.USER, MemberStatus.ACTIVITY));

        // when & then
        try {
            commentService.deleteComment(comment.getCommentId(), user);
        } catch (BusinessException e) {
            assertThat(e.getErrorCode())
                .isEqualTo(CommentErrorCode.COMMENT_ELIMINATOR_NOT_AUTHORIZED);
        }
    }

    @DisplayName("관리자 본인의 댓글(답변)을 삭제한다.")
    @Test
    @Order(8)
    void deleteAdminComment() {
        // given
        Long feedbackId = feedback.getFeedbackId();
        String content = "test admin comment content";
        CommentRequest request = new CommentRequest(content);
        CustomUser2Member user = new CustomUser2Member(
            new CustomUserDetails(admin.getMemberId(), "", "",
                "", "", MemberRole.ADMIN, MemberStatus.ACTIVITY));

        // when
        commentService.writeComment(feedbackId, request, user);
        commentService.deleteComment(2L, user);

        // then
        SoftAssertions.assertSoftly(softAssertions -> {
            softAssertions.assertThat(commentRepository.existsById(10L)).isFalse();
            softAssertions.assertThat(
                    feedbackRepository.findById(feedback.getFeedbackId()).get().getAnswerStatus())
                .isEqualTo(AnswerStatus.BEFORE);
        });
    }
}