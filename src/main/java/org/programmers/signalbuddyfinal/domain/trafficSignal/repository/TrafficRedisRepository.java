package org.programmers.signalbuddyfinal.domain.trafficSignal.repository;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.programmers.signalbuddyfinal.domain.trafficSignal.dto.TrafficResponse;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Repository
@RequiredArgsConstructor
public class TrafficRedisRepository {

    private final RedisTemplate<Object, Object> redisTemplate;
    private ValueOperations<Object, Object> operations;

    private static final String KEY_PREFIX = "traffic:";

    @PostConstruct
    private void init() {
        this.operations = redisTemplate.opsForValue();
    }

    public void save(TrafficResponse trafficResponse) {
        operations.set(
                KEY_PREFIX + trafficResponse.getSerialNumber(), trafficResponse,
                Duration.ofMinutes(5)
        );
    }

    public TrafficResponse findBySerial(String serialNumber) {

        if( !serialNumber.startsWith(KEY_PREFIX)){
            serialNumber = KEY_PREFIX + serialNumber;
        }

        return (TrafficResponse) operations.get(serialNumber);
    }

}
