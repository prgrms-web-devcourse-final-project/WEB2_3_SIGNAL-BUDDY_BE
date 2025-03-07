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
        @RequestParam(value="lng") Double lng,
        @RequestParam(value="radius") Integer radius
    ){
        crossroadService.searchAndSaveCrossroad(lat,lng,radius);
        trafficService.searchAndSaveTraffic(lat,lng,radius);

        return ResponseEntity.ok(ApiResponse.createSuccessWithNoData());
    }

    @GetMapping("find-info/crossroad/{apiId}")
    public ResponseEntity<ApiResponse<Object>> findCrossInfo(
            @PathVariable Long apiId
    ){
        CrossroadResponse crossroad = crossroadService.crossroadFindById(apiId);

        return ResponseEntity.ok(ApiResponse.createSuccess(crossroad));
    }

    @GetMapping("find-info/traffic/{serialNumber}")
    public ResponseEntity<ApiResponse<Object>> findTrafficInfo(
            @PathVariable Long serialNumber
    ){
        TrafficResponse traffic = trafficService.trafficFindById(serialNumber);

        return ResponseEntity.ok(ApiResponse.createSuccess(traffic));
    }
}
