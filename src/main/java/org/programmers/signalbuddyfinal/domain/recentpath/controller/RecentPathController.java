package org.programmers.signalbuddyfinal.domain.recentpath.controller;

import lombok.RequiredArgsConstructor;
import org.programmers.signalbuddyfinal.domain.recentpath.dto.RecentPathResponse;
import org.programmers.signalbuddyfinal.domain.recentpath.service.RecentPathService;
import org.programmers.signalbuddyfinal.global.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recent-path")
@RequiredArgsConstructor
public class RecentPathController {

    private final RecentPathService recentPathService;

    @PatchMapping("{id}")
    public ResponseEntity<ApiResponse<RecentPathResponse>> updateRecentPathTime(
        @PathVariable Long id) {
        final RecentPathResponse response = recentPathService.updateRecentPathTime(id);
        return ResponseEntity.ok(ApiResponse.createSuccess(response));
    }

    @DeleteMapping("{id}/bookmarks")
    public ResponseEntity<ApiResponse<Object>> unlinkBookmark(@PathVariable Long id) {
        recentPathService.unlinkBookmark(id);
        return ResponseEntity.ok(ApiResponse.createSuccessWithNoData());
    }
}
