package org.programmers.signalbuddyfinal.domain.crossroad.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.programmers.signalbuddyfinal.domain.crossroad.dto.CrossroadStateResponse;
import org.programmers.signalbuddyfinal.domain.crossroad.dto.NavigationRequest;
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
public class NavigationWebSocketHandler extends TextWebSocketHandler {

    private static final String NAVIGATION_COMPLETE_MESSAGE = "경로 안내가 완료되었습니다. 연결을 종료합니다.";
    private static final String SERVER_ERROR = "서버 내부 오류 발생";
    private static final String NO_REASON = "No reason provided";

    private final CrossroadService crossroadService;
    private final ObjectMapper objectMapper;

    @Override
    protected void handleTextMessage(@NonNull WebSocketSession session,
        @NonNull TextMessage message) throws Exception {
        try {
            final NavigationRequest navigationRequest = objectMapper.readValue(message.getPayload(),
                NavigationRequest.class);

            log.info("Navigation Request: {}", navigationRequest);

            // 경로 안내가 종료되었을 때 연결 종료
            if (Boolean.TRUE.equals(navigationRequest.getIsFinished())) {
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(
                    ApiResponse.createSuccess(NAVIGATION_COMPLETE_MESSAGE))));
                session.close(
                    new CloseStatus(CloseStatus.NORMAL.getCode(), NAVIGATION_COMPLETE_MESSAGE));
                return;
            }

            final List<String> apiIds = Arrays.stream(navigationRequest.getCrossroadApiIds())
                .toList();
            final List<Long> crossroadIds = crossroadService.getCrossroadIdsByApiIds(apiIds);

            final List<CrossroadStateResponse> responses = crossroadIds.stream()
                .map(crossroadService::checkSignalState).toList();

            session.sendMessage(new TextMessage(
                objectMapper.writeValueAsString(ApiResponse.createSuccess(responses))));
        } catch (Exception e) {
            log.error("Navigation WebSocket Handler Error: {}", e.getMessage());
            session.sendMessage(new TextMessage(
                objectMapper.writeValueAsString(ApiResponse.createError(SERVER_ERROR))));
            session.close(new CloseStatus(CloseStatus.SERVER_ERROR.getCode(), SERVER_ERROR));
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("Navigation WebSocket Connected : {}", session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        log.info("Navigation WebSocket Disconnected: {}\nCode: {}\nReason: {}", session.getId(),
            status.getCode(), (status.getReason() != null ? status.getReason() : NO_REASON));
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("WebSocket Transport Error: {}", exception.getMessage());
        try {
            session.close(new CloseStatus(CloseStatus.SERVER_ERROR.getCode(), SERVER_ERROR));
        } catch (IOException e) {
            log.error("Error closing WebSocket session", e);
        }
    }
}
