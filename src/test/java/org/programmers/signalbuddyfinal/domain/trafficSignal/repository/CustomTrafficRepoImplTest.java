package org.programmers.signalbuddyfinal.domain.trafficSignal.repository;

import jakarta.persistence.EntityManager;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.programmers.signalbuddyfinal.domain.trafficSignal.dto.TrafficFileResponse;
import org.programmers.signalbuddyfinal.domain.trafficSignal.dto.TrafficResponse;
import org.programmers.signalbuddyfinal.domain.trafficSignal.entity.TrafficSignal;
import org.programmers.signalbuddyfinal.global.support.RepositoryTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

@Import({CustomTrafficRepositoryImpl.class})
public class CustomTrafficRepoImplTest extends RepositoryTest {

    @Autowired
    private CustomTrafficRepositoryImpl customTrafficRepository;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {

        TrafficFileResponse response = TrafficFileResponse.builder()
            .serial(1L)
            .district("용산구")
            .signalType("보행신호등")
            .address("서울특별시 용산구 한강대로 405")
            .lat(37.5546)
            .lng(126.9706)
            .build();

        TrafficSignal signal = new TrafficSignal(response);

        entityManager.persist(signal);
        entityManager.flush();
    }

    @Test
    @DisplayName("주변 보행등 정보 DB 검색 테스트")
    void findNearestTrafficsTest(){

        // Given
        double lat = 37.5546;
        double lng = 126.9706;
        int radius = 10000;

        // When
        List<TrafficResponse> results = customTrafficRepository.findNearestTraffics(lat, lng, radius);

        // Then
        Assertions.assertNotNull(results);
        Assertions.assertNotEquals(0, results.size());

    }


}
