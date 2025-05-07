package org.programmers.signalbuddyfinal.global.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.programmers.signalbuddyfinal.global.exception.BusinessException;
import org.programmers.signalbuddyfinal.global.security.basic.CustomUserDetails;
import org.programmers.signalbuddyfinal.global.security.basic.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class JwtUtil {

    private final CustomUserDetailsService customUserDetailsService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final Key key;
    private final RedisTemplate<String, String> redisTemplate;

    private Long accessTokenExpiration;
    private Long refreshTokenExpiration;

    public JwtUtil(@Value("${jwt.secret}") String secretKey,
        @Value("${jwt.access-token-expiration-time}") Long accessTokenExpiration,
        @Value("${jwt.refresh-token-expiration-time}") Long refreshTokenExpiration,
        RefreshTokenRepository refreshTokenRepository,
        CustomUserDetailsService customUserDetailsService,
        RedisTemplate<String, String> redisTemplate) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.customUserDetailsService = customUserDetailsService;
        this.redisTemplate = redisTemplate;
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    public String generateAccessToken(Authentication authentication) {

        CustomUserDetails nowMember = authentication2User(authentication);
        Instant nowTime = Instant.now();

        return Jwts.builder()
            .subject(String.valueOf(nowMember.getMemberId()))
            .claim("status", nowMember.getStatus().name())
            .claim("auth", nowMember.getRole().name())
            .issuedAt(Date.from(nowTime))
            .expiration(Date.from(nowTime.plus(Duration.ofMillis(accessTokenExpiration))))
            .signWith(key)
            .compact();
    }

    @Transactional
    public String generateRefreshToken(Authentication authentication) {

        CustomUserDetails nowMember = authentication2User(authentication);
        Instant nowTime = Instant.now();

        String refreshToken = Jwts.builder()
            .subject(String.valueOf(nowMember.getMemberId()))
            .issuedAt(Date.from(nowTime))
            .expiration(Date.from(nowTime.plus(Duration.ofMillis(refreshTokenExpiration))))
            .signWith(key)
            .compact();

        refreshTokenRepository.save(nowMember.getMemberId(), refreshToken);
        return refreshToken;
    }

    public Claims parseToken(String token) {

        return Jwts.parser()
                .verifyWith((SecretKey) key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Claims extractClaimsOrThrow(String type, String token) {

        try {
            return parseToken(token);
        } catch (ExpiredJwtException e) {
            log.info(e.getMessage());
            if (type.equals("accessToken")) {
                return e.getClaims();
            }
            throw new BusinessException(TokenErrorCode.EXPIRED_REFRESH_TOKEN);
        } catch (JwtException e) {
            log.info(e.getMessage());
            throw new BusinessException(TokenErrorCode.INVALID_TOKEN);
        }
    }

    public String extractAccessToken(String bearerToken){

        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
            throw new BusinessException(TokenErrorCode.ACCESS_TOKEN_NOT_EXIST);
        }
        return bearerToken.substring(7);
    }

    public Authentication getAuthentication(String token) {

        String memberId = parseToken(token).getSubject();

        CustomUserDetails customUserDetails = customUserDetailsService.loadUserByUsername(String.valueOf(memberId));
        return new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());
    }

    private CustomUserDetails authentication2User(Authentication authentication) {
        return (CustomUserDetails) authentication.getPrincipal();
    }

    public void addBlackListExistingAccessToken(String accessToken, Date expirationDate) {

        redisTemplate.opsForValue()
            .set("pending-blacklist:access-token:"+accessToken, "pending",5, TimeUnit.MINUTES);

        redisTemplate.opsForValue()
            .set("blacklist:access-token:" + accessToken, expirationDate.toString(),
                Duration.between(new Date().toInstant(), expirationDate.toInstant()).getSeconds(),
                TimeUnit.SECONDS);
    }

    public boolean checkBlacklist(String accessToken) {
        Boolean isInBlackList = redisTemplate.hasKey("blacklist:access-token:" + accessToken);
        Boolean isInPendingBlackList = redisTemplate.hasKey(("pending-blacklist:access-token:" + accessToken));

        return Boolean.TRUE.equals(isInBlackList)&& Boolean.FALSE.equals(isInPendingBlackList);
    }

    public void validateAccessTokenExpiration(Claims accessTokenClaims, String accessToken) {
        Date accessTokenExpirationDate = accessTokenClaims.getExpiration();

        if(accessTokenExpirationDate.after(new Date())) {
            addBlackListExistingAccessToken(accessToken, accessTokenExpirationDate);
        }
    }

    // 테스트용
    public String generateAccessTokenWithShortExpiration(Long memberId) {

        return Jwts.builder()
            .subject(String.valueOf(memberId))
            .issuedAt(new Date())
            .expiration(new Date(new Date().getTime() + 10 * 1000))
            .signWith(key)
            .compact();
    }
}
