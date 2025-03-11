package org.programmers.signalbuddyfinal.domain.trafficSignal.repository;

import static org.programmers.signalbuddyfinal.domain.trafficSignal.entity.QTrafficSignal.trafficSignal;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.QBean;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.programmers.signalbuddyfinal.domain.trafficSignal.dto.TrafficResponse;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class CustomTrafficRepositoryImpl implements CustomTrafficRepository {

    private static final QBean<TrafficResponse> trafficRes = Projections.fields(
        TrafficResponse.class,
        trafficSignal.trafficSignalId,trafficSignal.serialNumber, trafficSignal.district, trafficSignal.signalType, trafficSignal.address,
        Expressions.numberTemplate(Double.class, "ST_Y({0})", trafficSignal.coordinate).as("lat"),
        Expressions.numberTemplate(Double.class, "ST_X({0})", trafficSignal.coordinate).as("lng")
    );
    private final JPAQueryFactory jqf;

    @Override
    public List<TrafficResponse> findNearestTraffics(double lat, double lng, int radius) {
        return jqf.select(trafficRes).from(trafficSignal).where(
            Expressions.numberTemplate(Double.class, "ST_DISTANCE_SPHERE(POINT({0}, {1}), {2})",
                lng, lat, trafficSignal.coordinate).loe(radius) // 반경 내 필터링
        ).orderBy(
            Expressions.numberTemplate(Double.class, "ST_DISTANCE_SPHERE(POINT({0}, {1}), {2})",
                lng, lat, trafficSignal.coordinate).asc()).fetch();
    }

}
