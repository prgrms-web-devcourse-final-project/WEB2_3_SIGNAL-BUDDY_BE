package org.programmers.signalbuddyfinal.domain.admin.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.programmers.signalbuddyfinal.domain.admin.dto.AdminJoinRequest;
import org.programmers.signalbuddyfinal.domain.admin.service.AdminService;
import org.programmers.signalbuddyfinal.domain.member.dto.MemberJoinRequest;
import org.programmers.signalbuddyfinal.domain.member.dto.MemberResponse;
import org.programmers.signalbuddyfinal.global.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("api/admins")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @PostMapping("/join")
    public ResponseEntity<ApiResponse<MemberResponse>> joinMember(
        @Valid @RequestPart("adminJoinRequest") AdminJoinRequest adminJoinRequest,
        @RequestPart(value = "profileImageUrl", required = false) MultipartFile profileImage) {
        MemberResponse memberResponse = adminService.joinAdminMember(adminJoinRequest, profileImage);
        return ResponseEntity.ok(ApiResponse.createSuccess(memberResponse));
    }
}
