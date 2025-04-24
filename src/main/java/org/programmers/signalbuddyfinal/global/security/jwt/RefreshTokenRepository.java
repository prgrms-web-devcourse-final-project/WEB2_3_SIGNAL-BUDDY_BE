package org.programmers.signalbuddyfinal.global.security.jwt;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRepository {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String PREFIX = "refresh_token:member:";

    public void save(Long memberId, String refreshToken) {
        redisTemplate.opsForValue()
            .set(PREFIX + String.valueOf(memberId), refreshToken, 7, TimeUnit.DAYS);
    }

    public String findByMemberId(String memberId) {
        return redisTemplate.opsForValue().get(PREFIX + memberId);
    }

    public void delete(String memberId) {
        redisTemplate.delete(PREFIX + memberId);
    }
}
