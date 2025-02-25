package org.programmers.signalbuddyfinal.domain.feedback_report.controller;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.programmers.signalbuddyfinal.domain.feedback_report.dto.FeedbackReportRequest;
import org.programmers.signalbuddyfinal.domain.feedback_report.dto.FeedbackReportResponse;
import org.programmers.signalbuddyfinal.domain.feedback_report.dto.FeedbackReportUpdateRequest;
import org.programmers.signalbuddyfinal.domain.feedback_report.entity.enums.FeedbackReportCategory;
import org.programmers.signalbuddyfinal.domain.feedback_report.entity.enums.FeedbackReportStatus;
import org.programmers.signalbuddyfinal.domain.feedback_report.service.FeedbackReportService;
import org.programmers.signalbuddyfinal.global.annotation.CurrentUser;
import org.programmers.signalbuddyfinal.global.dto.CustomUser2Member;
import org.programmers.signalbuddyfinal.global.dto.PageResponse;
import org.programmers.signalbuddyfinal.global.response.ApiResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/feedbacks")
@RequiredArgsConstructor
public class FeedbackReportController {

    private final FeedbackReportService reportService;

    @PostMapping("/{feedbackId}/reports")
    public ResponseEntity<ApiResponse<FeedbackReportResponse>> writeFeedbackReport(
        @PathVariable("feedbackId") long feedbackId,
        @Valid @RequestBody FeedbackReportRequest request,
        @CurrentUser CustomUser2Member user
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(
                ApiResponse.createSuccess(
                    reportService.writeFeedbackReport(feedbackId, request, user)
                )
            );
    }

    @GetMapping("/reports")
    public ResponseEntity<ApiResponse<PageResponse<FeedbackReportResponse>>> searchFeedbackReportList(
        @PageableDefault(sort = {"createdAt"}, direction = Direction.DESC)
        Pageable pageable,
        @RequestParam(value = "keyword", required = false)
        String keyword,
        @RequestParam(value = "category", required = false)
        Set<FeedbackReportCategory> categories,
        @RequestParam(value = "status", required = false)
        Set<FeedbackReportStatus> statuses,
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
                reportService.searchFeedbackReportList(
                    pageable, keyword,
                    categories, statuses,
                    startDate, endDate,
                    user
                )
            )
        );
    }

    @PatchMapping("/{feedbackId}/reports/{reportId}")
    public ResponseEntity<ApiResponse<Object>> updateFeedbackReports(
        @PathVariable("feedbackId") long feedbackId,
        @PathVariable("reportId") long reportId,
        @Valid @RequestBody FeedbackReportUpdateRequest request,
        @CurrentUser CustomUser2Member user
    ) {
        reportService.updateFeedbackReport(feedbackId, reportId, request, user);
        return ResponseEntity.ok(
            ApiResponse.createSuccessWithNoData()
        );
    }

    @DeleteMapping("/{feedbackId}/reports/{reportId}")
    public ResponseEntity<ApiResponse<Object>> deleteFeedbackReport(
        @PathVariable("feedbackId") long feedbackId,
        @PathVariable("reportId") long reportId,
        @CurrentUser CustomUser2Member user
    ) {
        reportService.deleteFeedbackReport(feedbackId, reportId, user);
        return ResponseEntity.ok(
            ApiResponse.createSuccessWithNoData()
        );
    }
}
