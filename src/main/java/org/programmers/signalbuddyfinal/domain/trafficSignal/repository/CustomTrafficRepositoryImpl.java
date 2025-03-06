package org.programmers.signalbuddyfinal.domain.trafficSignal.repository;

import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.programmers.signalbuddyfinal.domain.crossroad.dto.CrossroadApiResponse;
import org.programmers.signalbuddyfinal.domain.crossroad.entity.Crossroad;
import org.programmers.signalbuddyfinal.domain.crossroad.entity.QCrossroad;
import org.programmers.signalbuddyfinal.domain.trafficSignal.dto.TrafficResponse;
import org.programmers.signalbuddyfinal.domain.trafficSignal.entity.QTrafficSignal;
import org.programmers.signalbuddyfinal.domain.trafficSignal.entity.TrafficSignal;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class CustomTrafficRepositoryImpl implements CustomTrafficRepository {

    private static final QTrafficSignal qTraffic = QTrafficSignal.trafficSignal;

    private final JPAQueryFactory jqf;

    public List<TrafficResponse> findAroundTraffics(double latitude, double longitude) {

        List<TrafficResponse> trafficRes= new ArrayList<>();

        // ST_Distance_Sphere SQL 함수 사용
        String distanceExpression = "ST_Distance_Sphere(POINT({0}, {1}), coordinate)";

        List<TrafficSignal> aroundTraffics = jqf.selectFrom(qTraffic)
                .where(Expressions.numberTemplate(Double.class, distanceExpression, longitude, latitude).loe(1000)) // 거리 조건
                .fetch();

        for(TrafficSignal trafficSignal : aroundTraffics) {
            trafficRes.add(new TrafficResponse(trafficSignal));
        }

        return trafficRes;
    }

/*
    SELECT station,
    ST_Distance_Sphere(@location, POINT(longitude, latitude) AS distance
    FROM Subway
    WHERE station = "신촌역";
*/

}
