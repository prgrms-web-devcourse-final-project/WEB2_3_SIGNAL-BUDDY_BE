package org.programmers.signalbuddyfinal.domain.crossroad.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.programmers.signalbuddyfinal.domain.crossroad.dto.CrossroadResponse;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.core.GeoOperations;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org. springframework.data.redis.connection.RedisGeoCommands.GeoLocation;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Repository
public class CrossroadRedisRepository {

    private final RedisTemplate<Object, Object> redisTemplate;
    private final HashOperations<Object, Object, Map<String, String>> hashOperations;
    private final GeoOperations<Object, Object> geoOperations;

    private static final String KEY_HASH = "crossroad:info";
    private static final String KEY_GEO = "crossroad:geo";
    private static final Duration TTL = Duration.ofMinutes(5);

    public CrossroadRedisRepository(RedisTemplate<Object, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.hashOperations = redisTemplate.opsForHash();
        this.geoOperations = redisTemplate.opsForGeo();
    }

    public void save(CrossroadResponse crossroadResponse) {

            Long crossroadId = crossroadResponse.getCrossroadId();
            String idStr = crossroadId.toString();

            // GEO 저장: 경도(lng), 위도(lat) 순서로
            redisTemplate.opsForGeo().add(
                KEY_GEO,
                new Point(crossroadResponse.getLng(), crossroadResponse.getLat()),
                idStr
            );

            // HASH 저장
            Map<String, String> crossroadData = new HashMap<>();
            crossroadData.put("crossroadApiId", crossroadResponse.getCrossroadApiId());
            crossroadData.put("name", crossroadResponse.getName());
            crossroadData.put("status", crossroadResponse.getStatus());
            System.out.println("HASH data prepared: " + crossroadData);

            hashOperations.put(KEY_HASH, idStr, crossroadData);

            // GEO와 HASH 모두에 TTL 설정
            redisTemplate.expire(KEY_GEO, TTL);
            redisTemplate.expire(KEY_HASH, TTL);
            System.out.println("TTL set for both GEO and HASH");

    }

    public List<CrossroadResponse> findNearbyCrossroads(double lat, double lng, double radius) {

            List<GeoResult<GeoLocation<Object>>> results;
            // 반경 내 GEO 데이터 조회
            if (geoOperations!=null){
                // 반경 내 GEO 데이터 조회: 경도(lng), 위도(lat) 순서
                 results = geoOperations.radius(
                    KEY_GEO,
                    new Circle(new Point(lng, lat), new Distance(radius, Metrics.KILOMETERS))
                ).getContent();
            } else {
                return List.of();
            }

            if (results.isEmpty()) {
                return List.of();
            }

            List<CrossroadResponse> crossroadResponses = new ArrayList<>();
            for (GeoResult<GeoLocation<Object>> result : results) {
                String idStr = result.getContent().getName().toString();

                CrossroadResponse response = findById(Long.valueOf(idStr));

                crossroadResponses.add(response);
            }
            return crossroadResponses;

    }

    public CrossroadResponse findById(Long id) {

        String idStr = id.toString();
        Map<String, String> data = hashOperations.get(KEY_HASH, idStr);
        if (data == null) {
            return null;
        }

        List<Point> positions = geoOperations.position(KEY_GEO, idStr);
        if (positions == null || positions.isEmpty()) {
            return null;
        }

        Point point = positions.get(0);
        double savedLat = point.getY();  // 위도
        double savedLng = point.getX();  // 경도

        return CrossroadResponse.builder()
            .crossroadId(id)
            .crossroadApiId(data.get("crossroadApiId"))
            .name(data.get("name"))
            .status(data.get("status"))
            .lat(savedLat)
            .lng(savedLng)
            .build();
    }
}