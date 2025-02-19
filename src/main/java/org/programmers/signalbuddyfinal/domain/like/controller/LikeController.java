package org.programmers.signalbuddyfinal.domain.like.controller;

import lombok.RequiredArgsConstructor;
import org.programmers.signalbuddyfinal.domain.like.dto.LikeExistResponse;
import org.programmers.signalbuddyfinal.global.annotation.CurrentUser;
import org.programmers.signalbuddyfinal.global.dto.CustomUser2Member;
import org.programmers.signalbuddyfinal.global.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/feedbacks")
@RequiredArgsConstructor
public class LikeController {

    private final org.programmers.signalbuddyfinal.domain.like.service.LikeService likeService;

    @PostMapping("/{feedbackId}/like")
    public ResponseEntity<ApiResponse<Object>> addLike(
        @PathVariable("feedbackId") Long feedbackId,
        @CurrentUser CustomUser2Member user
    ) {
        likeService.addLike(feedbackId, user);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.createSuccessWithNoData());
    }

    @GetMapping("/{feedbackId}/like/exist")
    public ResponseEntity<ApiResponse<LikeExistResponse>> existsLike(
        @PathVariable("feedbackId") Long feedbackId,
        @CurrentUser CustomUser2Member user
    ) {
        LikeExistResponse response = likeService.existsLike(feedbackId, user);
        return ResponseEntity.ok(ApiResponse.createSuccess(response));
    }

    @DeleteMapping("/{feedbackId}/like")
    public ResponseEntity<ApiResponse<Object>> deleteLike(
        @PathVariable("feedbackId") Long feedbackId,
        @CurrentUser CustomUser2Member user
    ) {
        likeService.deleteLike(feedbackId, user);
        return ResponseEntity.ok().body(ApiResponse.createSuccessWithNoData());
    }
}
