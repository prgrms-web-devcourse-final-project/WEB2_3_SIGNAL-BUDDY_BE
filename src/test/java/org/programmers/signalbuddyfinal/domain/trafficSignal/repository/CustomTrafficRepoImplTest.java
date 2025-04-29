package org.programmers.signalbuddyfinal.domain.trafficSignal.repository;

import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.programmers.signalbuddyfinal.domain.trafficSignal.dto.TrafficResponse;
import org.programmers.signalbuddyfinal.global.support.RepositoryTest;

@ExtendWith(MockitoExtension.class)
public class CustomTrafficRepoImplTest extends RepositoryTest {

    @Mock
    private CustomTrafficRepository customTrafficRepository;

    private List<TrafficResponse> expected;

    @BeforeEach
    void setUp() {

        expected = List.of(
            TrafficResponse.builder()
                .district("강남구")
                .signalType("보행등")
                .lat(37.5000)
                .lng(127.0300)
                .address("강남대로 123")
                .build(),

            TrafficResponse.builder()
                .district("서초구")
                .signalType("보행등")
                .lat(37.4950)
                .lng(127.0200)
                .address("서초대로 456")
                .build()
        );

    }

    @Test
    @DisplayName("주변 보행등 정보 DB 검색 테스트")
    void findNearestTrafficsTest(){

        // Given
        double lat = 37.4950;
        double lng = 127.0200;
        int radius = 1000;

        when(customTrafficRepository.findNearestTraffics(lat,lng,radius)).thenReturn(expected);

        // When
        List<TrafficResponse> results = customTrafficRepository.findNearestTraffics(lat, lng, radius);

        // Then
        Assertions.assertNotNull(results);
        Assertions.assertEquals(2, results.size());
        Assertions.assertEquals("강남구",results.get(0).getDistrict());

    }


}
