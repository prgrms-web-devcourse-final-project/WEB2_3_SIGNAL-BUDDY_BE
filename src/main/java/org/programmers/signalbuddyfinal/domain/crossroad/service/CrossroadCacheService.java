package org.programmers.signalbuddyfinal.domain.crossroad.service;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.programmers.signalbuddyfinal.domain.crossroad.dto.CrossroadStateResponse;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CrossroadCacheService {

    private final RedisTemplate<Object, Object> redisTemplate;

    private static final String STATE_PREFIX = "crossroad-state:";

    public void putStateCache(Long crossroadId, CrossroadStateResponse response) {
        ValueOperations<Object, Object> operations = redisTemplate.opsForValue();

        int minTimeLeft = response.minTimeLeft();
        minTimeLeft *= 100; // 1/10초 단위를 1/1000(ms)로 변환

        operations.set(STATE_PREFIX + crossroadId, response, minTimeLeft, TimeUnit.MILLISECONDS);
    }

    public CrossroadStateResponse getStateCache(Long crossroadId) {
        ValueOperations<Object, Object> operations = redisTemplate.opsForValue();
        return (CrossroadStateResponse) operations.get(STATE_PREFIX + crossroadId);
    }
}
