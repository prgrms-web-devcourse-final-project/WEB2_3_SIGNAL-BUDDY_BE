package org.programmers.signalbuddyfinal.domain.feedback_summary.controller;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.programmers.signalbuddyfinal.domain.feedback_summary.dto.FeedbackSummaryResponse;
import org.programmers.signalbuddyfinal.domain.feedback_summary.service.FeedbackSummaryService;
import org.programmers.signalbuddyfinal.global.response.ApiResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/feedback-summary")
@RequiredArgsConstructor
public class FeedbackSummaryController {

    private final FeedbackSummaryService feedbackSummaryService;

    @GetMapping
    public ResponseEntity<ApiResponse<FeedbackSummaryResponse>> getFeedbackSummary(
        @RequestParam(name = "date")
        @DateTimeFormat(pattern = "yyyy-MM-dd")
        LocalDate date
    ) {
        return ResponseEntity.ok(
            ApiResponse.createSuccess(feedbackSummaryService.getFeedbackSummary(date))
        );
    }
}
