package org.programmers.signalbuddyfinal.domain.trafficSignal.controller;

import lombok.RequiredArgsConstructor;
import org.programmers.signalbuddyfinal.domain.trafficSignal.service.TrafficCsvService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Validated
@RestController
@RequestMapping("/api/traffic")
@RequiredArgsConstructor
public class TrafficController {

    @Autowired
    private TrafficCsvService trafficCsvService;

    @PostMapping("/save")
    public ResponseEntity<Void> saveTrafficData(@RequestParam MultipartFile file) throws IOException {
        trafficCsvService.saveCsvData(file);
        return ResponseEntity.ok().build();
    }

}
