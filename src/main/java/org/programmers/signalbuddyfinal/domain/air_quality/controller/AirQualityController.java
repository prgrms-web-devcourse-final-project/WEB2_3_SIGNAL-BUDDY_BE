package org.programmers.signalbuddyfinal.domain.air_quality.controller;

import lombok.RequiredArgsConstructor;
import org.programmers.signalbuddyfinal.domain.air_quality.dto.AirQualityResponse;
import org.programmers.signalbuddyfinal.domain.air_quality.service.AirQualityService;
import org.programmers.signalbuddyfinal.global.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/air-quality")
@RequiredArgsConstructor
public class AirQualityController {

  private final AirQualityService airQualityService;

  @GetMapping
  public ResponseEntity<ApiResponse<AirQualityResponse>> getAirQuality(
      @RequestParam double lat,
      @RequestParam double lng
  ) {
    return ResponseEntity.ok(ApiResponse.createSuccess(airQualityService.getAirQuality(lat, lng)));
  }
}
