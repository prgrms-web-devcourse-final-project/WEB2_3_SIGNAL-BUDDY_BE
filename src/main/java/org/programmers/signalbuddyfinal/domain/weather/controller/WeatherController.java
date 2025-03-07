package org.programmers.signalbuddyfinal.domain.weather.controller;

import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.programmers.signalbuddyfinal.domain.weather.dto.Weather;
import org.programmers.signalbuddyfinal.domain.weather.dto.WeatherResponse;
import org.programmers.signalbuddyfinal.domain.weather.service.WeatherService;
import org.programmers.signalbuddyfinal.global.response.ApiResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
public class WeatherController {

    private final WeatherService weatherService;

    @PostMapping("/api/weather")
    public ResponseEntity<ApiResponse<String>> saveExcel(@RequestBody String filePath) throws IOException {
        System.out.println("filePath: " + filePath);
        weatherService.saveExcel(filePath);
        return ResponseEntity.ok(ApiResponse.createSuccess("success"));
    }

    @GetMapping(value = "/sse/weather", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> subscribeToWeather(@RequestParam double lat, @RequestParam double lng) {
        return ResponseEntity.ok(weatherService.subscribeToWeather(lat, lng));
    }

}
