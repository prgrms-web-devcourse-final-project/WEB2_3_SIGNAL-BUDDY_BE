package org.programmers.signalbuddyfinal.domain.crossroad.controller;

import jakarta.validation.constraints.Min;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.programmers.signalbuddyfinal.domain.crossroad.dto.CrossroadApiResponse;
import org.programmers.signalbuddyfinal.domain.crossroad.dto.CrossroadStateResponse;
import org.programmers.signalbuddyfinal.domain.crossroad.repository.CrossroadRepository;
import org.programmers.signalbuddyfinal.domain.crossroad.service.CrossroadService;
import org.programmers.signalbuddyfinal.global.response.ApiResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/crossroads")
@RequiredArgsConstructor
public class CrossroadController {

    private final CrossroadService crossroadService;
    private final CrossroadRepository crossroadRepository;

    @PostMapping("/save")
    public ResponseEntity<ApiResponse<Object>> saveCrossroadDates(
        @Min(1) @RequestParam("page") int page,
        @Min(10) @RequestParam("size") int pageSize
    ) {
        crossroadService.saveCrossroadDates(page, pageSize);
        return ResponseEntity.ok(ApiResponse.createSuccessWithNoData());
    }


    @GetMapping("/marker") // 저장된 DB 데이터를 기반으로 map에 찍을 marker의 데이터를 point로 가져오기
    public ResponseEntity<List<CrossroadApiResponse>> pointToMarker(){
        List<CrossroadApiResponse> markers = crossroadService.getAllMarkers();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(markers);
    }


    @GetMapping("/{crossroadId}/state")
    public ResponseEntity<ApiResponse<CrossroadStateResponse>> checkSignalState(
        @PathVariable("crossroadId") long crossroadId
    ) {
        return ResponseEntity.ok(
            ApiResponse.createSuccess(
                crossroadService.checkSignalState(crossroadId)
            )
        );
    }

    @GetMapping("/around") // 좌표 값을 기반으로 50m이내 신호등 반환
    public ResponseEntity<List<CrossroadApiResponse>> aroundCrossroad(
            @RequestParam double lat,
            @RequestParam double lng
    ) {

        List<CrossroadApiResponse> aroundSign = crossroadRepository.findNearByCrossroads(lat, lng);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(aroundSign);

    }


}
