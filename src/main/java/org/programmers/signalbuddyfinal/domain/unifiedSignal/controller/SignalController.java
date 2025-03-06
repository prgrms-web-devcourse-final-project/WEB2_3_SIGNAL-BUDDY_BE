package org.programmers.signalbuddyfinal.domain.unifiedSignal.controller;

import lombok.RequiredArgsConstructor;
import org.programmers.signalbuddyfinal.domain.crossroad.dto.CrossroadResponse;
import org.programmers.signalbuddyfinal.domain.crossroad.service.CrossroadService;
import org.programmers.signalbuddyfinal.domain.trafficSignal.dto.TrafficResponse;
import org.programmers.signalbuddyfinal.domain.trafficSignal.service.TrafficService;
import org.programmers.signalbuddyfinal.global.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/unifiedSignal")
@RequiredArgsConstructor
public class SignalController {

    private final CrossroadService crossroadService;
    private final TrafficService trafficService;

    @PostMapping("/save")
    public ResponseEntity<ApiResponse<Object>> saveSignalInfo(
        @RequestParam(value="lat") Double lat,
        @RequestParam(value="lng") Double lng
    ){
        crossroadService.saveAroundCrossroad(lat,lng);
        trafficService.saveAroundTraffic(lat,lng);

        return ResponseEntity.ok(ApiResponse.createSuccessWithNoData());
    }

    @GetMapping("find-info/crossroad/{id}")
    public ResponseEntity<ApiResponse<Object>> findCrossInfo(
            @PathVariable String id
    ){
        CrossroadResponse crossroad = crossroadService.crossraodFindById(id);

        return ResponseEntity.ok(ApiResponse.createSuccess(crossroad));

    }

    @GetMapping("find-info/traffic/{id}")
    public ResponseEntity<ApiResponse<Object>> findTrafficInfo(
            @PathVariable String id
    ){
        TrafficResponse traffic = trafficService.trafficFindById(id);

        return ResponseEntity.ok(ApiResponse.createSuccess(traffic));
    }
}
