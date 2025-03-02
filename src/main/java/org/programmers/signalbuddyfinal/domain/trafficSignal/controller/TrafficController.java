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

        if (!isValidFileName(fileName)) {
            throw new SecurityException("경로 탐색 시도 감지됨");
        }

        File file = new File("src/main/resources/static/file/"+fileName);

        if(!file.exists()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.createError("파일을 찾을 수 없습니다."));
        }

        trafficCsvService.saveCsvData(file);

        return ResponseEntity.ok(ApiResponse.createSuccess("파일이 성공적으로 저장되었습니다."));
    }

    // 파일 이름 검증 (특수 문자 및 경로 탐색 방지)
    private boolean isValidFileName(String fileName) {
        String regex = "^[a-zA-Z0-9._-]+$";
        Pattern pattern = Pattern.compile(regex);
        return pattern.matcher(fileName).matches();
    }

}
