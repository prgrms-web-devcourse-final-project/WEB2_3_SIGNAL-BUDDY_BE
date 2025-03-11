package org.programmers.signalbuddyfinal.domain.weather.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.QBean;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberTemplate;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.programmers.signalbuddyfinal.domain.weather.dto.GridResponse;
import static org.programmers.signalbuddyfinal.domain.weather.entity.QGridCoordinate.gridCoordinate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CustomGridCoordinateRepositoryImpl implements CustomGridCoordinateRepository {

    private static final QBean<GridResponse> gridDto = Projections.fields(GridResponse.class,
        gridCoordinate.gridX, gridCoordinate.gridY);

    private final JPAQueryFactory queryFactory;

    /**
     * 중심 좌표로부터 가장 가까운 순서로 결과 정렬
     *
     * @param lat 위도
     * @param lng 경도
     */
    private static NumberTemplate<Double> getDistanceSquared(double lat, double lng) {
        return Expressions.numberTemplate(Double.class,
            "(({0} - {1})*({0} - {1}) + ({2} - {3})*({2} - {3}))", gridCoordinate.lat, lat,
            gridCoordinate.lng, lng);
    }

    @Override
    public GridResponse findByLatAndLngWithRadius(double lat, double lng, double radius) {
        return queryFactory.select(gridDto).distinct().from(gridCoordinate).where(
                gridCoordinate.lat.between(lat - radius, lat + radius)
                    .and(gridCoordinate.lng.between(lng - radius, lng + radius)))
            .orderBy(getDistanceSquared(lat, lng).asc()).fetchFirst();
    }
}
