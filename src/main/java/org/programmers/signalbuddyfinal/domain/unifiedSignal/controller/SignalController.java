package org.programmers.signalbuddyfinal.domain.unifiedSignal.controller;

import java.util.ArrayList;
import java.util.List;
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

    @GetMapping("/search_around/crossroad")
    public ResponseEntity<ApiResponse<List<CrossroadResponse>>> saveCrossInfo(
        @RequestParam(value="lat") Double lat,
        @RequestParam(value="lng") Double lng,
        @RequestParam(value="radius") Integer radius
    ){
        List<CrossroadResponse> signal = new ArrayList<>();

        signal.addAll( crossroadService.searchAndSaveCrossroad(lat,lng,radius) );

        return ResponseEntity.ok(ApiResponse.createSuccess(signal));
    }

    @GetMapping("/search_around/traffic")
    public ResponseEntity<ApiResponse<List<TrafficResponse>>> saveTrafficInfo(
        @RequestParam(value="lat") Double lat,
        @RequestParam(value="lng") Double lng,
        @RequestParam(value="radius") Integer radius
    ){
        List<TrafficResponse> signal = new ArrayList<>();

        signal.addAll( trafficService.searchAndSaveTraffic(lat,lng,radius) );

        return ResponseEntity.ok(ApiResponse.createSuccess(signal));
    }

    @GetMapping("find-info/crossroad/{id}")
    public ResponseEntity<ApiResponse<Object>> findCrossInfo(
            @PathVariable Long id
    ){
        CrossroadResponse crossroad = crossroadService.crossroadFindById(id);

        return ResponseEntity.ok(ApiResponse.createSuccess(crossroad));
    }

    @GetMapping("find-info/traffic/{id}")
    public ResponseEntity<ApiResponse<Object>> findTrafficInfo(
            @PathVariable Long id
    ){
        TrafficResponse traffic = trafficService.trafficFindById(id);

        return ResponseEntity.ok(ApiResponse.createSuccess(traffic));
    }
}
