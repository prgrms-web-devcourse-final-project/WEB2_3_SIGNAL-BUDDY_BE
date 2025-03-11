package org.programmers.signalbuddyfinal.domain.term.controller;

import lombok.RequiredArgsConstructor;
import org.programmers.signalbuddyfinal.domain.term.dto.TermResponse;
import org.programmers.signalbuddyfinal.domain.term.entity.enums.TermCategory;
import org.programmers.signalbuddyfinal.domain.term.service.TermService;
import org.programmers.signalbuddyfinal.global.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/terms")
public class TermController {

    private final TermService termService;

    @GetMapping
    public ResponseEntity<ApiResponse<TermResponse>> readTermForMember(@RequestParam TermCategory category){

        return termService.getTerm(category);
    }
}
