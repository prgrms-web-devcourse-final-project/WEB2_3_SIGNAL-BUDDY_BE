package org.programmers.signalbuddyfinal.domain.crossroad.repository;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.programmers.signalbuddyfinal.domain.crossroad.dto.CrossroadResponse;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Repository
@RequiredArgsConstructor
public class CrossroadRedisRepository {

    private final RedisTemplate<Object, Object> redisTemplate;
    private ValueOperations<Object, Object> operations;

    private static final String KEY_PREFIX = "crossroad:";

    @PostConstruct
    private void init() {
        this.operations = redisTemplate.opsForValue();
    }

    public void save(CrossroadResponse crossroadResponse) {
        operations.set(
                KEY_PREFIX + crossroadResponse.getCrossroadApiId(), crossroadResponse,
                Duration.ofMinutes(5)
        );
    }

    public CrossroadResponse findById(Long id) {
        return (CrossroadResponse) operations.get(KEY_PREFIX + id);
    }

}
