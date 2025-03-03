package org.programmers.signalbuddyfinal.domain.trafficSignal.controller;

import lombok.RequiredArgsConstructor;
import org.programmers.signalbuddyfinal.domain.trafficSignal.service.TrafficCsvService;
import org.programmers.signalbuddyfinal.global.annotation.CurrentUser;
import org.programmers.signalbuddyfinal.global.dto.CustomUser2Member;
import org.programmers.signalbuddyfinal.global.response.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

@Validated
@RestController
@RequestMapping("/api/traffic")
@RequiredArgsConstructor
public class TrafficController {

    @Autowired
    private TrafficCsvService trafficCsvService;

    @PostMapping("/save")
    public ResponseEntity<ApiResponse<Object>> saveTrafficData(
            @RequestParam String fileName
    ) throws IOException {

        trafficCsvService.saveCsvData(fileName);

        return ResponseEntity.ok(ApiResponse.createSuccess("파일이 성공적으로 저장되었습니다."));
    }


}
