package org.programmers.signalbuddyfinal.domain.crossroad.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Point;
import org.programmers.signalbuddyfinal.domain.crossroad.entity.Crossroad;
import org.programmers.signalbuddyfinal.global.support.RepositoryTest;
import org.programmers.signalbuddyfinal.global.util.PointUtils;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

class CrossroadRepositoryImplTest extends RepositoryTest {

    @MockitoSpyBean
    private CrossroadRepository crossroadRepository;

    private final double centerLat = 37.540899;
    private final double centerLng = 127.072285;

    @BeforeEach
    void setUp() {
        // 3km 이내
        saveCrossroad("1", "AA사거리", centerLat, centerLng);
        saveCrossroad("2", "BB사거리", centerLat, centerLng + 0.01);
        saveCrossroad("3", "CC사거리", centerLat + 0.01, centerLng);

        // 3km 외
        saveCrossroad("4", "DD사거리", centerLat - 0.04, centerLng);
        saveCrossroad("5", "EE사거리", centerLat, centerLng - 0.05);
    }

    @DisplayName("반경 3km 내 교차로 데이터를 가져온다. (MBRContains 함수 확인)")
    @Test
    void findNearestCrossroads() {
        // Given
        int radius = 3000;

        // When & Then
        assertThat(crossroadRepository.findNearestCrossroads(centerLat, centerLng, radius))
            .hasSize(3);
    }

    @DisplayName("특정 좌표들의 반경 5m 내 교차로 데이터를 가져온다. (MBRContains 함수 확인)")
    @Test
    void findByCoordinateInWithRadius() {
        // Given
        List<Point> points = List.of(
            // 포함 O
            PointUtils.toPoint(centerLat, centerLng + 0.01004),
            PointUtils.toPoint(centerLat + 0.00004, centerLng),

            // 포함 X
            PointUtils.toPoint(centerLat + 0.0101, centerLng),
            PointUtils.toPoint(centerLat, centerLng - 0.05007),
            PointUtils.toPoint(centerLat - 0.04008, centerLng)
        );

        // When & Then
        assertThat(crossroadRepository.findByCoordinateInWithRadius(points, 5))
            .hasSize(2);
    }

    private void saveCrossroad(String apiId, String name, double lat, double lng) {
        crossroadRepository.save(
            Crossroad.create()
                .crossroadApiId(apiId).name(name)
                .lat(lat).lng(lng)
                .build()
        );
    }
}