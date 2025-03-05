package org.programmers.signalbuddyfinal.domain.crossroad.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.programmers.signalbuddyfinal.domain.crossroad.dto.CrossroadResponse;
import org.programmers.signalbuddyfinal.domain.crossroad.dto.LocationRequest;
import org.programmers.signalbuddyfinal.domain.crossroad.service.CrossroadService;
import org.programmers.signalbuddyfinal.global.response.ApiResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
@RequiredArgsConstructor
@Slf4j
public class MapWebSocketHandler extends TextWebSocketHandler {

    private static final String INVALID_LOCATION = "요청 데이터 오류";
    private static final String SERVER_ERROR = "서버 내부 오류 발생";
    private static final String NO_REASON = "No reason provided";

    private final CrossroadService crossroadService;

    private final ObjectMapper objectMapper;

    @Override
    protected void handleTextMessage(@NonNull WebSocketSession session,
        @NonNull TextMessage message) throws Exception {
        try {
            final LocationRequest locationRequest = objectMapper.readValue(message.getPayload(),
                LocationRequest.class);

            log.info("Location Request: {}", locationRequest);

            if (!isValidLocationRequest(locationRequest)) {
                log.warn("Invalid Location Request: {}", locationRequest);
                session.sendMessage(new TextMessage(
                    objectMapper.writeValueAsString(ApiResponse.createError(INVALID_LOCATION))));
                session.close(new CloseStatus(CloseStatus.BAD_DATA.getCode(), INVALID_LOCATION));
                return;
            }

            final List<CrossroadResponse> nearestCrossroads = crossroadService.findNearestCrossroad(
                locationRequest.getLat(), locationRequest.getLng(), locationRequest.getRadius());

            session.sendMessage(new TextMessage(
                objectMapper.writeValueAsString(ApiResponse.createSuccess(nearestCrossroads))));

        } catch (Exception e) {
            log.error("Map WebSocket Handler Error: {}, \nRequest: {}", e.getMessage(),
                message.getPayload());
            session.sendMessage(new TextMessage(
                objectMapper.writeValueAsString(ApiResponse.createError(SERVER_ERROR))));
            session.close(new CloseStatus(CloseStatus.SERVER_ERROR.getCode(), SERVER_ERROR));
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("Map WebSocket Connected : {}", session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        log.info("Map WebSocket Disconnected: {}\nCode: {}\nReason: {}", session.getId(),
            status.getCode(), (status.getReason() != null ? status.getReason() : NO_REASON));
    }


    /**
     * 요청 데이터 검증 메서드
     */
    private boolean isValidLocationRequest(LocationRequest request) {
        return request.getLat() != null && request.getLng() != null && request.getRadius() != null
               && request.getLat() >= -90 && request.getLat() <= 90 && request.getLng() >= -180
               && request.getLng() <= 180;
    }
}
