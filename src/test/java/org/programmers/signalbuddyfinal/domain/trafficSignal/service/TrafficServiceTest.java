package org.programmers.signalbuddyfinal.domain.trafficSignal.service;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.programmers.signalbuddyfinal.domain.trafficSignal.dto.TrafficResponse;
import org.programmers.signalbuddyfinal.domain.trafficSignal.repository.CustomTrafficRepositoryImpl;
import org.programmers.signalbuddyfinal.domain.trafficSignal.repository.TrafficRedisRepository;
import org.programmers.signalbuddyfinal.global.support.ServiceTest;
import org.springframework.data.redis.core.RedisTemplate;

public class TrafficServiceTest extends ServiceTest {

    @InjectMocks
    private TrafficService trafficService;

    @Mock
    private RedisTemplate<Object,Object> redisTemplate;

    @Mock
    private TrafficRedisRepository trafficRedisRepository;

    @Mock
    private CustomTrafficRepositoryImpl customTrafficRepository;

    private List<TrafficResponse> expected;
    private static final String TRAFFIC_REDIS_KEY = "traffic:info";

    private Double lat;
    private Double lng;

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

        lat = expected.get(0).getLat();
        lng = expected.get(0).getLng();
    }

    @Test
    @DisplayName("주변 보행등 정보 redis 테스트")
    void testSearchAndSaveTrafficRedisExists() {

        // Given
        when(trafficRedisRepository.findNearbyTraffics(lat, lng, 1.0)).thenReturn(expected);
        when(redisTemplate.hasKey(TRAFFIC_REDIS_KEY)).thenReturn(true);

        // When
        List<TrafficResponse> result = trafficService.searchAndSaveTraffic(lat, lng, 1000);

        // Then
        assertThat(result).isEqualTo(expected);
    }

    @Test
    @DisplayName("주변 보행등 정보 캐싱 테스트")
    void testSearchAndSaveTrafficRedisNotExists() {

        // Given
        when(redisTemplate.hasKey(TRAFFIC_REDIS_KEY)).thenReturn(false);
        doNothing().when(trafficRedisRepository).save(any(TrafficResponse.class));

        when(customTrafficRepository.findNearestTraffics(lat,lng,1000)).thenReturn(expected);

        // When
        List<TrafficResponse> result = trafficService.searchAndSaveTraffic(lat, lng, 1000);

        // Then
        assertThat(result).isEqualTo(expected);
    }

    @Test
    @DisplayName("ID값 redis 테스트")
    void testTrafficFindByIdRedisExists() {

        // Given
        Long id = expected.get(0).getTrafficSignalId();

        when(trafficRedisRepository.findById(id)).thenReturn(expected.get(0));
        when(redisTemplate.hasKey(TRAFFIC_REDIS_KEY)).thenReturn(true);

        // When
        TrafficResponse result = trafficService.trafficFindById(id);

        // Then
        assertThat(result).isEqualTo(expected.get(0));

    }

    /**
     * TODO: TrafficService 코드 refactor 필요
     *   -> findById()
     *      Servive 레이어에서 entity에 접근하는 repository를 직접 사용하지 않게
     */
/*    @Test
    @DisplayName("보행등 ID 캐싱 테스트")
    void testTrafficFindByIdRedisNotExists() {

        // Given
        Long id = expected.get(0).getTrafficSignalId();

        when(redisTemplate.hasKey(TRAFFIC_REDIS_KEY)).thenReturn(false);
        when(trafficRepository.findById(id)).thenReturn(expected.get(0));

        // When
        TrafficResponse result = trafficService.trafficFindById(id);

        // Then
        assertThat(result).isNotNull();
    }*/
}
