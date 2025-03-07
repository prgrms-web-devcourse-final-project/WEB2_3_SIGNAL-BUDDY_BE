package org.programmers.signalbuddyfinal.domain.crossroad.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.QBean;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Point;
import org.programmers.signalbuddyfinal.domain.crossroad.dto.CrossroadApiResponse;
import org.programmers.signalbuddyfinal.domain.crossroad.dto.CrossroadResponse;
import org.programmers.signalbuddyfinal.domain.crossroad.entity.Crossroad;
import static org.programmers.signalbuddyfinal.domain.crossroad.entity.QCrossroad.crossroad;
import org.springframework.stereotype.Repository;


@Repository
@RequiredArgsConstructor
public class CustomCrossroadRepositoryImpl implements CustomCrossroadRepository {

    private static final QBean<CrossroadResponse> crossroadDto = Projections.fields(
        CrossroadResponse.class, crossroad.crossroadId, crossroad.crossroadApiId, crossroad.name,
        Expressions.numberTemplate(Double.class, "ST_Y({0})", crossroad.coordinate).as("lat"),
        Expressions.numberTemplate(Double.class, "ST_X({0})", crossroad.coordinate).as("lng"),
        crossroad.status);
    private final JPAQueryFactory jqf;

    @Override
    public List<CrossroadResponse> findNearestCrossroads(double lat, double lng, int radius) {
        return jqf.select(crossroadDto).from(crossroad).where(
            Expressions.numberTemplate(Double.class, "ST_DISTANCE_SPHERE(POINT({0}, {1}), {2})",
                lng, lat, crossroad.coordinate).loe(radius) // 반경 내 필터링
        ).orderBy(
            Expressions.numberTemplate(Double.class, "ST_DISTANCE_SPHERE(POINT({0}, {1}), {2})",
                lng, lat, crossroad.coordinate).asc()).fetch();
    }

    @Override
    public List<Long> findByCoordinateInWithRadius(List<Point> points, int radius) {
        return jqf.select(crossroad.crossroadId).from(crossroad)
            .where(filterByRadius(points, radius)).fetch();
    }

    private BooleanExpression filterByRadius(List<Point> points, int radius) {
        return points.stream().map(
                point -> Expressions.numberTemplate(Double.class, "ST_Distance_Sphere({0}, {1})",
                    crossroad.coordinate, point).loe(radius))  // 반경 내 교차로 필터링
            .reduce(BooleanExpression::or).orElse(null);
    }
}
