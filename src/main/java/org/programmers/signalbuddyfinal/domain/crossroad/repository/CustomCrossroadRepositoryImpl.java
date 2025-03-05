package org.programmers.signalbuddyfinal.domain.crossroad.repository;

import static org.programmers.signalbuddyfinal.domain.crossroad.entity.QCrossroad.crossroad;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.programmers.signalbuddyfinal.domain.crossroad.dto.CrossroadApiResponse;
import org.programmers.signalbuddyfinal.domain.crossroad.dto.CrossroadResponse;
import org.programmers.signalbuddyfinal.domain.crossroad.entity.Crossroad;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CustomCrossroadRepositoryImpl implements CustomCrossroadRepository {

    private final JPAQueryFactory jqf;

    @Override
    public List<CrossroadApiResponse> findNearByCrossroads(double latitude, double longitude) {
        List<CrossroadApiResponse> res = new ArrayList<>();
        // ST_Distance_Sphere SQL 함수 사용
        String distanceExpression = "ST_Distance_Sphere(POINT({0}, {1}), coordinate)";

        List<Crossroad> nearCrossroads = jqf.selectFrom(crossroad).where(
                Expressions.numberTemplate(Double.class, distanceExpression, longitude, latitude)
                    .loe(80)) // 거리 조건
            .fetch();

        for (Crossroad near : nearCrossroads) {
            res.add(new CrossroadApiResponse(near));
        }

        return res;
    }


    @Override
    public List<CrossroadResponse> findNearestCrossroads(double lat, double lng, int radius) {
        return jqf.select(Projections.fields(CrossroadResponse.class, crossroad.crossroadId,
            crossroad.crossroadApiId, crossroad.name,
            Expressions.numberTemplate(Double.class, "ST_Y({0})", crossroad.coordinate).as("lat"),
            Expressions.numberTemplate(Double.class, "ST_X({0})", crossroad.coordinate).as("lng"),
            crossroad.status,
            Expressions.numberTemplate(Double.class, "ST_DISTANCE_SPHERE(POINT({0}, {1}), {2})",
                lng, lat, crossroad.coordinate).as("distance"))).from(crossroad).where(
            Expressions.numberTemplate(Double.class, "ST_DISTANCE_SPHERE(POINT({0}, {1}), {2})",
                lng, lat, crossroad.coordinate).loe(radius) // 반경 내 필터링
        ).orderBy(
            Expressions.numberTemplate(Double.class, "ST_DISTANCE_SPHERE(POINT({0}, {1}), {2})",
                lng, lat, crossroad.coordinate).asc()).fetch();
    }
}
