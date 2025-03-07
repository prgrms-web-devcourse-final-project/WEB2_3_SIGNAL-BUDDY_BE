package org.programmers.signalbuddyfinal.domain.feedback.service;

import lombok.RequiredArgsConstructor;
import org.programmers.signalbuddyfinal.domain.comment.repository.CommentRepository;
import org.programmers.signalbuddyfinal.domain.crossroad.entity.Crossroad;
import org.programmers.signalbuddyfinal.domain.crossroad.repository.CrossroadRepository;
import org.programmers.signalbuddyfinal.domain.feedback.dto.FeedbackRequest;
import org.programmers.signalbuddyfinal.domain.feedback.dto.FeedbackResponse;
import org.programmers.signalbuddyfinal.domain.feedback.dto.FeedbackSearchRequest;
import org.programmers.signalbuddyfinal.domain.feedback.entity.Feedback;
import org.programmers.signalbuddyfinal.domain.feedback.exception.FeedbackErrorCode;
import org.programmers.signalbuddyfinal.domain.feedback.mapper.FeedbackMapper;
import org.programmers.signalbuddyfinal.domain.feedback.repository.FeedbackRepository;
import org.programmers.signalbuddyfinal.domain.feedback_report.repository.FeedbackReportRepository;
import org.programmers.signalbuddyfinal.domain.like.repository.LikeRepository;
import org.programmers.signalbuddyfinal.domain.member.entity.Member;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberRole;
import org.programmers.signalbuddyfinal.domain.member.repository.MemberRepository;
import org.programmers.signalbuddyfinal.global.constant.SearchTarget;
import org.programmers.signalbuddyfinal.global.dto.CustomUser2Member;
import org.programmers.signalbuddyfinal.global.dto.PageResponse;
import org.programmers.signalbuddyfinal.global.exception.BusinessException;
import org.programmers.signalbuddyfinal.global.service.AwsFileService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final MemberRepository memberRepository;
    private final CommentRepository commentRepository;
    private final LikeRepository likeRepository;
    private final CrossroadRepository crossroadRepository;
    private final FeedbackReportRepository reportRepository;
    private final AwsFileService awsFileService;

    @Value("${cloud.aws.s3.folder.feedback}")
    private String feedbackDir;

    public PageResponse<FeedbackResponse> searchFeedbackList(
        Pageable pageable,
        SearchTarget target,
        FeedbackSearchRequest request,
        Long crossroadId
    ) {
        return new PageResponse<>(
            feedbackRepository.findAllByActiveMembers(
                pageable, target, request.getStatus(), request.getCategory(),
                crossroadId, request.getKeyword()
            )
        );
    }

    public PageResponse<FeedbackResponse> findPagedExcludingMember(
        Long memberId, Pageable pageable
    ) {
        return new PageResponse<>(
            feedbackRepository.findPagedExcludingMember(memberId, pageable)
        );
    }

    public FeedbackResponse searchFeedbackDetail(
        Long feedbackId, CustomUser2Member user
    ) {
        Feedback feedback = feedbackRepository.findByIdOrThrow(feedbackId);

        // 비밀글일 때
        // 피드백 작성자와 조회 요청자가 다른 경우 (관리자는 모든 피드백을 조회할 수 있음)
        if (Boolean.TRUE.equals(feedback.getSecret())
            && Member.isNotSameMember(user, feedback.getMember())
            && !MemberRole.ADMIN.equals(user.getRole())) {
            throw new BusinessException(FeedbackErrorCode.SECRET_FEEDBACK_NOT_AUTHORIZED);
        }

        return FeedbackMapper.INSTANCE.toResponse(feedback);
    }

    @Transactional
    public FeedbackResponse writeFeedback(
        FeedbackRequest request, MultipartFile image,
        CustomUser2Member user
    ) {
        Member member = memberRepository.findByIdOrThrow(user.getMemberId());
        Crossroad crossroad = crossroadRepository.findByIdOrThrow(request.getCrossroadId());

        String imageUrl = null;
        if (image != null) {
            String fileName = awsFileService.uploadFileToS3(image, feedbackDir);
            imageUrl = awsFileService.getFileFromS3(fileName, feedbackDir).toString();
        }

        Feedback feedback = Feedback.create()
            .subject(request.getSubject()).content(request.getContent())
            .category(request.getCategory()).secret(request.getSecret())
            .member(member).crossroad(crossroad).imageUrl(imageUrl)
            .build();
        Feedback savedFeedback = feedbackRepository.save(feedback);

        return FeedbackMapper.INSTANCE.toResponse(savedFeedback);
    }

    @Transactional
    public FeedbackResponse updateFeedback(
        Long feedbackId,
        FeedbackRequest request, MultipartFile image,
        CustomUser2Member user
    ) {
        Feedback feedback = feedbackRepository.findByIdOrThrow(feedbackId);

        // 피드백 작성자와 수정 요청자가 다른 경우
        if (Member.isNotSameMember(user, feedback.getMember())) {
            throw new BusinessException(FeedbackErrorCode.FEEDBACK_MODIFIER_NOT_AUTHORIZED);
        }

        feedback.updateFeedback(request);

        if (!feedback.getCrossroad().getCrossroadId().equals(request.getCrossroadId())) {
            Crossroad crossroad = crossroadRepository.findByIdOrThrow(request.getCrossroadId());
            feedback.updateCrossroad(crossroad);
        }

        if (image != null) {
            String fileName = awsFileService.uploadFileToS3(image, feedbackDir);
            String imageUrl = awsFileService.getFileFromS3(fileName, feedbackDir).toString();
            feedback.updateImageUrl(imageUrl);
        }

        return FeedbackMapper.INSTANCE.toResponse(feedback);
    }

    @Transactional
    public void deleteFeedback(
        Long feedbackId, CustomUser2Member user
    ) {
        Feedback feedback = feedbackRepository.findByIdOrThrow(feedbackId);

        // 피드백 작성자와 삭제 요청자가 다른 경우 (관리자는 모든 피드백을 삭제할 수 있음)
        if (Member.isNotSameMember(user, feedback.getMember())
            && !MemberRole.ADMIN.equals(user.getRole())) {
            throw new BusinessException(FeedbackErrorCode.FEEDBACK_ELIMINATOR_NOT_AUTHORIZED);
        }

        commentRepository.softDeleteAllByFeedbackId(feedbackId);
        likeRepository.deleteAllByFeedbackId(feedbackId);
        reportRepository.deleteAllByFeedbackId(feedbackId);
        feedbackRepository.deleteById(feedbackId);
    }
}
