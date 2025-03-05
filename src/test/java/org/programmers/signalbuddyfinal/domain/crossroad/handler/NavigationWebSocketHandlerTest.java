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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.programmers.signalbuddyfinal.domain.crossroad.dto.CrossroadStateResponse;
import org.programmers.signalbuddyfinal.domain.crossroad.dto.NavigationRequest;
import org.programmers.signalbuddyfinal.domain.crossroad.service.CrossroadService;
import org.programmers.signalbuddyfinal.global.response.ApiResponse;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

@ExtendWith(MockitoExtension.class)
class NavigationWebSocketHandlerTest {

    private static final String NAVIGATION_COMPLETE_MESSAGE = "경로 안내가 완료되었습니다. 연결을 종료합니다.";
    private static final String SERVER_ERROR = "서버 내부 오류 발생";

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private CrossroadService crossroadService;

    @Mock
    private WebSocketSession session;

    @InjectMocks
    private NavigationWebSocketHandler webSocketHandler;


    private NavigationRequest navigationRequest;
    private List<CrossroadStateResponse> mockResponse;


    @BeforeEach
    void setUp() throws Exception {
        navigationRequest = new NavigationRequest(new String[]{"1", "2", "3"}, false);

        mockResponse = List.of(
            CrossroadStateResponse.builder().crossroadId(1L).eastTimeLeft(30).westTimeLeft(20)
                .build(),
            CrossroadStateResponse.builder().crossroadId(2L).northTimeLeft(15).southTimeLeft(25)
                .build(),
            CrossroadStateResponse.builder().crossroadId(3L).northTimeLeft(35).southTimeLeft(15)
                .build());

        lenient().when(objectMapper.writeValueAsString(any()))
            .thenAnswer(invocation -> new ObjectMapper().writeValueAsString(invocation.getArgument(0)));
    }


    @DisplayName("WebSocket 메시지를 처리하면 신호 상태 응답 반환")
    @Test
    void handleTextSuccess() throws Exception {
        final String payload = "{\"crossroadApiIds\":[\"1\",\"2\",\"3\"],\"isFinished\":false}";
        final TextMessage message = new TextMessage(payload);

        when(objectMapper.readValue(payload, NavigationRequest.class)).thenReturn(navigationRequest);
        when(crossroadService.getCrossroadIdsByApiIds(any())).thenReturn(List.of(1L, 2L, 3L));
        when(crossroadService.checkSignalState(any(Long.class))).thenReturn(mockResponse.get(0), mockResponse.get(1), mockResponse.get(2));

        final CompletableFuture<String> futureResponse = new CompletableFuture<>();
        doAnswer(invocation -> {
            TextMessage sentMessage = invocation.getArgument(0, TextMessage.class);
            futureResponse.complete(sentMessage.getPayload());
            return null;
        }).when(session).sendMessage(any(TextMessage.class));

        webSocketHandler.handleTextMessage(session, message);

        final String responsePayload = futureResponse.get();
        assertThat(responsePayload).isEqualTo(objectMapper.writeValueAsString(ApiResponse.createSuccess(mockResponse)));

        verify(crossroadService, times(1)).getCrossroadIdsByApiIds(any());
        verify(crossroadService, times(3)).checkSignalState(any(Long.class));
    }

    @DisplayName("경로 안내가 종료되면 WebSocket 연결 종료")
    @Test
    void handleTextFinished() throws Exception {
        final String payload = "{\"crossroadApiIds\":[\"1\",\"2\",\"3\"],\"isFinished\":true}";
        final TextMessage message = new TextMessage(payload);
        final NavigationRequest finishedRequest = new NavigationRequest(new String[]{"1", "2", "3"}, true);

        when(objectMapper.readValue(payload, NavigationRequest.class)).thenReturn(finishedRequest);

        final CompletableFuture<String> futureResponse = new CompletableFuture<>();
        doAnswer(invocation -> {
            TextMessage sentMessage = invocation.getArgument(0, TextMessage.class);
            futureResponse.complete(sentMessage.getPayload());
            return null;
        }).when(session).sendMessage(any(TextMessage.class));

        webSocketHandler.handleTextMessage(session, message);

        final String responsePayload = futureResponse.get();
        assertThat(responsePayload).isEqualTo(objectMapper.writeValueAsString(ApiResponse.createSuccess(NAVIGATION_COMPLETE_MESSAGE)));

        verify(session, times(1)).sendMessage(any(TextMessage.class));
        verify(session, times(1)).close(any());
    }

    @DisplayName("예외 발생 시 서버 오류 메시지 반환")
    @Test
    void handleTextServerError() throws Exception {
        final String payload = "{\"crossroadApiIds\":[\"1\",\"2\",\"3\"],\"isFinished\":false}";
        final TextMessage message = new TextMessage(payload);

        when(objectMapper.readValue(payload, NavigationRequest.class)).thenThrow(new RuntimeException("Parsing Error"));

        final CompletableFuture<String> futureResponse = new CompletableFuture<>();
        doAnswer(invocation -> {
            TextMessage sentMessage = invocation.getArgument(0, TextMessage.class);
            futureResponse.complete(sentMessage.getPayload());
            return null;
        }).when(session).sendMessage(any(TextMessage.class));

        webSocketHandler.handleTextMessage(session, message);

        final String responsePayload = futureResponse.get();
        assertThat(responsePayload).isEqualTo(objectMapper.writeValueAsString(ApiResponse.createError(SERVER_ERROR)));

        verify(session, times(1)).sendMessage(any(TextMessage.class));
        verify(session, times(1)).close(any());
        verify(crossroadService, never()).checkSignalState(any(Long.class));
    }
}