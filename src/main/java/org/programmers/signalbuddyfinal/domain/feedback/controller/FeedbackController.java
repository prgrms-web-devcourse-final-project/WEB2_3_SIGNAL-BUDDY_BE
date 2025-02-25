package org.programmers.signalbuddyfinal.domain.feedback.controller;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.programmers.signalbuddyfinal.domain.feedback.dto.FeedbackRequest;
import org.programmers.signalbuddyfinal.domain.feedback.dto.FeedbackResponse;
import org.programmers.signalbuddyfinal.domain.feedback.entity.enums.AnswerStatus;
import org.programmers.signalbuddyfinal.domain.feedback.entity.enums.FeedbackCategory;
import org.programmers.signalbuddyfinal.domain.feedback.service.FeedbackService;
import org.programmers.signalbuddyfinal.global.annotation.CurrentUser;
import org.programmers.signalbuddyfinal.global.dto.CustomUser2Member;
import org.programmers.signalbuddyfinal.global.dto.PageResponse;
import org.programmers.signalbuddyfinal.global.response.ApiResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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
        @RequestParam(value = "status", required = false) AnswerStatus answerStatus,
        @RequestParam(value = "category", required = false) Set<FeedbackCategory> categories,
        @RequestParam(value = "crossroadId", required = false) Long crossroadId,
        @RequestParam(value = "keyword", required = false) String keyword
    ) {
        return ResponseEntity.ok(
            ApiResponse.createSuccess(
                feedbackService.searchFeedbackList(
                    pageable, answerStatus, categories, crossroadId, keyword
                )
            )
        );
    }

    @GetMapping(headers = HttpHeaders.AUTHORIZATION)
    public ResponseEntity<ApiResponse<PageResponse<FeedbackResponse>>> searchFeedbackListByAdmin(
        @PageableDefault(sort = {"createdAt"}, direction = Direction.DESC)
        Pageable pageable,
        @RequestParam(value = "keyword", required = false)
        String keyword,
        @RequestParam(value = "deleted", required = false)
        Boolean deleted,
        @RequestParam(value = "status", required = false)
        AnswerStatus answerStatus,
        @RequestParam(value = "category", required = false)
        Set<FeedbackCategory> categories,
        @RequestParam(name = "start-date", required = false)
        @DateTimeFormat(pattern = "yyyy-MM-dd")
        LocalDate startDate,
        @RequestParam(name = "end-date", required = false)
        @DateTimeFormat(pattern = "yyyy-MM-dd")
        LocalDate endDate,
        @CurrentUser CustomUser2Member user
    ) {
        return ResponseEntity.ok(
            ApiResponse.createSuccess(
                feedbackService.searchFeedbackListByAdmin(
                    pageable, keyword, deleted, answerStatus, categories, startDate, endDate, user
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
