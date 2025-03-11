package org.programmers.signalbuddyfinal.domain.weather.controller;

import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.programmers.signalbuddyfinal.domain.weather.service.WeatherService;
import org.programmers.signalbuddyfinal.global.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
public class WeatherController {

    private final WeatherService weatherService;

    @PostMapping("/api/weather")
    public ResponseEntity<ApiResponse<Object>> saveExcel() throws IOException {
        weatherService.saveExcel();
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.createSuccessWithNoData());
    }

    @GetMapping(value = "/sse/weather", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> subscribeToWeather(@RequestParam double lat,
        @RequestParam double lng) {
        return ResponseEntity.ok(weatherService.subscribeToWeather(lat, lng));
    }

}
