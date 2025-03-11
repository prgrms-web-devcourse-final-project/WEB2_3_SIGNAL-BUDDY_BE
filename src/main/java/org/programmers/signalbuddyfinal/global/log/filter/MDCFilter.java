package org.programmers.signalbuddyfinal.global.log.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Component
public class MDCFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;

        // 고유한 요청 식별자(UUID)를 생성
        // 요청 식별자를 남기는 이유 : 로그 추적성을 높이기 위함.
        final String requestId = UUID.randomUUID().toString();

        try {
            String ipAddress = request.getRemoteAddr();

            // IPv6 localhost 체크 및 변환
            if ("0:0:0:0:0:0:0:1".equals(ipAddress) || "::1".equals(ipAddress)) {
                ipAddress = "127.0.0.1";
            }
            MDC.put("requestId", requestId);
            MDC.put("ipAddress", ipAddress);
            MDC.put("method", httpRequest.getMethod());
            MDC.put("uri", httpRequest.getRequestURI());

            chain.doFilter(request, response);
        } finally {
            MDC.clear();  // 반드시 clear 처리로 메모리 누수 방지
        }
    }
}