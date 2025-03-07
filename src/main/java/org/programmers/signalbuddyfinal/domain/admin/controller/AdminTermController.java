package org.programmers.signalbuddyfinal.domain.admin.controller;

import lombok.RequiredArgsConstructor;
import org.programmers.signalbuddyfinal.domain.admin.dto.CreateTermRequest;
import org.programmers.signalbuddyfinal.domain.admin.service.AdminTermService;
import org.programmers.signalbuddyfinal.global.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/admin/terms")
public class AdminTermController {

    private final AdminTermService adminTermService;

    @PostMapping
    public ResponseEntity<ApiResponse<Object>> save(@RequestBody CreateTermRequest createTermRequest){

        return adminTermService.registerTerm(createTermRequest);
    }
}
