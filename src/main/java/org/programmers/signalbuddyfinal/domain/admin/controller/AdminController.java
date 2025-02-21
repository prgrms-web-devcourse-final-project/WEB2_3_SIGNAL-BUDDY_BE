package org.programmers.signalbuddyfinal.domain.admin.controller;

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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/admins")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/members")
    public ResponseEntity<Page<AdminMemberResponse>> getAllMembers(
        @PageableDefault(page = 0, size = 10, sort = "email") Pageable pageable) {

        Page<AdminMemberResponse> members = adminService.getAllMembers(pageable);
        return ResponseEntity.ok(members);
    }

    @GetMapping("members-detail/{id}")
    public ResponseEntity<AdminMemberResponse> getMember(@PathVariable Long id) {
        final AdminMemberResponse member = adminService.getMember(id);
        return ResponseEntity.ok(member);
    }

    @GetMapping("/members-withdrawal")
    public ResponseEntity<Page<WithdrawalMemberResponse>> getAllWithdrawMembers(
        @PageableDefault(page = 0, size = 10, sort = "email") Pageable pageable) {

            Page<WithdrawalMemberResponse> members = adminService.getAllWithdrawalMembers(pageable);
            return ResponseEntity.ok(members);
    }

    @PostMapping("/join")
    public ResponseEntity<MemberResponse> joinMember(
        @Validated @RequestBody AdminJoinRequest memberJoinRequest) {

        MemberResponse saved = adminService.joinAdminMember(memberJoinRequest);
        return ResponseEntity.ok(saved);
    }
}
