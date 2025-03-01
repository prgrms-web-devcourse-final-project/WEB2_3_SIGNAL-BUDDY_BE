package org.programmers.signalbuddyfinal.domain.postit_report.controller;

import lombok.RequiredArgsConstructor;
import org.programmers.signalbuddyfinal.domain.postit_report.service.PostItReportService;
import org.programmers.signalbuddyfinal.global.annotation.CurrentUser;
import org.programmers.signalbuddyfinal.global.dto.CustomUser2Member;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/postit")
@RequiredArgsConstructor
public class PostItReportController {

    private final PostItReportService postItReportService;

    // 신고
    @PutMapping("/{postitId}")
    public void addReport(@PathVariable(value = "postitId") Long postitId,
        @CurrentUser CustomUser2Member user
    ) {


    }

    // 신고 취소
}
