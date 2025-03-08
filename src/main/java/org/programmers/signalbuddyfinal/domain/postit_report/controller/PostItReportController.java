package org.programmers.signalbuddyfinal.domain.postit_report.controller;

import lombok.RequiredArgsConstructor;
import org.programmers.signalbuddyfinal.domain.postit_report.service.PostItReportService;
import org.programmers.signalbuddyfinal.global.annotation.CurrentUser;
import org.programmers.signalbuddyfinal.global.dto.CustomUser2Member;
import org.programmers.signalbuddyfinal.global.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/postit-report")
@RequiredArgsConstructor
public class PostItReportController {

    private final PostItReportService postItReportService;

    @PostMapping("add/{postitId}")
    public ResponseEntity<ApiResponse> addReport(@PathVariable(value = "postitId") Long postitId,
        @CurrentUser CustomUser2Member user) {

        postItReportService.addReport(postitId, user);

        return ResponseEntity.ok(ApiResponse.createSuccessWithNoData());
    }

    @DeleteMapping("cancel/{postitId}")
    public ResponseEntity<ApiResponse> cancelReport(@PathVariable(value = "postitId") Long postitId,
        @CurrentUser CustomUser2Member user) {

        postItReportService.cancelReport(postitId, user);

        return ResponseEntity.ok(ApiResponse.createSuccessWithNoData());
    }
}
