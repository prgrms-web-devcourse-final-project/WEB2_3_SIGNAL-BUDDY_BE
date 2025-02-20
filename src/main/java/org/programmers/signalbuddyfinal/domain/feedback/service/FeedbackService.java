package org.programmers.signalbuddyfinal.domain.feedback.service;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.programmers.signalbuddyfinal.domain.comment.repository.CommentRepository;
import org.programmers.signalbuddyfinal.domain.feedback.dto.FeedbackMapper;
import org.programmers.signalbuddyfinal.domain.feedback.dto.FeedbackResponse;
import org.programmers.signalbuddyfinal.domain.feedback.dto.FeedbackWriteRequest;
import org.programmers.signalbuddyfinal.domain.feedback.entity.Feedback;
import org.programmers.signalbuddyfinal.domain.feedback.exception.FeedbackErrorCode;
import org.programmers.signalbuddyfinal.domain.feedback.repository.FeedbackJdbcRepository;
import org.programmers.signalbuddyfinal.domain.feedback.repository.FeedbackRepository;
import org.programmers.signalbuddyfinal.domain.like.repository.LikeRepository;
import org.programmers.signalbuddyfinal.domain.member.entity.Member;
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
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final MemberRepository memberRepository;
    private final FeedbackJdbcRepository feedbackJdbcRepository;
    private final CommentRepository commentRepository;
    private final LikeRepository likeRepository;

    public PageResponse<FeedbackResponse> searchFeedbackList(Pageable pageable, Long answerStatus) {
        Page<FeedbackResponse> responsePage = feedbackRepository.findAllByActiveMembers(pageable,
            answerStatus);
        return new PageResponse<>(responsePage);
    }

    public PageResponse<FeedbackResponse> searchFeedbackList(
        Pageable pageable,
        LocalDate startDate, LocalDate endDate,
        Long answerStatus
    ) {
        Page<FeedbackResponse> responsePage = feedbackRepository.findAll(pageable, startDate,
            endDate, answerStatus);
        return new PageResponse<>(responsePage);
    }

    public PageResponse<FeedbackResponse> searchByKeyword(
        Pageable pageable, String keyword, Long answerStatus
    ) {
        Page<FeedbackResponse> responsePage = feedbackJdbcRepository.fullTextSearch(pageable,
            keyword, answerStatus);
        return new PageResponse<>(responsePage);
    }

    public FeedbackResponse searchFeedbackDetail(Long feedbackId) {
        Feedback feedback = feedbackRepository.findByIdOrThrow(feedbackId);
        return FeedbackMapper.INSTANCE.toResponse(feedback);
    }

    @Transactional
    public FeedbackResponse writeFeedback(FeedbackWriteRequest request, CustomUser2Member user) {
        Member member = memberRepository.findByIdOrThrow(user.getMemberId());

        Feedback feedback = Feedback.create()
            .subject(request.getSubject()).content(request.getContent()).member(member)
            .build();
        Feedback savedFeedback = feedbackRepository.save(feedback);

        return FeedbackMapper.INSTANCE.toResponse(savedFeedback);
    }

    @Transactional
    public void updateFeedback(Long feedbackId, FeedbackWriteRequest request, CustomUser2Member user) {
        Feedback feedback = feedbackRepository.findByIdOrThrow(feedbackId);

        // 피드백 작성자와 수정 요청자가 다른 경우
        if (Member.isNotSameMember(user, feedback.getMember())) {
            throw new BusinessException(FeedbackErrorCode.FEEDBACK_MODIFIER_NOT_AUTHORIZED);
        }

        feedback.updateFeedback(request);
    }

    @Transactional
    public void deleteFeedback(Long feedbackId, CustomUser2Member user) {
        Feedback feedback = feedbackRepository.findByIdOrThrow(feedbackId);

        // 피드백 작성자와 삭제 요청자가 다른 경우
        if (Member.isNotSameMember(user, feedback.getMember())) {
            throw new BusinessException(FeedbackErrorCode.FEEDBACK_ELIMINATOR_NOT_AUTHORIZED);
        }

        commentRepository.softDeleteAllByFeedbackId(feedbackId);
        likeRepository.deleteAllByFeedbackId(feedbackId);
        feedbackRepository.deleteById(feedbackId);
    }

    public Page<FeedbackResponse> findPagedFeedbacksByMember(Long memberId, Pageable pageable) {
        return feedbackRepository.findPagedByMember(memberId, pageable);
    }
}
