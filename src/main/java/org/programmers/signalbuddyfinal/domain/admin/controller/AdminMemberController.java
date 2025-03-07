package org.programmers.signalbuddyfinal.domain.admin.controller;

import lombok.RequiredArgsConstructor;
import org.programmers.signalbuddyfinal.domain.admin.dto.AdminMemberDetailResponse;
import org.programmers.signalbuddyfinal.domain.admin.dto.AdminMemberResponse;
import org.programmers.signalbuddyfinal.domain.admin.dto.MemberFilterRequest;
import org.programmers.signalbuddyfinal.domain.admin.dto.WithdrawalMemberResponse;
import org.programmers.signalbuddyfinal.domain.admin.service.AdminMemberService;
import org.programmers.signalbuddyfinal.global.dto.PageResponse;
import org.programmers.signalbuddyfinal.global.response.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/admin/members")
@RequiredArgsConstructor
public class AdminMemberController {

    private final AdminMemberService adminService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AdminMemberResponse>>> getAllMembers(
        @PageableDefault(page = 0, size = 10, sort = "email") Pageable pageable) {

        PageResponse<AdminMemberResponse> members = adminService.getAllMembers(pageable);
        return ResponseEntity.ok(ApiResponse.createSuccess(members));
    }

    @GetMapping("{id}")
    public ResponseEntity<AdminMemberDetailResponse> getMember(@PathVariable Long id) {
        final AdminMemberDetailResponse member = adminService.getMember(id);
        return ResponseEntity.ok(member);
    }

    @GetMapping("/withdrawal")
    public ResponseEntity<Page<WithdrawalMemberResponse>> getAllWithdrawMembers(
        @PageableDefault(page = 0, size = 10, sort = "email") Pageable pageable) {

        Page<WithdrawalMemberResponse> members = adminService.getAllWithdrawalMembers(pageable);
        return ResponseEntity.ok(members);
    }

    @GetMapping("/filter")
    public ResponseEntity<ApiResponse<PageResponse<AdminMemberResponse>>> getAllFilteredMembers(
        @PageableDefault(page = 0, size = 10, sort = "email") Pageable pageable,
        @ModelAttribute MemberFilterRequest memberFilterRequest) {

        PageResponse<AdminMemberResponse> members = adminService.getAllMemberWithFilter(pageable,
            memberFilterRequest);

        return ResponseEntity.ok(ApiResponse.createSuccess(members));
    }

}
