package org.programmers.signalbuddyfinal.domain.comment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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
import org.programmers.signalbuddyfinal.domain.notification.dto.FcmMessage;
import org.programmers.signalbuddyfinal.domain.notification.service.FcmService;
import org.programmers.signalbuddyfinal.global.dto.CustomUser2Member;
import org.programmers.signalbuddyfinal.global.exception.BusinessException;
import org.programmers.signalbuddyfinal.global.security.basic.CustomUserDetails;
import org.programmers.signalbuddyfinal.global.support.ServiceTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
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

    @MockitoBean
    private FcmService fcmService;

    private Member member;
    private Member admin;
    private Feedback feedback;
    private Comment comment;

    @BeforeEach
    void setup() {
        member = saveMember("test@test.com", "tester");
        admin = saveAdmin("admin@test.com", "admin");

        Crossroad crossroad = saveCrossroad("13214", "00사거리", 37.12222, 127.12132);

        feedback = saveFeedback("test subject", "test content", member, crossroad);

        comment = saveComment("test comment content", member, feedback);
    }

    @DisplayName("일반 사용자가 자신의 피드백이 아닌 글에 댓글을 작성한다.")
    @Test
    @Order(1)
    void writeComment() {
        // given
        Long feedbackId = feedback.getFeedbackId();
        Member otherMember = saveMember("other@test.com", "other tester");
        String content = "test comment content";
        CommentRequest request = new CommentRequest(content);
        CustomUser2Member user = getCurrentMember(otherMember.getMemberId(), MemberRole.USER);

        doNothing().when(fcmService).sendMessage(any(FcmMessage.class), anyLong());

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
        verify(fcmService, times(1))
            .sendMessage(any(FcmMessage.class), anyLong());
    }

    @DisplayName("사용자가 자신의 피드백에 댓글을 남긴다.")
    @Test
    @Order(2)
    void writeComment_SameWriter() {
        // given
        Long feedbackId = feedback.getFeedbackId();
        String content = "test comment content";
        CommentRequest request = new CommentRequest(content);
        CustomUser2Member user = getCurrentMember(member.getMemberId(), MemberRole.USER);

        doNothing().when(fcmService).sendMessage(any(FcmMessage.class), anyLong());

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
        verify(fcmService, times(0))
            .sendMessage(any(FcmMessage.class), anyLong());
    }

    @DisplayName("피드백 작성자가 알림 설정을 허용하지 않아, 알림이 전송되지 않는다.")
    @Test
    @Order(3)
    void writeComment_NotiDisabled() {
        // given
        Long feedbackId = feedback.getFeedbackId();
        Member otherMember = saveMember("other@test.com", "other tester");
        String content = "test comment content";
        CommentRequest request = new CommentRequest(content);
        CustomUser2Member user = getCurrentMember(otherMember.getMemberId(), MemberRole.USER);
        member.updateNotifyEnabled(Boolean.FALSE);

        doNothing().when(fcmService).sendMessage(any(FcmMessage.class), anyLong());

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
        verify(fcmService, times(0))
            .sendMessage(any(FcmMessage.class), anyLong());
    }

    @DisplayName("관리자가 댓글(답변)을 작성한다.")
    @Test
    @Order(4)
    void writeCommentByAdmin() {
        // given
        Long feedbackId = feedback.getFeedbackId();
        String content = "test admin comment content";
        CommentRequest request = new CommentRequest(content);
        CustomUser2Member user = getCurrentMember(admin.getMemberId(), MemberRole.ADMIN);

        doNothing().when(fcmService).sendMessage(any(FcmMessage.class), anyLong());

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
        verify(fcmService, times(1))
            .sendMessage(any(FcmMessage.class), anyLong());
    }

    @DisplayName("본인의 댓글을 수정한다.")
    @Test
    @Order(5)
    void updateComment() {
        // given
        String updatedContent = "update comment content";
        CommentRequest request = new CommentRequest(updatedContent);
        CustomUser2Member user = getCurrentMember(member.getMemberId(), MemberRole.USER);

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
    @Order(6)
    void updateCommentFailure() {
        // given
        String updatedContent = "update comment content";
        CommentRequest request = new CommentRequest(updatedContent);
        CustomUser2Member user = getCurrentMember(999999L, MemberRole.USER);

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
    @Order(7)
    void deleteComment() {
        // given
        CustomUser2Member user = getCurrentMember(member.getMemberId(), MemberRole.USER);

        // when
        commentService.deleteComment(comment.getCommentId(), user);

        // then
        assertThat(commentRepository.existsById(comment.getCommentId())).isFalse();
    }

    @DisplayName("관리자가 일반 사용자의 댓글을 삭제한다.")
    @Test
    @Order(8)
    void deleteCommentByAdmin() {
        // given
        CustomUser2Member user = getCurrentMember(admin.getMemberId(), MemberRole.ADMIN);

        // when
        commentService.deleteComment(comment.getCommentId(), user);

        // then
        assertThat(commentRepository.existsById(comment.getCommentId())).isFalse();
    }

    @DisplayName("댓글 작성자와 다른 사람이 삭제 시, 실패한다.")
    @Test
    @Order(9)
    void deleteCommentFailure() {
        // given
        CustomUser2Member user = getCurrentMember(999999L, MemberRole.USER);

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
    @Order(10)
    void deleteAdminComment() {
        // given
        Long feedbackId = feedback.getFeedbackId();
        String content = "test admin comment content";
        CommentRequest request = new CommentRequest(content);
        CustomUser2Member user = getCurrentMember(admin.getMemberId(), MemberRole.ADMIN);

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

    private Member saveMember(String email, String nickname) {
        return memberRepository.save(
            Member.builder().email(email).password("123456").role(MemberRole.USER)
                .nickname(nickname).memberStatus(MemberStatus.ACTIVITY)
                .profileImageUrl("https://test-image.com/test-123131").build());
    }

    private Member saveAdmin(String email, String nickname) {
        return memberRepository.save(
            Member.builder().email(email).password("123456").role(MemberRole.ADMIN)
                .nickname(nickname).memberStatus(MemberStatus.ACTIVITY)
                .profileImageUrl("https://test-image.com/test-123131").build());
    }

    private Crossroad saveCrossroad(String apiId, String name, double lat, double lng) {
        return crossroadRepository.save(new Crossroad(
            CrossroadApiResponse.builder().crossroadApiId(apiId).name(name).lat(lat).lng(lng)
                .build()));
    }

    private Feedback saveFeedback(String subject, String content, Member member, Crossroad crossroad) {
        return feedbackRepository.save(
            Feedback.create().subject(subject).content(content).secret(Boolean.FALSE)
                .category(FeedbackCategory.ETC).member(member).crossroad(crossroad).build());
    }

    private Comment saveComment(String content, Member member, Feedback feedback) {
        return commentRepository.save(
            Comment.create().content(content).feedback(feedback).member(member).build());
    }

    private CustomUser2Member getCurrentMember(Long id, MemberRole role) {
        return new CustomUser2Member(
            new CustomUserDetails(id, "", "",
                "", "", role, MemberStatus.ACTIVITY));
    }
}