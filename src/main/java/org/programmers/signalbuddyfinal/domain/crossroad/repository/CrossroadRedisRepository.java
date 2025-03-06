package org.programmers.signalbuddyfinal.domain.crossroad.repository;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.programmers.signalbuddyfinal.domain.crossroad.entity.Crossroad;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Repository
@RequiredArgsConstructor
public class CrossroadRedisRepository {

    private final RedisTemplate<Object, Object> redisTemplate;
    private ValueOperations<Object, Object> operations;

    private static final String KEY_PREFIX = "Crossroad_";

    @PostConstruct
    private void init() {
        this.operations = redisTemplate.opsForValue();
    }

    public void save(Crossroad crossroad) {
        operations.set(
                KEY_PREFIX + crossroad.getCrossroadApiId(), crossroad,
                Duration.ofMinutes(5)
        );
    }

    public Crossroad findById(Object id) {
        return (Crossroad) operations.get(id);
    }

}
