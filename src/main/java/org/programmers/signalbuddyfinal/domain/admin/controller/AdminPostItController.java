package org.programmers.signalbuddyfinal.domain.admin.controller;

import lombok.RequiredArgsConstructor;
import org.programmers.signalbuddyfinal.domain.admin.dto.AdminMemberResponse;
import org.programmers.signalbuddyfinal.domain.admin.dto.AdminPostItResponse;
import org.programmers.signalbuddyfinal.domain.admin.service.AdminPostItService;
import org.programmers.signalbuddyfinal.domain.postit.service.PostItService;
import org.programmers.signalbuddyfinal.global.dto.PageResponse;
import org.programmers.signalbuddyfinal.global.response.ApiResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
}
