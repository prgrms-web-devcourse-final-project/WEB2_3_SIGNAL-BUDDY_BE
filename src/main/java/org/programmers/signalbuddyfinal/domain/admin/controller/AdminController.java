package org.programmers.signalbuddyfinal.domain.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.programmers.signalbuddyfinal.domain.admin.dto.AdminJoinRequest;
import org.programmers.signalbuddyfinal.domain.admin.dto.AdminMemberResponse;
import org.programmers.signalbuddyfinal.domain.admin.dto.WithdrawalMemberResponse;
import org.programmers.signalbuddyfinal.domain.admin.service.AdminService;
import org.programmers.signalbuddyfinal.domain.member.dto.MemberResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/admins")
@RequiredArgsConstructor
@Tag(name="Admin API")
public class AdminController {

    private final AdminService adminService;


    @Operation(summary = "사용자 전체 조회 API")
    @GetMapping("/members")
    public ResponseEntity<Page<AdminMemberResponse>> getAllMembers(
        @PageableDefault(page = 0, size = 10, sort = "email") Pageable pageable) {

        Page<AdminMemberResponse> members = adminService.getAllMembers(pageable);
        return ResponseEntity.ok(members);
    }

    @Operation(summary = "사용자 상세 조회 API")
    @GetMapping("members-detail/{id}")
    public ResponseEntity<AdminMemberResponse> getMember(@PathVariable Long id) {
        final AdminMemberResponse member = adminService.getMember(id);
        return ResponseEntity.ok(member);
    }

    @Operation(summary = "탈퇴 사용자 전체 조회 API")
    @GetMapping("/members-withdrawal")
    public ResponseEntity<Page<WithdrawalMemberResponse>> getAllWithdrawMembers(
        @PageableDefault(page = 0, size = 10, sort = "email") Pageable pageable) {

            Page<WithdrawalMemberResponse> members = adminService.getAllWithdrawalMembers(pageable);
            return ResponseEntity.ok(members);
    }

    @Operation(summary = "관리자 회원 가입 API")
    @PostMapping("/join")
    public ResponseEntity<MemberResponse> joinMember(
        @Validated @RequestBody AdminJoinRequest memberJoinRequest) {

        MemberResponse saved = adminService.joinAdminMember(memberJoinRequest);
        return ResponseEntity.ok(saved);
    }
}
