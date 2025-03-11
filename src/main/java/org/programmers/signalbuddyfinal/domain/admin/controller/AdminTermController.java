package org.programmers.signalbuddyfinal.domain.admin.controller;

import lombok.RequiredArgsConstructor;
import org.programmers.signalbuddyfinal.domain.admin.dto.AdminTermResponse;
import org.programmers.signalbuddyfinal.domain.admin.dto.CreateTermRequest;
import org.programmers.signalbuddyfinal.domain.admin.service.AdminTermService;
import org.programmers.signalbuddyfinal.global.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/admin/terms")
public class AdminTermController {

    private final AdminTermService adminTermService;

    // 약관 작성
    @PostMapping
    public ResponseEntity<ApiResponse<Object>> save(
        @RequestBody CreateTermRequest createTermRequest) {

        return adminTermService.registerTerm(createTermRequest);
    }

    // 약관 상세 조회
    @GetMapping("/{termId}")
    public ResponseEntity<ApiResponse<AdminTermResponse>> getDetailTerm(@PathVariable Long termId,
        @RequestParam Long termVersionId) {
        return adminTermService.getDetailTerm(termId, termVersionId);
    }

    // 약관 수정

    // 약관 삭제
}
