package org.programmers.signalbuddyfinal.domain.trafficSignal.repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.programmers.signalbuddyfinal.domain.trafficSignal.dto.TrafficResponse;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands.GeoLocation;
import org.springframework.data.redis.core.GeoOperations;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Repository
public class TrafficRedisRepository {

    private final RedisTemplate<Object, Object> redisTemplate;
    private final HashOperations<Object, Object, Map<String,String>> hashOperations;
    private final GeoOperations<Object,Object> geoOperations;

    private static final String KEY_HASH = "traffic:info";
    private static final String KEY_GEO = "traffic:geo";
    private static final Duration TTL = Duration.ofMinutes(5);

    public TrafficRedisRepository(RedisTemplate<Object,Object> redisTemplate){
        this.redisTemplate = redisTemplate;
        this.hashOperations = redisTemplate.opsForHash();
        this.geoOperations = redisTemplate.opsForGeo();
    }

    public void save(TrafficResponse trafficResponse) {

            Long trafficId = trafficResponse.getTrafficSignalId();

            // GEO 데이터 저장
            redisTemplate.opsForGeo().add(
                KEY_GEO,
                new Point(trafficResponse.getLng(),trafficResponse.getLat()),
                trafficId.toString()
            );

            // HASH 데이터 저장
            Map<String, String> trafficData = new HashMap<>();
            trafficData.put("serialNumber", String.valueOf(trafficResponse.getSerialNumber()));
            trafficData.put("district", trafficResponse.getDistrict());
            trafficData.put("signalType", trafficResponse.getSignalType());
            trafficData.put("address", trafficResponse.getAddress());

            hashOperations.put(KEY_HASH, trafficId.toString(), trafficData);

            // GEO와 HASH 모두에 TTL 설정
            redisTemplate.expire(KEY_GEO, TTL);
            redisTemplate.expire(KEY_HASH, TTL);

    }

    public List<TrafficResponse> findNearbyTraffics(double lat, double lng, double radius) {

            List<GeoResult<GeoLocation<Object>>> geoResults;
            // 반경 내 GEO 데이터 조회
            if (geoOperations!=null){
                geoResults = geoOperations.radius(
                    KEY_GEO,
                    new Circle(new Point(lng, lat), new Distance(radius, Metrics.KILOMETERS))
                ).getContent();
            } else {
                return List.of();
            }

            if (geoResults.isEmpty()) {
                return Collections.emptyList();
            }

            List<TrafficResponse> trafficResponses = new ArrayList<>();

            for (GeoResult<GeoLocation<Object>> result : geoResults) {
                String trafficId = result.getContent().getName().toString(); // GEO에서 가져온 ID

                TrafficResponse response = findById(Long.valueOf(trafficId));

                trafficResponses.add(response);
            }

            return trafficResponses;
    }


    public TrafficResponse findById(Long id) {

        String trafficId = String.valueOf(id);

        Map<String, String> data = hashOperations.get(KEY_HASH, trafficId);
        if (data == null) {
            return null;
        }

        List<Point> positions = geoOperations.position(KEY_GEO, trafficId);

        if (positions == null || positions.isEmpty()) {
            return null;
        }

        Point point = positions.get(0);
        double savedLat = point.getY();  // 위도
        double savedLng = point.getX();  // 경도

        return TrafficResponse.builder()
            .trafficSignalId(id)
            .serialNumber(Long.valueOf(data.get("serialNumber")))
            .district(data.get("district"))
            .signalType(data.get("signalType"))
            .address(data.get("address"))
            .lat(savedLat)
            .lng(savedLng)
            .build();
    }

}
