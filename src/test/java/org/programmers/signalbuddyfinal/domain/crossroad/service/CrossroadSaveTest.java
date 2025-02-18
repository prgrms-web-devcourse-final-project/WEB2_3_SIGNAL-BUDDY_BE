package org.programmers.signalbuddyfinal.domain.crossroad.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.programmers.signalbuddy.domain.crossroad.entity.Crossroad;
import org.programmers.signalbuddy.domain.crossroad.repository.CrossroadRepository;
import org.programmers.signalbuddy.global.support.RepositoryTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class CrossroadSaveTest extends RepositoryTest {

    private final GeometryFactory geometryFactory = new GeometryFactory();

    @Autowired
    private CrossroadRepository crossroadRepository;

    @DisplayName("교차로 위경도 테스트")
    @Test
    void testSaveAndRetrieveCrossroad() {
        // Given: Point 생성 (경도, 위도)
        Point point = geometryFactory.createPoint(new Coordinate(127.12345, 37.12345));

        // Crossroad 엔티티 생성
        Crossroad crossroad = Crossroad.builder().crossroadApiId("API12345").name("Test Crossroad")
            .coordinate(point).build();

        // When: 엔티티 저장
        Crossroad savedCrossroad = crossroadRepository.save(crossroad);

        // Then: 저장된 데이터 검증
        Optional<Crossroad> retrievedCrossroad = crossroadRepository.findById(
            savedCrossroad.getCrossroadId());
        assertThat(retrievedCrossroad).isPresent();
        assertThat(retrievedCrossroad.get().getName()).isEqualTo("Test Crossroad");
        assertThat(retrievedCrossroad.get().getCoordinate().getX()).isEqualTo(127.12345);
        assertThat(retrievedCrossroad.get().getCoordinate().getY()).isEqualTo(37.12345);
    }
}