package org.programmers.signalbuddyfinal.domain.comment.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.programmers.signalbuddyfinal.domain.comment.dto.CommentRequest;
import org.programmers.signalbuddyfinal.domain.comment.dto.CommentResponse;
import org.programmers.signalbuddyfinal.domain.comment.service.CommentService;
import org.programmers.signalbuddyfinal.global.annotation.CurrentUser;
import org.programmers.signalbuddyfinal.global.dto.CustomUser2Member;
import org.programmers.signalbuddyfinal.global.dto.PageResponse;
import org.programmers.signalbuddyfinal.global.response.ApiResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/feedbacks")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/{feedbackId}/comments")
    public ResponseEntity<ApiResponse<Object>> writeComment(
        @PathVariable("feedbackId") Long feedbackId,
        @Valid @RequestBody CommentRequest request,
        @CurrentUser CustomUser2Member user
    ) {
        commentService.writeComment(feedbackId, request, user);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.createSuccessWithNoData());
    }

    @GetMapping("/{feedbackId}/comments")
    public ResponseEntity<ApiResponse<PageResponse<CommentResponse>>> searchCommentList(
        @PathVariable("feedbackId") Long feedbackId,
        @PageableDefault(page = 0, size = 7) Pageable pageable
    ) {
        PageResponse<CommentResponse> response =
            commentService.searchCommentList(feedbackId , pageable);
        return ResponseEntity.ok(ApiResponse.createSuccess(response));
    }

    @PatchMapping("/{feedbackId}/comments/{commentId}")
    public ResponseEntity<ApiResponse<Object>> modifyComment(
        @PathVariable("feedbackId") Long feedbackId,
        @PathVariable("commentId") Long commentId,
        @Valid @RequestBody CommentRequest request,
        @CurrentUser CustomUser2Member user
    ) {
        commentService.updateComment(commentId, request, user);
        return ResponseEntity.ok().body(ApiResponse.createSuccessWithNoData());
    }

    @DeleteMapping("/{feedbackId}/comments/{commentId}")
    public ResponseEntity<ApiResponse<Object>> deleteComment(
        @PathVariable("feedbackId") Long feedbackId,
        @PathVariable("commentId") Long commentId,
        @CurrentUser CustomUser2Member user
    ) {
        commentService.deleteComment(commentId, user);
        return ResponseEntity.ok().body(ApiResponse.createSuccessWithNoData());
    }
}
