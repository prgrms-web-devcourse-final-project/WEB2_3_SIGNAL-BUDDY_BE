package org.programmers.signalbuddyfinal.domain.traffic.service;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.programmers.signalbuddyfinal.domain.trafficSignal.dto.TrafficResponse;
import org.programmers.signalbuddyfinal.domain.trafficSignal.repository.TrafficRedisRepository;
import org.programmers.signalbuddyfinal.domain.trafficSignal.service.TrafficService;
import org.programmers.signalbuddyfinal.global.support.ServiceTest;
import org.springframework.data.redis.core.RedisTemplate;

public class TrafficServiceTest extends ServiceTest {

    @InjectMocks
    private TrafficService trafficService;

    @Mock
    private RedisTemplate<Object,Object> redisTemplate;

    @Mock
    private TrafficRedisRepository trafficRedisRepository;

    private List<TrafficResponse> expected;

    @BeforeEach
    void setUp() {

        expected = List.of(
            TrafficResponse.builder()
                .trafficSignalId(1L)
                .signalType("1")
                .district("강남구")
                .address("강남구 대변로 29")
                .lat(37.4777135)
                .lng(126.9153603)
                .build()
        );
    }

    @Test
    void testSearchAndSaveCrossroadRedisExists() {

        Double lat = expected.get(0).getLat();
        Double lng = expected.get(0).getLng();

        when(trafficRedisRepository.findNearbyTraffics(lat, lng, 1.0)).thenReturn(expected);
        when(redisTemplate.hasKey("traffic:info")).thenReturn(true);

        // When
        List<TrafficResponse> result = trafficService.searchAndSaveTraffic(lat, lng, 1000);

        // Then
        assertThat(result).isEqualTo(expected);
    }
}
