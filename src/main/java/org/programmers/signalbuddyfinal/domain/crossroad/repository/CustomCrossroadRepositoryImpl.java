package org.programmers.signalbuddyfinal.domain.crossroad.repository;

import static org.programmers.signalbuddyfinal.domain.crossroad.entity.QCrossroad.crossroad;
import static org.programmers.signalbuddyfinal.global.util.QueryDslUtils.mbrContains;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.QBean;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Point;
import org.programmers.signalbuddyfinal.domain.crossroad.dto.CrossroadResponse;
import org.programmers.signalbuddyfinal.global.util.PointUtils;
import org.programmers.signalbuddyfinal.global.util.QueryDslUtils;
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
        Point point = PointUtils.toPoint(lat, lng);
        NumberExpression<Double> distanceSphere = QueryDslUtils.distanceSphere(
            crossroad.coordinate, point
        );

        return jqf.select(crossroadDto).from(crossroad)
            .where(
                mbrContains(point, radius, crossroad.coordinate).isTrue(),
                distanceSphere.loe(radius)
            )
            .orderBy(distanceSphere.asc()).fetch();
    }

    @Override
    public List<Long> findByCoordinateInWithRadius(List<Point> points, int radius) {
        return jqf.select(crossroad.crossroadId).from(crossroad)
            .where(filterByRadius(points, radius)).fetch();
    }

    private BooleanExpression filterByRadius(List<Point> points, int radius) {
        return points.stream()
            // 반경 내 교차로 필터링
            .map(point -> QueryDslUtils.distanceSphere(crossroad.coordinate, point).loe(radius))
            .reduce(BooleanExpression::or)
            .orElse(null);
    }
}
