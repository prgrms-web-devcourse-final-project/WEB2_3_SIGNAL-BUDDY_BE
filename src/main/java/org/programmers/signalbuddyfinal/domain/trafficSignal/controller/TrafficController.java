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
import java.util.Map;

@Validated
@RestController
@RequestMapping("/api/traffic")
@RequiredArgsConstructor
public class TrafficController {

    @Autowired
    private TrafficCsvService trafficCsvService;

    @PostMapping("/save")
    public ResponseEntity<ApiResponse<Object>> saveTrafficData(
            @RequestBody Map<String,String> request
    ) throws IOException {
        String filePath = request.get("filePath");
        File file = new File(filePath);

        if(!file.exists()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.createError("파일을 찾을 수 없습니다."));
        }

        trafficCsvService.saveCsvData(file);

        return ResponseEntity.ok(ApiResponse.createSuccess("파일이 성공적으로 저장되었습니다."));
    }

}
