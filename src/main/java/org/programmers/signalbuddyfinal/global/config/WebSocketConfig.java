package org.programmers.signalbuddyfinal.global.config;

import lombok.RequiredArgsConstructor;
import org.programmers.signalbuddyfinal.domain.crossroad.handler.MapWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final MapWebSocketHandler mapWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(mapWebSocketHandler, "/ws/location").setAllowedOrigins("*");
    }
}
