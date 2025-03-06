package org.programmers.signalbuddyfinal.domain.unifiedSignal.controller;

import lombok.RequiredArgsConstructor;
import org.programmers.signalbuddyfinal.domain.crossroad.service.CrossroadService;
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
        @RequestParam(value="lat") double lat,
        @RequestParam(value="lng") double lng
    ){
        crossroadService.saveAroundCrossroad(lat,lng);
        trafficService.saveAroundTraffic(lat,lng);

        return ResponseEntity.ok(ApiResponse.createSuccessWithNoData());
    }

    @GetMapping("find-info/{id}")
    public ResponseEntity<ApiResponse<Object>> findSignalInfo(
            @PathVariable String id
    ){
        Object signal = new Object();

        if(id.startsWith("Crossroad")){
            signal = crossroadService.crossraodFindById(id);
        } else if (id.startsWith("Traffic")){
            signal = trafficService.trafficFindById(id);
        }

        return ResponseEntity.ok(ApiResponse.createSuccess(signal));

    }

}
