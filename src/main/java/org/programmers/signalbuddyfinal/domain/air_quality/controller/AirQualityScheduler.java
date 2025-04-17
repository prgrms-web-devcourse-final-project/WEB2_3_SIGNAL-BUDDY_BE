package org.programmers.signalbuddyfinal.domain.air_quality.controller;

import lombok.RequiredArgsConstructor;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.programmers.signalbuddyfinal.domain.air_quality.service.AirQualityService;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@RequiredArgsConstructor
public class AirQualityScheduler {
    private final AirQualityService airQualityService;

    @Scheduled(cron = "${schedule.air-quality-api.cron}")
    @SchedulerLock(
        name = "updateAirQualityScheduler",
        lockAtMostFor = "${schedule.air-quality-api.lockAtMostFor}",
        lockAtLeastFor = "${schedule.air-quality-api.lockAtLeastFor}")
    public void updateAirQuality(){
        airQualityService.updateAriQuality();
    }
}
