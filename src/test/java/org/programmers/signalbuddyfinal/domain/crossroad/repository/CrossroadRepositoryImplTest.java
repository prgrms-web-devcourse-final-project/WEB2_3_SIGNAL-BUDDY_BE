package org.programmers.signalbuddyfinal.domain.crossroad.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.programmers.signalbuddyfinal.domain.crossroad.entity.Crossroad;
import org.programmers.signalbuddyfinal.global.support.RepositoryTest;
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

    private void saveCrossroad(String apiId, String name, double lat, double lng) {
        crossroadRepository.save(
            Crossroad.create()
                .crossroadApiId(apiId).name(name)
                .lat(lat).lng(lng)
                .build()
        );
    }
}