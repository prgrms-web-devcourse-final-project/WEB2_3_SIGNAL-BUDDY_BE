package org.programmers.signalbuddyfinal.domain.admin.controller;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.programmers.signalbuddyfinal.domain.admin.service.AdminFeedbackService;
import org.programmers.signalbuddyfinal.domain.feedback.dto.FeedbackResponse;
import org.programmers.signalbuddyfinal.domain.feedback.dto.FeedbackSearchRequest;
import org.programmers.signalbuddyfinal.global.annotation.CurrentUser;
import org.programmers.signalbuddyfinal.global.dto.CustomUser2Member;
import org.programmers.signalbuddyfinal.global.dto.PageResponse;
import org.programmers.signalbuddyfinal.global.response.ApiResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/feedbacks")
@RequiredArgsConstructor
public class AdminFeedbackController {

    private final AdminFeedbackService adminFeedbackService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<FeedbackResponse>>> searchFeedbackList(
        @PageableDefault(sort = {"createdAt"}, direction = Direction.DESC)
        Pageable pageable,
        @ModelAttribute FeedbackSearchRequest request,
        @RequestParam(value = "deleted", required = false)
        Boolean deleted,
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
                adminFeedbackService.searchFeedbackList(
                    pageable, request, deleted, startDate, endDate, user
                )
            )
        );
    }
}
