package org.programmers.signalbuddyfinal.domain.comment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.programmers.signalbuddyfinal.domain.comment.dto.CommentRequest;
import org.programmers.signalbuddyfinal.domain.comment.entity.Comment;
import org.programmers.signalbuddyfinal.domain.comment.exception.CommentErrorCode;
import org.programmers.signalbuddyfinal.domain.comment.repository.CommentRepository;
import org.programmers.signalbuddyfinal.domain.crossroad.entity.Crossroad;
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
import org.springframework.test.util.ReflectionTestUtils;

class CommentServiceTest extends ServiceTest {

    @InjectMocks
    private CommentService commentService;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private FeedbackRepository feedbackRepository;

    @Mock
    private FcmService fcmService;

    private Member member;
    private Member admin;
    private Feedback feedback;
    private Comment comment;

    @BeforeEach
    void setup() {
        member = createMember(1L, "test@test.com", "tester", MemberRole.USER);
        admin = createMember(7777L, "admin@test.com", "admin", MemberRole.ADMIN);
        feedback = createFeedback(member);
        comment = createComment(1L, "test comment content", member, feedback);
    }

    @DisplayName("일반 사용자가 자신의 피드백이 아닌 글에 댓글을 작성한다.")
    @Test
    void writeComment() {
        // given
        Long feedbackId = feedback.getFeedbackId();
        CommentRequest request = new CommentRequest("test comment content");
        Member otherMember = createMember(
            2L, "other@test.com", "other tester",
            MemberRole.USER
        );
        CustomUser2Member requestUser = createCurrentMember(
            otherMember.getMemberId(), MemberRole.USER
        );

        given(memberRepository.findByIdOrThrow(requestUser.getMemberId()))
            .willReturn(otherMember);
        given(feedbackRepository.findByIdOrThrow(feedbackId))
            .willReturn(feedback);
        given(commentRepository.save(any(Comment.class)))
            .willReturn(createComment(2L, request.getContent(), otherMember, feedback));
        doNothing().when(fcmService).sendMessage(any(FcmMessage.class), anyLong());

        // when
        commentService.writeComment(feedbackId, request, requestUser);

        // then
        verify(commentRepository, times(1))
            .save(any(Comment.class));
        verify(fcmService, times(1))
            .sendMessage(any(FcmMessage.class), anyLong());
    }

    @DisplayName("사용자가 자신의 피드백에 댓글을 남긴다.")
    @Test
    void writeComment_SameWriter() {
        // given
        Long feedbackId = feedback.getFeedbackId();
        String content = "test comment content";
        CommentRequest request = new CommentRequest(content);
        CustomUser2Member requestUser = createCurrentMember(member.getMemberId(), MemberRole.USER);

        given(memberRepository.findByIdOrThrow(requestUser.getMemberId()))
            .willReturn(member);
        given(feedbackRepository.findByIdOrThrow(feedbackId))
            .willReturn(feedback);
        given(commentRepository.save(any(Comment.class)))
            .willReturn(createComment(2L, request.getContent(), member, feedback));
        doNothing().when(fcmService).sendMessage(any(FcmMessage.class), anyLong());

        // when
        commentService.writeComment(feedbackId, request, requestUser);

        // then
        verify(commentRepository, times(1))
            .save(any(Comment.class));
        verify(fcmService, times(0))
            .sendMessage(any(FcmMessage.class), anyLong());
    }

    @DisplayName("피드백 작성자가 알림 설정을 허용하지 않아, 알림이 전송되지 않는다.")
    @Test
    void writeComment_NotiDisabled() {
        // given
        Long feedbackId = feedback.getFeedbackId();
        String content = "test comment content";
        CommentRequest request = new CommentRequest(content);
        Member otherMember = createMember(
            2L, "other@test.com", "other tester",
            MemberRole.USER
        );
        member.updateNotifyEnabled(Boolean.FALSE);
        CustomUser2Member requestUser = createCurrentMember(
            otherMember.getMemberId(), MemberRole.USER
        );

        given(memberRepository.findByIdOrThrow(requestUser.getMemberId()))
            .willReturn(otherMember);
        given(feedbackRepository.findByIdOrThrow(feedbackId))
            .willReturn(feedback);
        given(commentRepository.save(any(Comment.class)))
            .willReturn(createComment(2L, request.getContent(), otherMember, feedback));
        doNothing().when(fcmService).sendMessage(any(FcmMessage.class), anyLong());

        // when
        commentService.writeComment(feedbackId, request, requestUser);

        // then
        verify(commentRepository, times(1))
            .save(any(Comment.class));
        verify(fcmService, times(0))
            .sendMessage(any(FcmMessage.class), anyLong());
    }

    @DisplayName("관리자가 댓글(답변)을 작성한다.")
    @Test
    void writeCommentByAdmin() {
        // given
        Long feedbackId = feedback.getFeedbackId();
        String content = "test admin comment content";
        CommentRequest request = new CommentRequest(content);
        CustomUser2Member requestAdmin = createCurrentMember(admin.getMemberId(), MemberRole.ADMIN);

        given(memberRepository.findByIdOrThrow(requestAdmin.getMemberId()))
            .willReturn(admin);
        given(feedbackRepository.findByIdOrThrow(feedbackId))
            .willReturn(feedback);
        given(commentRepository.save(any(Comment.class)))
            .willReturn(createComment(2L, request.getContent(), admin, feedback));
        doNothing().when(fcmService).sendMessage(any(FcmMessage.class), anyLong());

        // when
        commentService.writeComment(feedbackId, request, requestAdmin);

        // then
        verify(commentRepository, times(1))
            .save(any(Comment.class));
        verify(fcmService, times(1))
            .sendMessage(any(FcmMessage.class), anyLong());
        assertThat(feedback.getAnswerStatus()).isEqualTo(AnswerStatus.COMPLETION);
    }

    @DisplayName("본인의 댓글을 수정한다.")
    @Test
    void updateComment() {
        // given
        String updatedContent = "update comment content";
        CommentRequest request = new CommentRequest(updatedContent);
        CustomUser2Member requestUser = createCurrentMember(member.getMemberId(), MemberRole.USER);

        given(commentRepository.findByIdOrThrow(requestUser.getMemberId()))
            .willReturn(comment);

        // when
        commentService.updateComment(comment.getCommentId(), request, requestUser);

        // then
        assertThat(comment.getContent()).isEqualTo(updatedContent);
    }

    @DisplayName("댓글 작성자와 다른 사람이 수정 시, 실패한다.")
    @Test
    void updateCommentFailure() {
        // given
        String updatedContent = "update comment content";
        CommentRequest request = new CommentRequest(updatedContent);
        Member otherMember = createMember(
            2L, "other@test.com", "other tester",
            MemberRole.USER
        );
        CustomUser2Member requestUser = createCurrentMember(
            otherMember.getMemberId(), MemberRole.USER
        );

        given(commentRepository.findByIdOrThrow(comment.getCommentId()))
            .willReturn(comment);

        // when & then
        try {
            commentService.updateComment(comment.getCommentId(), request, requestUser);
        } catch (BusinessException e) {
            assertThat(e.getErrorCode())
                .isEqualTo(CommentErrorCode.COMMENT_MODIFIER_NOT_AUTHORIZED);
        }
    }

    @DisplayName("일반 사용자가 본인 댓글을 삭제한다.")
    @Test
    void deleteComment() {
        // given
        Long commentId = comment.getCommentId();
        CustomUser2Member requestUser = createCurrentMember(member.getMemberId(), MemberRole.USER);

        given(commentRepository.findByIdOrThrow(commentId))
            .willReturn(comment);

        // when
        commentService.deleteComment(commentId, requestUser);

        // then
        verify(commentRepository, times(1))
            .deleteById(commentId);
    }

    @DisplayName("관리자가 일반 사용자의 댓글을 삭제한다.")
    @Test
    void deleteCommentByAdmin() {
        // given
        Long commentId = comment.getCommentId();
        CustomUser2Member requestAdmin = createCurrentMember(admin.getMemberId(), MemberRole.ADMIN);

        given(commentRepository.findByIdOrThrow(commentId))
            .willReturn(comment);

        // when
        commentService.deleteComment(commentId, requestAdmin);

        // then
        verify(commentRepository, times(1))
            .deleteById(commentId);
    }

    @DisplayName("댓글 작성자와 다른 사람이 삭제 시, 실패한다.")
    @Test
    void deleteCommentFailure() {
        // given
        Long commentId = comment.getCommentId();
        CustomUser2Member requestUser = createCurrentMember(999999L, MemberRole.USER);

        given(commentRepository.findByIdOrThrow(commentId))
            .willReturn(comment);

        // when & then
        try {
            commentService.deleteComment(commentId, requestUser);
        } catch (BusinessException e) {
            assertThat(e.getErrorCode())
                .isEqualTo(CommentErrorCode.COMMENT_ELIMINATOR_NOT_AUTHORIZED);
        }
    }

    @DisplayName("관리자 본인의 댓글(답변)을 삭제한다.")
    @Test
    void deleteAdminComment() {
        // given
        feedback.updateFeedbackStatus();    // AnswerStatus : BEFORE -> COMPLETION
        Comment commentByAdmin = createComment(2L, "by admin", admin, feedback);
        Long commentId = commentByAdmin.getCommentId();
        CustomUser2Member requestAdmin = createCurrentMember(admin.getMemberId(), MemberRole.ADMIN);

        given(commentRepository.findByIdOrThrow(commentId))
            .willReturn(commentByAdmin);

        // when
        commentService.deleteComment(commentId, requestAdmin);

        // then
        verify(commentRepository, times(1))
            .deleteById(commentId);
        assertThat(feedback.getAnswerStatus())
            .isEqualTo(AnswerStatus.BEFORE);
    }

    private Member createMember(Long id, String email, String nickname, MemberRole role) {
        return Member.builder()
            .memberId(id).email(email).password("123456").role(role)
            .nickname(nickname).memberStatus(MemberStatus.ACTIVITY)
            .profileImageUrl("https://test-image.com/test-123131")
            .build();
    }

    private Crossroad createCrossroad() {
        Crossroad entity = Crossroad.create()
            .crossroadApiId("13214").name("00사거리")
            .lat(37.12222).lng(127.12132)
            .build();
        ReflectionTestUtils.setField(entity, "crossroadId", 1L);

        return entity;
    }

    private Feedback createFeedback(Member member) {
        Feedback entity = Feedback.create()
            .subject("test subject").content("test content").secret(Boolean.FALSE)
            .category(FeedbackCategory.ETC).member(member)
            .crossroad(createCrossroad())
            .build();
        ReflectionTestUtils.setField(entity, "feedbackId", 1L);

        return entity;
    }

    private Comment createComment(Long id, String content, Member member, Feedback feedback) {
        Comment entity = Comment.create()
            .content(content).feedback(feedback).member(member)
            .build();
        ReflectionTestUtils.setField(entity, "commentId", id);

        return entity;
    }

    private CustomUser2Member createCurrentMember(Long id, MemberRole role) {
        return new CustomUser2Member(
            new CustomUserDetails(id, "", "",
                "", "", role, MemberStatus.ACTIVITY));
    }
}
