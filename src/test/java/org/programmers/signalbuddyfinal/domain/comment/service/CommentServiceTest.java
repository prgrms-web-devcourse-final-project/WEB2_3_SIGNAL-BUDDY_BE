package org.programmers.signalbuddyfinal.domain.comment.service;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.programmers.signalbuddy.domain.comment.dto.CommentRequest;
import org.programmers.signalbuddy.domain.comment.entity.Comment;
import org.programmers.signalbuddy.domain.comment.exception.CommentErrorCode;
import org.programmers.signalbuddy.domain.comment.repository.CommentRepository;
import org.programmers.signalbuddy.domain.feedback.dto.FeedbackWriteRequest;
import org.programmers.signalbuddy.domain.feedback.entity.Feedback;
import org.programmers.signalbuddy.domain.feedback.entity.enums.AnswerStatus;
import org.programmers.signalbuddy.domain.feedback.repository.FeedbackRepository;
import org.programmers.signalbuddy.domain.member.entity.Member;
import org.programmers.signalbuddy.domain.member.entity.enums.MemberRole;
import org.programmers.signalbuddy.domain.member.entity.enums.MemberStatus;
import org.programmers.signalbuddy.domain.member.repository.MemberRepository;
import org.programmers.signalbuddy.global.dto.CustomUser2Member;
import org.programmers.signalbuddy.global.exception.BusinessException;
import org.programmers.signalbuddy.global.security.basic.CustomUserDetails;
import org.programmers.signalbuddy.global.support.ServiceTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

        String subject = "test subject";
        String content = "test content";
        FeedbackWriteRequest request = new FeedbackWriteRequest(subject, content);
        feedback = feedbackRepository.save(Feedback.create(request, member));

        comment = Comment.creator()
            .request(new CommentRequest(feedback.getFeedbackId(), "test comment content"))
            .feedback(feedback).member(member).build();
        comment = commentRepository.save(comment);
    }

    @DisplayName("댓글 작성 성공")
    @Test
    @Order(1)
    void writeComment() {
        // given
        Long feedbackId = feedback.getFeedbackId();
        String content = "test comment content";
        CommentRequest request = new CommentRequest(feedbackId, content);
        CustomUser2Member user = new CustomUser2Member(
            new CustomUserDetails(member.getMemberId(), "", "",
                "", "", MemberRole.USER, MemberStatus.ACTIVITY));

        // when
        commentService.writeComment(request, user);

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

    @DisplayName("관리자 댓글 작성 성공")
    @Test
    @Order(2)
    void writeCommentByAdmin() {
        // given
        Long feedbackId = feedback.getFeedbackId();
        String content = "test admin comment content";
        CommentRequest request = new CommentRequest(feedbackId, content);
        CustomUser2Member user = new CustomUser2Member(
            new CustomUserDetails(admin.getMemberId(), "", "",
                "", "", MemberRole.ADMIN, MemberStatus.ACTIVITY));

        // when
        commentService.writeComment(request, user);

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

    @DisplayName("댓글 수정 성공")
    @Test
    @Order(3)
    void updateComment() {
        // given
        Long feedbackId = feedback.getFeedbackId();
        String updatedContent = "update comment content";
        CommentRequest request = new CommentRequest(feedbackId, updatedContent);
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

    @DisplayName("댓글 작성자와 다른 사람이 수정 시, 실패")
    @Test
    @Order(4)
    void updateCommentFailure() {
        // given
        Long feedbackId = feedback.getFeedbackId();
        String updatedContent = "update comment content";
        CommentRequest request = new CommentRequest(feedbackId, updatedContent);
        // TODO: User 객체는 나중에 변경해야 함!
        CustomUser2Member user = new CustomUser2Member(
            new CustomUserDetails(999999L, "", "",
                "", "", MemberRole.USER, MemberStatus.ACTIVITY));

        // when & then
        assertThatThrownBy(() -> {
            commentService.updateComment(comment.getCommentId(), request, user);
        }).isExactlyInstanceOf(BusinessException.class)
            .hasMessage(CommentErrorCode.COMMENT_MODIFIER_NOT_AUTHORIZED.getMessage());
    }

    @DisplayName("댓글 삭제 성공")
    @Test
    @Order(5)
    void deleteComment() {
        // given
        CustomUser2Member user = new CustomUser2Member(
            new CustomUserDetails(member.getMemberId(), "", "",
                "", "", MemberRole.ADMIN, MemberStatus.ACTIVITY));

        // when
        commentService.deleteComment(comment.getCommentId(), user);

        // then
        assertThat(commentRepository.existsById(comment.getCommentId())).isFalse();
    }

    @DisplayName("댓글 작성자와 다른 사람이 삭제 시, 실패")
    @Test
    @Order(6)
    void deleteCommentFailure() {
        // given
        CustomUser2Member user = new CustomUser2Member(
            new CustomUserDetails(999999L, "", "",
                "", "", MemberRole.USER, MemberStatus.ACTIVITY));

        // when & then
        assertThatThrownBy(() -> {
            commentService.deleteComment(comment.getCommentId(), user);
        }).isExactlyInstanceOf(BusinessException.class)
            .hasMessage(CommentErrorCode.COMMENT_ELIMINATOR_NOT_AUTHORIZED.getMessage());
    }

    @DisplayName("관리자 댓글 삭제 성공")
    @Test
    @Order(7)
    void deleteCommentByAdmin() {
        // given
        Long feedbackId = feedback.getFeedbackId();
        String content = "test admin comment content";
        CommentRequest request = new CommentRequest(feedbackId, content);
        CustomUser2Member user = new CustomUser2Member(
            new CustomUserDetails(admin.getMemberId(), "", "",
                "", "", MemberRole.ADMIN, MemberStatus.ACTIVITY));

        // when
        commentService.writeComment(request, user);
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