package org.programmers.signalbuddyfinal.domain.air_quality.controller;

import lombok.RequiredArgsConstructor;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.programmers.signalbuddyfinal.domain.air_quality.service.AirQualityService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AirQualityScheduler {
    private final AirQualityService airQualityService;

    @Scheduled(cron = "${schedule.air-quality-api.cron:0 0/1 * * * ?}")
    @SchedulerLock(
        name = "updateAirQualityScheduler",
        lockAtMostFor = "${schedule.air-quality-api.lockAtMostFor:5m}",
        lockAtLeastFor = "${schedule.air-quality-api.lockAtLeastFor:50m}"
    )
    public void updateAirQuality(){
        airQualityService.updateAriQuality();
    }
}