package org.programmers.signalbuddyfinal.domain.like.service;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.programmers.signalbuddyfinal.domain.like.dto.LikeRequestType;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LikeCacheService {

    private final StringRedisTemplate redisTemplate;

    private static final String LIKE_KEY_PREFIX = "like:";

    public void addLike(String key) {
        redisTemplate.opsForValue()
            .set(key, LikeRequestType.ADD.name(), 3L, TimeUnit.MINUTES);
    }

    public void cancelLike(String key) {
        redisTemplate.opsForValue()
            .set(key, LikeRequestType.CANCEL.name(), 3L, TimeUnit.MINUTES);
    }

    public boolean exists(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public void delete(String key) {
        redisTemplate.delete(key);
    }

    public String getLikeType(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public static String generateKey(Long feedbackId, Long memberId) {
        return LIKE_KEY_PREFIX + feedbackId + ":" + memberId;
    }

    public static String getLikeKeyPrefix() {
        return LIKE_KEY_PREFIX;
    }
}
