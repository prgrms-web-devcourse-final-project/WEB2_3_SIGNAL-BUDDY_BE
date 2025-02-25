package org.programmers.signalbuddyfinal.global.security.exception;

import com.nimbusds.jwt.JWT;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.programmers.signalbuddyfinal.domain.auth.exception.AuthErrorCode;
import org.programmers.signalbuddyfinal.domain.member.exception.MemberErrorCode;
import org.programmers.signalbuddyfinal.global.exception.ErrorCode;
import org.programmers.signalbuddyfinal.global.security.jwt.TokenErrorCode;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
        AuthenticationException authException) throws IOException, ServletException {

        String exceptionMessage = (String) request.getAttribute("exception");
        String errorCode = "";
        String errorMessage = "";

        if("EXPIRED_ACCESS_TOKEN".equals(exceptionMessage)){
            errorCode = TokenErrorCode.EXPIRED_ACCESS_TOKEN.getCode();
            errorMessage = TokenErrorCode.EXPIRED_ACCESS_TOKEN.getMessage();
        }else if("EXPIRED_REFRESH_TOKEN".equals(exceptionMessage)){
            errorCode = TokenErrorCode.EXPIRED_REFRESH_TOKEN.getCode();
            errorMessage = TokenErrorCode.EXPIRED_REFRESH_TOKEN.getMessage();
        }else if("INVALID_TOKEN".equals(exceptionMessage)){
            errorCode = TokenErrorCode.INVALID_TOKEN.getCode();
            errorMessage = TokenErrorCode.INVALID_TOKEN.getMessage();
        }else if("ACCESS_TOKEN_NOT_EXIST".equals(exceptionMessage)){
            errorCode = TokenErrorCode.ACCESS_TOKEN_NOT_EXIST.getCode();
            errorMessage = TokenErrorCode.ACCESS_TOKEN_NOT_EXIST.getMessage();
        }else if("REFRESH_TOKEN_EXPIRED".equals(exceptionMessage)){
            errorCode = TokenErrorCode.REFRESH_TOKEN_NOT_EXIST.getCode();
            errorMessage = TokenErrorCode.REFRESH_TOKEN_NOT_EXIST.getMessage();
        }

        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write("{\"code\": "+errorCode+",\"message\": \""+errorMessage+"\"}");
    }
}
