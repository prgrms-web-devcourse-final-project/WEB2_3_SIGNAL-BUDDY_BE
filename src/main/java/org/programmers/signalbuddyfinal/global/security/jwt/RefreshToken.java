package org.programmers.signalbuddyfinal.global.security.jwt;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@RedisHash(value = "token", timeToLive = 604800)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
public class RefreshToken {

    private String memberId;
    private String refreshToken;
}
