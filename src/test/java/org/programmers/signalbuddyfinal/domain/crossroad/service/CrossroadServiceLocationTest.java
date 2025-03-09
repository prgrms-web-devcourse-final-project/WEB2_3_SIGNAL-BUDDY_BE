package org.programmers.signalbuddyfinal.domain.crossroad.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.programmers.signalbuddyfinal.domain.crossroad.dto.CrossroadResponse;
import org.programmers.signalbuddyfinal.domain.crossroad.repository.CrossroadRedisRepository;
import org.programmers.signalbuddyfinal.global.support.ServiceTest;
import org.springframework.data.redis.core.RedisTemplate;

public class CrossroadServiceLocationTest extends ServiceTest {

    @InjectMocks
    private CrossroadService crossroadService;

    @Mock
    private RedisTemplate<Object,Object> redisTemplate;

    @Mock
    private CrossroadRedisRepository crossroadRedisRepository;

    private List<CrossroadResponse> expected;

    @BeforeEach
    void setUp() {
        expected = List.of(
            CrossroadResponse.builder()
                .crossroadId(1L)
                .crossroadApiId("10")
                .name("강남 사거리")
                .status("true")
                .lat(37.4777135)
                .lng(126.9153603)
                .build()
        );
    }

    @Test
    void testSearchAndSaveCrossroadRedisExists() {

        Double lat = expected.get(0).getLat();
        Double lng = expected.get(0).getLng();

        when(crossroadRedisRepository.findNearbyCrossroads(lat, lng, 1.0)).thenReturn(expected);
        when(redisTemplate.hasKey("crossroad:info")).thenReturn(true);

        // When
        List<CrossroadResponse> result = crossroadService.searchAndSaveCrossroad(lat, lng, 1000);

        // Then
        assertThat(result).isEqualTo(expected);
    }
}
