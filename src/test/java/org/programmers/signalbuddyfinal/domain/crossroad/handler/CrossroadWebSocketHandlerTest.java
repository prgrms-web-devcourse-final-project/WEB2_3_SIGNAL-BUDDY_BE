package org.programmers.signalbuddyfinal.domain.crossroad.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.programmers.signalbuddyfinal.domain.crossroad.dto.CrossroadResponse;
import org.programmers.signalbuddyfinal.domain.crossroad.dto.LocationRequest;
import org.programmers.signalbuddyfinal.domain.crossroad.service.CrossroadService;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

@ExtendWith(MockitoExtension.class)
class CrossroadWebSocketHandlerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    @Mock
    private CrossroadService crossroadService;

    @Mock
    private WebSocketSession session;

    @InjectMocks
    private CrossroadWebSocketHandler webSocketHandler;

    @DisplayName("WebSocket 메시지를 처리하면 가장 가까운 교차로 목록 반환")
    @Test
    void handleTextMessageSuccess() throws Exception {
        final LocationRequest locationRequest = new LocationRequest(37.5665, 126.9780, 500);
        final String payload = objectMapper.writeValueAsString(locationRequest);
        final TextMessage message = new TextMessage(payload);

        final List<CrossroadResponse> mockResponse = List.of(
            CrossroadResponse.builder().crossroadId(1L).crossroadApiId("API-123").name("테스트 교차로")
                .lat(37.5665).lng(126.9780).status("ACTIVE").distance(100.0).build());

        when(crossroadService.findNearestCrossroad(37.5665, 126.9780, 500)).thenReturn(
            mockResponse);

        final CompletableFuture<String> futureResponse = new CompletableFuture<>();
        doAnswer(invocation -> {
            TextMessage sentMessage = invocation.getArgument(0, TextMessage.class);
            futureResponse.complete(sentMessage.getPayload());
            return null;
        }).when(session).sendMessage(any(TextMessage.class));

        webSocketHandler.handleTextMessage(session, message);

        final String responsePayload = futureResponse.get();
        assertThat(responsePayload).isEqualTo(objectMapper.writeValueAsString(mockResponse));

        verify(crossroadService, times(1)).findNearestCrossroad(37.5665, 126.9780, 500);
    }

    @DisplayName("WebSocket 메시지 처리 실패")
    @Test
    void handleTextMessageFail() throws Exception {
        final TextMessage message = new TextMessage("invalid json");

        final CompletableFuture<String> futureResponse = new CompletableFuture<>();
        doAnswer(invocation -> {
            TextMessage sentMessage = invocation.getArgument(0, TextMessage.class);
            futureResponse.complete(sentMessage.getPayload());  // 오류 메시지 저장
            return null;
        }).when(session).sendMessage(any(TextMessage.class));

        webSocketHandler.handleTextMessage(session, message);

        final String responsePayload = futureResponse.get();
        assertThat(responsePayload).isEqualTo("위치 요청 처리 중 오류 발생");

        verify(crossroadService, never()).findNearestCrossroad(anyDouble(), anyDouble(), anyInt());
    }
}