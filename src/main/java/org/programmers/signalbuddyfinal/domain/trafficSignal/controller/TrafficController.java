package org.programmers.signalbuddyfinal.domain.trafficSignal.controller;

import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.programmers.signalbuddyfinal.domain.trafficSignal.service.TrafficCsvService;
import org.programmers.signalbuddyfinal.global.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/traffic")
@RequiredArgsConstructor
public class TrafficController {

    private final TrafficCsvService trafficCsvService;

    @PostMapping("/save")
    public ResponseEntity<ApiResponse<Object>> saveTrafficData(
            @RequestParam String fileName
    ) throws IOException {

        trafficCsvService.saveCsvData(fileName);

        return ResponseEntity.ok(ApiResponse.createSuccess("파일이 성공적으로 저장되었습니다."));
    }
}
