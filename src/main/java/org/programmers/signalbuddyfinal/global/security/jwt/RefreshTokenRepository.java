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

    // 리프레시 토큰 저장
    public void save(Long memberId, String refreshToken) {
        redisTemplate.opsForValue()
            .set(PREFIX + String.valueOf(memberId), refreshToken, 7, TimeUnit.DAYS);
    }

    // 리프레시 토큰 조회
    public String findByMemberId(String memberId) {
        return redisTemplate.opsForValue().get(PREFIX + memberId);
    }

    // 리프레시 토큰 삭제
    public void delete(String memberId) {
        redisTemplate.delete(PREFIX + memberId);
    }
}
