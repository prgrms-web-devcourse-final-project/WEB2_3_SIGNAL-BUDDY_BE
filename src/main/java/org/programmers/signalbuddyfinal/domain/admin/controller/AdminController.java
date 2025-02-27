package org.programmers.signalbuddyfinal.domain.admin.controller;

import lombok.RequiredArgsConstructor;
import org.programmers.signalbuddyfinal.domain.admin.dto.AdminJoinRequest;
import org.programmers.signalbuddyfinal.domain.admin.service.AdminService;
import org.programmers.signalbuddyfinal.domain.member.dto.MemberResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/admins")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @PostMapping("/join")
    public ResponseEntity<MemberResponse> joinMember(
        @Validated @RequestBody AdminJoinRequest memberJoinRequest) {

        MemberResponse saved = adminService.joinAdminMember(memberJoinRequest);
        return ResponseEntity.ok(saved);
    }
}
