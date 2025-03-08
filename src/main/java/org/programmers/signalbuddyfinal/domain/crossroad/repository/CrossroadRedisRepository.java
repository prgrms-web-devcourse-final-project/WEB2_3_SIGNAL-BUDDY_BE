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
        try {
            Long crossroadId = crossroadResponse.getCrossroadId();
            if (crossroadId == null) {
                throw new IllegalArgumentException("Crossroad ID cannot be null");
            }
            String idStr = crossroadId.toString();
            System.out.println("Saving crossroadId: " + crossroadId);
            System.out.println("Lat: " + crossroadResponse.getLat() + ", Lng: " + crossroadResponse.getLng());

            // GEO 저장: 경도(lng), 위도(lat) 순서로
            redisTemplate.opsForGeo().add(
                KEY_GEO,
                new Point(crossroadResponse.getLng(), crossroadResponse.getLat()),
                idStr
            );
            System.out.println("GEO data saved for key: " + KEY_GEO);

            // HASH 저장
            Map<String, String> crossroadData = new HashMap<>();
            crossroadData.put("crossroadApiId", crossroadResponse.getCrossroadApiId());
            crossroadData.put("name", crossroadResponse.getName());
            crossroadData.put("status", crossroadResponse.getStatus());
            System.out.println("HASH data prepared: " + crossroadData);

            hashOperations.put(KEY_HASH, idStr, crossroadData);
            System.out.println("HASH data saved for key: " + KEY_HASH);

            // GEO와 HASH 모두에 TTL 설정
            redisTemplate.expire(KEY_GEO, TTL);
            redisTemplate.expire(KEY_HASH, TTL);
            System.out.println("TTL set for both GEO and HASH");
        } catch (Exception e) {
            System.err.println("Error saving to Redis: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to save crossroad data", e);
        }
    }

    public List<CrossroadResponse> findNearbyCrossroads(double lat, double lng, double radius) {
        try {
            System.out.println("findNearbyCrossroads: lat=" + lat + ", lng=" + lng + ", radius=" + radius);

            // 반경 내 GEO 데이터 조회: 경도(lng), 위도(lat) 순서
            List<GeoResult<GeoLocation<Object>>> results = geoOperations.radius(
                KEY_GEO,
                new Circle(new Point(lng, lat), new Distance(radius, Metrics.KILOMETERS))
            ).getContent();

            if (results.isEmpty()) {
                System.out.println("No nearby crossroads found");
                return List.of();
            }

            List<CrossroadResponse> crossroadResponses = new ArrayList<>();
            for (GeoResult<GeoLocation<Object>> result : results) {
                String idStr = result.getContent().getName().toString();

                CrossroadResponse response = findById(Long.valueOf(idStr));

                crossroadResponses.add(response);
            }
            return crossroadResponses;
        } catch (Exception e) {
            System.err.println("Error finding nearby crossroads: " + e.getMessage());
            throw new RuntimeException("Failed to find nearby crossroads", e);
        }
    }

    public CrossroadResponse findById(Long id) {
        System.out.println("findById: crossroadId=" + id);

        String idStr = id.toString();
        Map<String, String> data = hashOperations.get(KEY_HASH, idStr);
        if (data == null) {
            System.out.println("No HASH data found for crossroadId=" + id);
            return null;
        }

        List<Point> positions = geoOperations.position(KEY_GEO, idStr);
        if (positions == null || positions.isEmpty()) {
            System.out.println("No GEO data found for crossroadId=" + id);
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