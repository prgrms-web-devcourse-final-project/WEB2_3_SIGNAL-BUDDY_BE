package org.programmers.signalbuddyfinal.global.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.programmers.signalbuddyfinal.global.exception.BusinessException;
import org.programmers.signalbuddyfinal.global.security.basic.CustomUserDetails;
import org.programmers.signalbuddyfinal.global.security.basic.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${jwt.access-token-expiration-time}")
    private Long accessTokenExpiration;
    @Value("${jwt.refresh-token-expiration-time}")
    private Long refreshTokenExpiration;

    public JwtUtil(@Value("${jwt.secret}") String secretKey,
        RefreshTokenRepository refreshTokenRepository,
        CustomUserDetailsService customUserDetailsService) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.customUserDetailsService = customUserDetailsService;
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessToken(Authentication authentication) {

        CustomUserDetails nowMember = authentication2User(authentication);

        return Jwts.builder()
            .subject(String.valueOf(nowMember.getMemberId()))
            .claim("status", nowMember.getStatus().name())
            .claim("auth", nowMember.getRole().name())
            .issuedAt(new Date())
            .expiration(new Date(new Date().getTime() + accessTokenExpiration))
            .signWith(key)
            .compact();
    }

    @Transactional
    public String generateRefreshToken(Authentication authentication) {

        CustomUserDetails nowMember = authentication2User(authentication);

        String refreshToken = Jwts.builder()
            .subject(String.valueOf(nowMember.getMemberId()))
            .issuedAt(new Date())
            .expiration(new Date(new Date().getTime() + refreshTokenExpiration))
            .signWith(key)
            .compact();

        // 리프레시 토큰 저장
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

    public String extractAccessToken(String bearerToken){

        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
            throw new BusinessException(TokenErrorCode.ACCESS_TOKEN_NOT_EXIST);
        }
        return bearerToken.substring(7);
    }

    // authentication 추출
    public Authentication getAuthentication(String token) {

        String memberId = parseToken(token).getSubject();

        CustomUserDetails customUserDetails = customUserDetailsService.loadUserByUsername(String.valueOf(memberId));
        return new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());
    }

    private CustomUserDetails authentication2User(Authentication authentication) {
        return (CustomUserDetails) authentication.getPrincipal();
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
