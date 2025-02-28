package org.programmers.signalbuddyfinal.domain.admin.controller;

import lombok.RequiredArgsConstructor;
import org.programmers.signalbuddyfinal.domain.admin.dto.AdminPostItResponse;
import org.programmers.signalbuddyfinal.domain.admin.dto.PostItFilterRequest;
import org.programmers.signalbuddyfinal.domain.admin.service.AdminPostItService;
import org.programmers.signalbuddyfinal.domain.postit.dto.PostItResponse;
import org.programmers.signalbuddyfinal.global.dto.PageResponse;
import org.programmers.signalbuddyfinal.global.response.ApiResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/postits")
@RequiredArgsConstructor
public class AdminPostItController {

    private final AdminPostItService adminPostItService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AdminPostItResponse>>> getAllPostIt(
        @PageableDefault(page = 0, size = 10) Pageable pageable) {

        PageResponse<AdminPostItResponse> postIt = adminPostItService.getAllPostIt(pageable);
        return ResponseEntity.ok(ApiResponse.createSuccess(postIt));
    }

    @PatchMapping("/{postitId}")
    public ResponseEntity<ApiResponse<PostItResponse>> completePostIt(
        @PathVariable(value = "postitId") Long postitId
    ) {
        return ResponseEntity.ok(
            ApiResponse.createSuccess(
               adminPostItService.completePostIt(postitId)));
    }

    @GetMapping("/filter")
    public ResponseEntity<ApiResponse<PageResponse<AdminPostItResponse>>> getAllFilteredPostIt(
        @PageableDefault(page = 0, size = 10) Pageable pageable,
        @ModelAttribute PostItFilterRequest postItFilterRequest) {

        PageResponse<AdminPostItResponse> postIts = adminPostItService.getAllPostItWithFilter(pageable,
            postItFilterRequest);

        return ResponseEntity.ok(ApiResponse.createSuccess(postIts));
    }
}
