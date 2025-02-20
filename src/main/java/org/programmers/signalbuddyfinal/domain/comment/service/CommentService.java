package org.programmers.signalbuddyfinal.domain.comment.service;

import lombok.RequiredArgsConstructor;
import org.programmers.signalbuddyfinal.domain.comment.dto.CommentRequest;
import org.programmers.signalbuddyfinal.domain.comment.dto.CommentResponse;
import org.programmers.signalbuddyfinal.domain.comment.entity.Comment;
import org.programmers.signalbuddyfinal.domain.comment.exception.CommentErrorCode;
import org.programmers.signalbuddyfinal.domain.comment.repository.CommentRepository;
import org.programmers.signalbuddyfinal.domain.feedback.entity.Feedback;
import org.programmers.signalbuddyfinal.domain.feedback.repository.FeedbackRepository;
import org.programmers.signalbuddyfinal.domain.member.entity.Member;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberRole;
import org.programmers.signalbuddyfinal.domain.member.repository.MemberRepository;
import org.programmers.signalbuddyfinal.global.dto.CustomUser2Member;
import org.programmers.signalbuddyfinal.global.dto.PageResponse;
import org.programmers.signalbuddyfinal.global.exception.BusinessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final MemberRepository memberRepository;
    private final FeedbackRepository feedbackRepository;

    @Transactional
    public void writeComment(Long feedbackId, CommentRequest request, CustomUser2Member user) {
        Member member = memberRepository.findByIdOrThrow(user.getMemberId());
        Feedback feedback = feedbackRepository.findByIdOrThrow(feedbackId);

        Comment comment = Comment.create()
            .content(request.getContent())
            .feedback(feedback).member(member)
            .build();

        // 관리자일 때 피드백 상태 변경
        if (comment.getMember().isAdmin()) {
            feedback.updateFeedbackStatus();
        }

        commentRepository.save(comment);
    }

    public PageResponse<CommentResponse> searchCommentList(Long feedbackId, Pageable pageable) {
        Page<CommentResponse> responsePage = commentRepository.findAllByFeedbackId(
            feedbackId, pageable
        );
        return new PageResponse<>(responsePage);
    }

    @Transactional
    public void updateComment(Long commentId, CommentRequest request, CustomUser2Member user) {
        Comment comment = commentRepository.findByIdOrThrow(commentId);

        // 수정 요청자와 댓글 작성자 다른 경우
        if (Member.isNotSameMember(user, comment.getMember())) {
            throw new BusinessException(CommentErrorCode.COMMENT_MODIFIER_NOT_AUTHORIZED);
        }

        comment.updateContent(request);
    }

    @Transactional
    public void deleteComment(Long commentId, CustomUser2Member user) {
        Comment comment = commentRepository.findByIdOrThrow(commentId);

        // 삭제 요청자와 댓글 작성자 다른 경우 (관리자는 모든 댓글을 삭제할 수 있음)
        if (Member.isNotSameMember(user, comment.getMember())
            && !MemberRole.ADMIN.equals(user.getRole())) {
            throw new BusinessException(CommentErrorCode.COMMENT_ELIMINATOR_NOT_AUTHORIZED);
        }

        // 관리자일 때 피드백 상태 변경
        if (comment.getMember().isAdmin()) {
            comment.getFeedback().updateFeedbackStatus();
        }

        commentRepository.deleteById(commentId);
    }
}
