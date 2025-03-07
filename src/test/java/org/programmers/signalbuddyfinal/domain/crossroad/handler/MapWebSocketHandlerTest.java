package org.programmers.signalbuddyfinal.domain.crossroad.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.programmers.signalbuddyfinal.domain.crossroad.dto.CrossroadResponse;
import org.programmers.signalbuddyfinal.domain.crossroad.dto.LocationRequest;
import org.programmers.signalbuddyfinal.domain.crossroad.service.CrossroadService;
import org.programmers.signalbuddyfinal.global.response.ApiResponse;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

@ExtendWith(MockitoExtension.class)
class MapWebSocketHandlerTest {

    private static final String INVALID_LOCATION = "요청 데이터 오류";
    private static final String SERVER_ERROR = "서버 내부 오류 발생";

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private CrossroadService crossroadService;

    @Mock
    private WebSocketSession session;

    @InjectMocks
    private MapWebSocketHandler webSocketHandler;

    private LocationRequest locationRequest;
    private List<CrossroadResponse> mockResponse;


    @BeforeEach
    void setUp() throws Exception {
        locationRequest = new LocationRequest(37.5665, 126.9780, 500);

        mockResponse = List.of(
            CrossroadResponse.builder().crossroadId(1L).crossroadApiId("API-123").name("테스트 교차로")
                .lat(37.5665).lng(126.9780).status("ACTIVE").build());

        // ObjectMapper의 writeValueAsString을 Mocking
        lenient().when(objectMapper.writeValueAsString(any())).thenAnswer(
            invocation -> new ObjectMapper().writeValueAsString(invocation.getArgument(0)));
    }

    @DisplayName("WebSocket 메시지를 처리하면 가장 가까운 교차로 목록 반환")
    @Test
    void handleTextMessageSuccess() throws Exception {
        final String payload = "{\"lat\":37.5665,\"lng\":126.9780,\"radius\":500}";
        final TextMessage message = new TextMessage(payload);

        when(objectMapper.readValue(payload, LocationRequest.class)).thenReturn(locationRequest);
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
        assertThat(responsePayload).isEqualTo(
            new ObjectMapper().writeValueAsString(ApiResponse.createSuccess(mockResponse)));

        verify(crossroadService, times(1)).findNearestCrossroad(37.5665, 126.9780, 500);
    }

    @DisplayName("잘못된 데이터 요청 시 WebSocket 메시지 처리 실패")
    @Test
    void handleTextInvalidRequest() throws Exception {
        final String invalidPayload = "{\"lat\":200,\"lng\":500,\"radius\":500}";
        final TextMessage message = new TextMessage(invalidPayload);
        final CompletableFuture<String> futureResponse = new CompletableFuture<>();

        when(objectMapper.readValue(invalidPayload, LocationRequest.class)).thenReturn(
            new LocationRequest(200.0, 500.0, 500));

        doAnswer(invocation -> {
            TextMessage sentMessage = invocation.getArgument(0, TextMessage.class);
            futureResponse.complete(sentMessage.getPayload());
            return null;
        }).when(session).sendMessage(any(TextMessage.class));

        webSocketHandler.handleTextMessage(session, message);

        final String responsePayload = futureResponse.get();
        assertThat(responsePayload).isEqualTo(
            new ObjectMapper().writeValueAsString(ApiResponse.createError(INVALID_LOCATION)));

        verify(session, times(1)).sendMessage(any(TextMessage.class));
        verify(session, times(1)).close(any());
        verify(crossroadService, never()).findNearestCrossroad(anyDouble(), anyDouble(), anyInt());
    }


    @DisplayName("WebSocket 메시지 처리 중 예외 발생 시 서버 오류 메시지 반환")
    @Test
    void handleTextServerError() throws Exception {
        final String payload = "{\"lat\":37.5665,\"lng\":126.9780,\"radius\":500}";
        final TextMessage message = new TextMessage(payload);
        final CompletableFuture<String> futureResponse = new CompletableFuture<>();

        when(objectMapper.readValue(payload, LocationRequest.class)).thenThrow(
            new RuntimeException("Parsing Error"));

        doAnswer(invocation -> {
            TextMessage sentMessage = invocation.getArgument(0, TextMessage.class);
            futureResponse.complete(sentMessage.getPayload());
            return null;
        }).when(session).sendMessage(any(TextMessage.class));

        webSocketHandler.handleTextMessage(session, message);

        final String responsePayload = futureResponse.get();
        assertThat(responsePayload).isEqualTo(
            new ObjectMapper().writeValueAsString(ApiResponse.createError(SERVER_ERROR)));

        verify(session, times(1)).sendMessage(any(TextMessage.class));
        verify(session, times(1)).close(any());
        verify(crossroadService, never()).findNearestCrossroad(anyDouble(), anyDouble(), anyInt());
    }
}