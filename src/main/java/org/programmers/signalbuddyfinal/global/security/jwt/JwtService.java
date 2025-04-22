package org.programmers.signalbuddyfinal.global.security.jwt;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.programmers.signalbuddyfinal.domain.auth.dto.NewTokenResponse;
import org.programmers.signalbuddyfinal.domain.auth.exception.AuthErrorCode;
import org.programmers.signalbuddyfinal.global.exception.BusinessException;
import org.programmers.signalbuddyfinal.global.exception.GlobalErrorCode;
import org.programmers.signalbuddyfinal.global.response.ApiResponse;
import org.programmers.signalbuddyfinal.global.security.basic.CustomUserDetails;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
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

    public NewTokenResponse reissue(String accessToken, String refreshToken) {
log.info("accessToken: {} refreshToken: {}", accessToken, refreshToken);
        String extractAccessToken = jwtUtil.extractAccessToken(accessToken);

        Claims claimsAccessToken = jwtUtil.extractClaimsOrThrow("accessToken", extractAccessToken);
        Claims claimsRefreshToken = jwtUtil.extractClaimsOrThrow("refreshToken", refreshToken);

        String memberIdFromAccessToken = claimsAccessToken.getSubject();
        String memberIdFromRefreshToken = claimsRefreshToken.getSubject();

        if (!memberIdFromRefreshToken.equals(memberIdFromAccessToken)) {
            throw new BusinessException(GlobalErrorCode.BAD_REQUEST);
        }

        if(jwtUtil.checkBlacklist(extractAccessToken)){
            logout(accessToken);
            throw new BusinessException(GlobalErrorCode.BAD_REQUEST);
        }

        jwtUtil.validateAccessTokenExpiration(claimsAccessToken, extractAccessToken);

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
        Claims claimsAccessToken = jwtUtil.extractClaimsOrThrow("accessToken", extractAccessToken);

        // 액세스 토큰 블랙리스트 처리
        jwtUtil.addBlackListExistingAccessToken(extractAccessToken, claimsAccessToken.getExpiration());

        // 리프레시 토큰 삭제
        if(!refreshTokenRepository.findByMemberId(claimsAccessToken.getSubject()).isEmpty())
        {refreshTokenRepository.delete(claimsAccessToken.getSubject());}

    }

    // 테스트용 코드
    public ResponseEntity<ApiResponse<Object>> addBlackListExistingAccessTokenForTest(
        String accessToken){

        String extractAccessToken = jwtUtil.extractAccessToken(accessToken);
        Claims claimsAccessToken = jwtUtil.parseToken(extractAccessToken);
        jwtUtil.addBlackListExistingAccessToken(extractAccessToken, claimsAccessToken.getExpiration());
        return ResponseEntity.ok(ApiResponse.createSuccessWithNoData());
    }

    public ResponseEntity<ApiResponse<Object>> deleteBlackListExistingAccessTokenForTest(String accessToken){

        String extractAccessToken = jwtUtil.extractAccessToken(accessToken);
        redisTemplate.delete("blacklist:access-token:" + extractAccessToken);
        return ResponseEntity.ok(ApiResponse.createSuccessWithNoData());
    }

    public ResponseEntity<ApiResponse<Object>> createShortTimeAccessToken(Long memberId){
        String accessToken = jwtUtil.generateAccessTokenWithShortExpiration(memberId);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        return ResponseEntity.ok().headers(headers).body(ApiResponse.createSuccessWithNoData());
    }


}
