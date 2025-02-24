package org.programmers.signalbuddyfinal.domain.feedback_report.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.programmers.signalbuddyfinal.domain.feedback_report.dto.FeedbackReportRequest;
import org.programmers.signalbuddyfinal.domain.feedback_report.dto.FeedbackReportResponse;
import org.programmers.signalbuddyfinal.domain.feedback_report.dto.FeedbackReportUpdateRequest;
import org.programmers.signalbuddyfinal.domain.feedback_report.service.FeedbackReportService;
import org.programmers.signalbuddyfinal.global.annotation.CurrentUser;
import org.programmers.signalbuddyfinal.global.dto.CustomUser2Member;
import org.programmers.signalbuddyfinal.global.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class FeedbackReportController {

    private final FeedbackReportService reportService;

    @PostMapping("/api/feedbacks/{feedbackId}/reports")
    public ResponseEntity<ApiResponse<FeedbackReportResponse>> writeFeedbackReport(
        @PathVariable("feedbackId") long feedbackId,
        @Valid @RequestBody FeedbackReportRequest request,
        @CurrentUser CustomUser2Member user
    ) {
        return ResponseEntity.ok(
            ApiResponse.createSuccess(
                reportService.writeFeedbackReport(feedbackId, request, user)
            )
        );
    }

    @GetMapping("/{feedbackId}/reports")
    public ResponseEntity<ApiResponse<Object>> searchFeedbackReportList(
        @PathVariable("feedbackId") long feedbackId
    ) {
        return ResponseEntity.ok(
            ApiResponse.createSuccessWithNoData()
        );
    }

    @PatchMapping("/api/feedbacks/{feedbackId}/reports/{reportId}")
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

    @DeleteMapping("/api/feedbacks/{feedbackId}/reports/{reportId}")
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
