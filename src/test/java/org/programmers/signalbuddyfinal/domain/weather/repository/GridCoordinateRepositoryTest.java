package org.programmers.signalbuddyfinal.domain.weather.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.programmers.signalbuddyfinal.domain.weather.dto.GridResponse;
import org.programmers.signalbuddyfinal.domain.weather.entity.GridCoordinate;
import org.programmers.signalbuddyfinal.global.support.RepositoryTest;
import org.springframework.beans.factory.annotation.Autowired;

class GridCoordinateRepositoryTest extends RepositoryTest {

    @Autowired
    private GridCoordinateRepository gridCoordinateRepository;


    @BeforeEach
    void setUp() {
        final List<GridCoordinate> gridCoordinateList = List.of(
            GridCoordinate.builder().regionCode("11").city("서울특별시").district("중구").subdistrict("명동")
                .gridX(60.0).gridY(127.0).lng(126.9780).lat(37.5665).build(),

            GridCoordinate.builder().regionCode("26").city("부산광역시").district("해운대구")
                .subdistrict("좌동").gridX(98.0).gridY(76.0).lng(129.0756).lat(35.1796).build(),

            GridCoordinate.builder().regionCode("28").city("인천광역시").district("남동구")
                .subdistrict("구월동").gridX(55.0).gridY(123.0).lng(126.7052).lat(37.4563).build(),

            GridCoordinate.builder().regionCode("30").city("대전광역시").district("서구")
                .subdistrict("둔산동").gridX(67.0).gridY(89.0).lng(127.3845).lat(36.3504).build());

        gridCoordinateRepository.saveAll(gridCoordinateList);
    }

    @DisplayName("주어진 위치 반경 내에서 가장 가까운 좌표 조회")
    @Test
    void findByLatAndLngWithRadius() {
        final double searchLat = 37.5665;
        final double searchLng = 126.9780;
        final double radius = 0.05;

        final GridResponse response = gridCoordinateRepository.findByLatAndLngWithRadius(
            searchLat, searchLng, radius);

        assertThat(response).isNotNull();
        assertThat(response.getGridX()).isEqualTo(60.0);
        assertThat(response.getGridY()).isEqualTo(127.0);
    }
}