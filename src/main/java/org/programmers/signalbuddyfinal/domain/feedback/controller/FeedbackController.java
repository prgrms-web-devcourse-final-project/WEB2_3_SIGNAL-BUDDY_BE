package org.programmers.signalbuddyfinal.domain.feedback.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.programmers.signalbuddyfinal.domain.feedback.dto.FeedbackRequest;
import org.programmers.signalbuddyfinal.domain.feedback.dto.FeedbackResponse;
import org.programmers.signalbuddyfinal.domain.feedback.dto.FeedbackSearchRequest;
import org.programmers.signalbuddyfinal.domain.feedback.service.FeedbackService;
import org.programmers.signalbuddyfinal.global.annotation.CurrentUser;
import org.programmers.signalbuddyfinal.global.constant.SearchTarget;
import org.programmers.signalbuddyfinal.global.dto.CustomUser2Member;
import org.programmers.signalbuddyfinal.global.dto.PageResponse;
import org.programmers.signalbuddyfinal.global.response.ApiResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/feedbacks")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    @PostMapping
    public ResponseEntity<ApiResponse<FeedbackResponse>> writeFeedback(
        @Valid @RequestPart("request") FeedbackRequest request,
        @RequestPart(value = "imageFile", required = false) MultipartFile image,
        @CurrentUser CustomUser2Member user
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(
                ApiResponse.createSuccess(
                    feedbackService.writeFeedback(request, image, user)
                )
            );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<FeedbackResponse>>> searchFeedbackList(
        @PageableDefault Pageable pageable,
        @RequestParam(value = "target", defaultValue = "content") SearchTarget target,
        @ModelAttribute FeedbackSearchRequest request,
        @RequestParam(value = "crossroadId", required = false) Long crossroadId
    ) {
        return ResponseEntity.ok(
            ApiResponse.createSuccess(
                feedbackService.searchFeedbackList(
                    pageable, target, request, crossroadId
                )
            )
        );
    }

    @GetMapping("/{feedbackId}")
    public ResponseEntity<ApiResponse<FeedbackResponse>> searchFeedbackDetail(
        @PathVariable("feedbackId") long feedbackId,
        @CurrentUser CustomUser2Member user
    ) {
        return ResponseEntity.ok(
            ApiResponse.createSuccess(
                feedbackService.searchFeedbackDetail(feedbackId, user)
            )
        );
    }

    @PatchMapping("/{feedbackId}")
    public ResponseEntity<ApiResponse<FeedbackResponse>> updateFeedback(
        @PathVariable("feedbackId") long feedbackId,
        @Valid @RequestPart("request") FeedbackRequest request,
        @RequestPart(value = "imageFile", required = false) MultipartFile image,
        @CurrentUser CustomUser2Member user
    ) {
        return ResponseEntity.ok(
            ApiResponse.createSuccess(
                feedbackService.updateFeedback(feedbackId, request, image, user)
            )
        );
    }

    @DeleteMapping("/{feedbackId}")
    public ResponseEntity<ApiResponse<Object>> deleteFeedback(
        @PathVariable("feedbackId") long feedbackId,
        @CurrentUser CustomUser2Member user
    ) {
        feedbackService.deleteFeedback(feedbackId, user);
        return ResponseEntity.ok(ApiResponse.createSuccessWithNoData());
    }
}
