package org.programmers.signalbuddyfinal.domain.crossroad.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.programmers.signalbuddyfinal.domain.crossroad.dto.CrossroadResponse;
import org.programmers.signalbuddyfinal.domain.crossroad.dto.LocationRequest;
import org.programmers.signalbuddyfinal.domain.crossroad.service.CrossroadService;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
@RequiredArgsConstructor
@Slf4j
public class CrossroadWebSocketHandler extends TextWebSocketHandler {

    private final CrossroadService crossroadService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void handleTextMessage(@NonNull WebSocketSession session,
        @NonNull TextMessage message) throws Exception {
        try {

            final LocationRequest locationRequest = objectMapper.readValue(message.getPayload(),
                LocationRequest.class);

            log.info("Location Request: {}", locationRequest);

            final List<CrossroadResponse> nearestCrossroads = crossroadService.findNearestCrossroad(
                locationRequest.getLat(), locationRequest.getLng(), locationRequest.getRadius());

            final String response = objectMapper.writeValueAsString(nearestCrossroads);

            session.sendMessage(new TextMessage(response));
        } catch (Exception e) {
            session.sendMessage(new TextMessage("위치 요청 처리 중 오류 발생"));
            log.error("Crossroad WebSocket Handler Error: {}, \nRequest: {}", e.getMessage(), message.getPayload());
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("WebSocket Connected : {}", session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        log.info("WebSocket Disconnected : {}", session.getId());
    }
}
