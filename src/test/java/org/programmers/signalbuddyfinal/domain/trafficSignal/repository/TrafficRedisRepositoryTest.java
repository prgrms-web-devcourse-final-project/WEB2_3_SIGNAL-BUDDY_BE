package org.programmers.signalbuddyfinal.domain.trafficSignal.repository;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.programmers.signalbuddyfinal.domain.trafficSignal.dto.TrafficResponse;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.GeoOperations;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;

@ExtendWith(MockitoExtension.class)
public class TrafficRedisRepositoryTest {

    @Mock
    private RedisTemplate<Object, Object> redisTemplate;

    @Mock
    private HashOperations<Object, Object, Object> hashOperations;

    @Mock
    private GeoOperations<Object, Object> geoOperations;

    private static final String KEY_HASH = "traffic:info";
    private static final String KEY_GEO = "traffic:geo";
    private static final Duration TTL = Duration.ofMinutes(5);

    private TrafficRedisRepository trafficRedisRepository;
    private List<TrafficResponse> expected;

    private Point point;
    private Long id;
    private double lat;
    private double lng;

    @BeforeEach
    void setUp() {

        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(redisTemplate.opsForGeo()).thenReturn(geoOperations);

        trafficRedisRepository = new TrafficRedisRepository(redisTemplate);

        expected = List.of(
            TrafficResponse.builder()
                .trafficSignalId(1L)
                .serialNumber(1L)
                .signalType("1")
                .district("강남구")
                .address("강남구 대변로 29")
                .lat(37.4777135)
                .lng(126.9153603)
                .build()
        );

        id = expected.get(0).getTrafficSignalId();
        lat = expected.get(0).getLat();
        lng = expected.get(0).getLng();

        point = new Point(lng, lat);
    }

    @Test
    @DisplayName("보행등 저장 테스트")
    void trafficSaveTest(){

        // When
        trafficRedisRepository.save(expected.get(0));

        // Then
        verify(geoOperations).add(
            eq(KEY_GEO),
            eq(point),
            eq( String.valueOf(id) )
        );

        verify(hashOperations).put(eq(KEY_HASH), eq(id.toString()), eq(expected.get(0)));
        verify(redisTemplate).expire(KEY_GEO, TTL);
        verify(redisTemplate).expire(KEY_HASH, TTL);
    }

    @Test
    @DisplayName("주변 보행등 redis 데이터 반환")
    void trafficNearByTestReturnTrafficList(){

        //Given
        double radius = 1;

        // result set
        List<GeoResult<RedisGeoCommands.GeoLocation<Object>>> geoResults = new ArrayList<>();
        RedisGeoCommands.GeoLocation<Object> geoLocation = new RedisGeoCommands.GeoLocation<>(id.toString(),point);
        GeoResult<RedisGeoCommands.GeoLocation<Object>> geoResult
            =  new GeoResult<>(geoLocation, new Distance(1, Metrics.KILOMETERS));

        geoResults.add(geoResult);

        GeoResults<RedisGeoCommands.GeoLocation<Object>> mockGeoResults = mock(GeoResults.class);
        when(mockGeoResults.getContent()).thenReturn(geoResults);

        when(geoOperations.radius(
            eq(KEY_GEO),
            any(Circle.class)
        )).thenReturn(mockGeoResults);

        when(geoOperations.position(KEY_GEO, id.toString())).thenReturn(List.of(point));



        doReturn(expected.get(0)).when(hashOperations).get(eq(KEY_HASH), eq(id.toString()));

        //When
        trafficRedisRepository.save(expected.get(0));
        List<TrafficResponse> result = trafficRedisRepository.findNearbyTraffics(lat, lng, radius);

        //Then
        Assertions.assertNotNull(result);
        Assertions.assertEquals(expected.size(), result.size());
        Assertions.assertEquals(id, result.get(0).getTrafficSignalId());

        verify(geoOperations).radius(
            eq(KEY_GEO),
            argThat(circle -> {
                return circle.getCenter().getX() == lng &&
                       circle.getCenter().getY() == lat &&
                       circle.getRadius().getValue() == radius;
            })
        );

    }
}
