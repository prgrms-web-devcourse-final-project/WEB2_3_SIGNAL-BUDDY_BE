package org.programmers.signalbuddyfinal.global.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import java.time.Duration;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.programmers.signalbuddyfinal.domain.auth.dto.NewTokenResponse;
import org.programmers.signalbuddyfinal.domain.auth.exception.AuthErrorCode;
import org.programmers.signalbuddyfinal.global.exception.BusinessException;
import org.programmers.signalbuddyfinal.global.exception.GlobalErrorCode;
import org.programmers.signalbuddyfinal.global.security.basic.CustomUserDetails;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class JwtService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, String> redisTemplate;

    public NewTokenResponse reissue(String refreshToken, String accessToken) {

        String extractAccessToken = jwtUtil.extractAccessToken(accessToken);

        Claims claimsAccessToken = extractClaimsFromToken("accessToken", extractAccessToken);
        Claims claimsRefreshToken = extractClaimsFromToken("refreshToken", refreshToken);

        String memberIdFromAccessToken = claimsAccessToken.getSubject();
        String memberIdFromRefreshToken = claimsRefreshToken.getSubject();

        if (!memberIdFromRefreshToken.equals(memberIdFromAccessToken)) {
            throw new BusinessException(GlobalErrorCode.BAD_REQUEST);
        }

        validateAccessTokenExpiration(claimsAccessToken, extractAccessToken);

        String existingRefreshToken = refreshTokenRepository.findByMemberId(memberIdFromRefreshToken);
        if (existingRefreshToken == null) {
            throw new BusinessException(AuthErrorCode.UNAUTHORIZED);
        }

        Authentication authentication = jwtUtil.getAuthentication(refreshToken);
        String newAccessToken = jwtUtil.generateAccessToken(authentication);
        String newRefreshToken = jwtUtil.generateRefreshToken(authentication);

        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
        Authentication newAuthentication = new UsernamePasswordAuthenticationToken(
            customUserDetails, null, customUserDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(newAuthentication);

        return new NewTokenResponse(newAccessToken, newRefreshToken);
    }

    public void logout(String accessToken) {

        String extractAccessToken = jwtUtil.extractAccessToken(accessToken);
        Claims claimsAccessToken = extractClaimsFromToken("accessToken", extractAccessToken);

        // 액세스 토큰 블랙리스트 처리
        addBlackListExistingAccessToken(extractAccessToken, claimsAccessToken.getExpiration());

        // 리프레시 토큰 삭제
        if(!refreshTokenRepository.findByMemberId(claimsAccessToken.getSubject()).isEmpty())
        {refreshTokenRepository.delete(claimsAccessToken.getSubject());}

    }

    // 토큰 재발급 시, 액세스 토큰 검증
    private void validateAccessTokenExpiration(Claims accessTokenClaims, String accessToken) {

            Date accessTokenExpirationDate = accessTokenClaims.getExpiration();

            if(accessTokenExpirationDate.after(new Date())) {
                addBlackListExistingAccessToken(accessToken, accessTokenExpirationDate);
            }
    }

    private Claims extractClaimsFromToken(String type, String token) {

        try {
            return jwtUtil.parseToken(token);
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

    // 기존의 액세스 토큰을 블랙리스트로 추가
    private void addBlackListExistingAccessToken(String accessToken, Date expirationDate) {
        redisTemplate.opsForValue()
            .set("blacklist:access-token:" + accessToken, expirationDate.toString(),
                Duration.between(new Date().toInstant(), expirationDate.toInstant()).getSeconds(),
                TimeUnit.SECONDS);
    }
}
