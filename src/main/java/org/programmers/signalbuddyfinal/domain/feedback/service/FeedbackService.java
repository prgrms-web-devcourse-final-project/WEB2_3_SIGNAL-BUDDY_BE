package org.programmers.signalbuddyfinal.domain.feedback.service;

import lombok.RequiredArgsConstructor;
import org.programmers.signalbuddyfinal.domain.comment.repository.CommentRepository;
import org.programmers.signalbuddyfinal.domain.feedback.dto.FeedbackMapper;
import org.programmers.signalbuddyfinal.domain.feedback.dto.FeedbackResponse;
import org.programmers.signalbuddyfinal.domain.feedback.dto.FeedbackWriteRequest;
import org.programmers.signalbuddyfinal.domain.feedback.entity.Feedback;
import org.programmers.signalbuddyfinal.domain.feedback.exception.FeedbackErrorCode;
import org.programmers.signalbuddyfinal.domain.feedback.repository.FeedbackJdbcRepository;
import org.programmers.signalbuddyfinal.domain.feedback.repository.FeedbackRepository;
import org.programmers.signalbuddyfinal.domain.member.entity.Member;
import org.programmers.signalbuddyfinal.domain.member.exception.MemberErrorCode;
import org.programmers.signalbuddyfinal.domain.member.repository.MemberRepository;
import org.programmers.signalbuddyfinal.global.dto.CustomUser2Member;
import org.programmers.signalbuddyfinal.global.dto.PageResponse;
import org.programmers.signalbuddyfinal.global.exception.BusinessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final MemberRepository memberRepository;
    private final FeedbackJdbcRepository feedbackJdbcRepository;
    private final CommentRepository commentRepository;

    public PageResponse<FeedbackResponse> searchFeedbackList(Pageable pageable, Long answerStatus) {
        Page<FeedbackResponse> responsePage = feedbackRepository.findAllByActiveMembers(pageable,
            answerStatus);
        return new PageResponse<>(responsePage);
    }

    public PageResponse<FeedbackResponse> searchFeedbackList(Pageable pageable,
        LocalDate startDate, LocalDate endDate, Long answerStatus) {
        Page<FeedbackResponse> responsePage = feedbackRepository.findAll(pageable, startDate,
            endDate, answerStatus);
        return new PageResponse<>(responsePage);
    }

    public PageResponse<FeedbackResponse> searchByKeyword(Pageable pageable, String keyword,
        Long answerStatus) {
        Page<FeedbackResponse> responsePage = feedbackJdbcRepository.fullTextSearch(pageable,
            keyword, answerStatus);
        return new PageResponse<>(responsePage);
    }

    public FeedbackResponse searchFeedbackDetail(Long feedbackId) {
        Feedback feedback = feedbackRepository.findById(feedbackId)
            .orElseThrow(() -> new BusinessException(FeedbackErrorCode.NOT_FOUND_FEEDBACK));
        return FeedbackMapper.INSTANCE.toResponse(feedback);
    }

    @Transactional
    public FeedbackResponse writeFeedback(FeedbackWriteRequest request, CustomUser2Member user) {
        Member member = memberRepository.findById(user.getMemberId())
            .orElseThrow(() -> new BusinessException(MemberErrorCode.NOT_FOUND_MEMBER));

        Feedback feedback = Feedback.create(request, member);
        Feedback savedFeedback = feedbackRepository.save(feedback);

        return FeedbackMapper.INSTANCE.toResponse(savedFeedback);
    }

    @Transactional
    public void updateFeedback(Long feedbackId, FeedbackWriteRequest request, CustomUser2Member user) {
        Feedback feedback = feedbackRepository.findById(feedbackId)
            .orElseThrow(() -> new BusinessException(FeedbackErrorCode.NOT_FOUND_FEEDBACK));

        // 피드백 작성자와 수정 요청자가 다른 경우
        if (Member.isNotSameMember(user, feedback.getMember())) {
            throw new BusinessException(FeedbackErrorCode.FEEDBACK_MODIFIER_NOT_AUTHORIZED);
        }

        feedback.updateFeedback(request);
    }

    @Transactional
    public void deleteFeedback(Long feedbackId, CustomUser2Member user) {
        Feedback feedback = feedbackRepository.findById(feedbackId)
            .orElseThrow(() -> new BusinessException(FeedbackErrorCode.NOT_FOUND_FEEDBACK));

        // 피드백 작성자와 삭제 요청자가 다른 경우
        if (Member.isNotSameMember(user, feedback.getMember())) {
            throw new BusinessException(FeedbackErrorCode.FEEDBACK_ELIMINATOR_NOT_AUTHORIZED);
        }

        commentRepository.deleteAllByFeedbackId(feedbackId);
        feedbackRepository.deleteById(feedbackId);
    }

    public Page<FeedbackResponse> findPagedFeedbacksByMember(Long memberId, Pageable pageable) {
        return feedbackRepository.findPagedByMember(memberId, pageable);
    }
}
