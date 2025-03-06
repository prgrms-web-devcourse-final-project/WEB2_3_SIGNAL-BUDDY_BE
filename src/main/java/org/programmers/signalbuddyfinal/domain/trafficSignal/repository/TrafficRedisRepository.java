package org.programmers.signalbuddyfinal.domain.trafficSignal.repository;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.programmers.signalbuddyfinal.domain.trafficSignal.entity.TrafficSignal;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Repository
@RequiredArgsConstructor
public class TrafficRedisRepository {

    private final RedisTemplate<Object, Object> redisTemplate;
    private ValueOperations<Object, Object> operations;

    private static final String KEY_PREFIX = "Traffic_";

    @PostConstruct
    private void init() {
        this.operations = redisTemplate.opsForValue();
    }

    public void save(TrafficSignal TrafficSignal) {
        operations.set(
                KEY_PREFIX + TrafficSignal.getSerialNumber(), TrafficSignal,
                Duration.ofMinutes(5)
        );
    }

    public TrafficSignal findById(Object id) {
        return (TrafficSignal) operations.get(id);
    }

}
