package org.programmers.signalbuddyfinal.domain.weather.service;

import java.io.IOException;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.programmers.signalbuddyfinal.domain.weather.dto.GridResponse;
import org.programmers.signalbuddyfinal.domain.weather.dto.Weather;
import org.programmers.signalbuddyfinal.domain.weather.dto.WeatherResponse;
import org.programmers.signalbuddyfinal.domain.weather.repository.GridCoordinateRepository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@ExtendWith(MockitoExtension.class)
class WeatherServiceMockitoTest {

    @Mock
    private GridCoordinateRepository gridCoordinateRepository;

    @Mock
    private WeatherProvider weatherProvider;

    @InjectMocks
    private WeatherService weatherService;

    @Test
    void testGetWeatherData() {
        final Weather weather1 = Weather.builder()
            .category(Weather.T1H)
            .obsrValue("25.0")
            .build();

        final Weather weather2 = Weather.builder()
            .category(Weather.PTY)
            .obsrValue("0")
            .build();

        final Weather weather3 = Weather.builder()
            .category(Weather.REH)
            .obsrValue("80.0")
            .build();

        final Weather weather4 = Weather.builder()
            .category(Weather.RN1)
            .obsrValue("0.0")
            .build();

        final Weather weather5 = Weather.builder()
            .category(Weather.VEC)
            .obsrValue("45.0")
            .build();

        final Weather weather6 = Weather.builder()
            .category(Weather.WSD)
            .obsrValue("3.5")
            .build();

        when(weatherProvider.requestWeatherApi(anyInt(), anyInt())).thenReturn(
            List.of(weather1, weather2, weather3, weather4, weather5, weather6));

        // when: getWeatherData 호출
        final WeatherResponse response = weatherService.getWeatherData(55, 127);

        // then: 반환된 WeatherResponse의 값 확인
        assertNotNull(response);
        assertEquals(25.0, response.getTemperature());
        assertEquals("0", response.getPrecipitationType());
        assertEquals(80.0, response.getHumidity());
        assertEquals(0.0, response.getPrecipitation());
        // windDirection 계산: floor((45.0 + (22.5 * 0.5)) / 22.5) = floor(56.25 / 22.5) = 2
        assertEquals(2, response.getWindDirection());
        assertEquals(3.5, response.getWindSpeed());
    }

    @Test
    void testSubscribeToWeather() {
        // given: gridCoordinateRepository가 지정한 위치 근처의 그리드 정보를 반환하도록 설정
        final GridResponse gridResponse = GridResponse.builder().gridX(36).gridY(127).build();
        when(gridCoordinateRepository.findByLatAndLngWithRadius(anyDouble(), anyDouble(),
            anyDouble())).thenReturn(gridResponse);

        // when: 특정 위도, 경도로 SSE 구독 요청
        final SseEmitter emitter = weatherService.subscribeToWeather(37.5665, 126.9780);

        // then: SseEmitter가 정상적으로 생성되었는지 검증 및 gridCoordinateRepository의 호출 확인
        assertNotNull(emitter);
        verify(gridCoordinateRepository).findByLatAndLngWithRadius(eq(37.5665), eq(126.9780),
            eq(WeatherService.RADIUS));
    }

    @Test
    void testSendWeatherUpdates() throws IOException {
        // given: 구독된 SSE 연결이 존재하도록 설정
        final GridResponse gridResponse = GridResponse.builder().gridX(36).gridY(127).build();
        when(gridCoordinateRepository.findByLatAndLngWithRadius(anyDouble(), anyDouble(),
            anyDouble())).thenReturn(gridResponse);

        Weather weather = Weather.builder().category(Weather.T1H).obsrValue("20.0").build();
        when(weatherProvider.requestWeatherApi(anyInt(), anyInt())).thenReturn(List.of(weather));

        // SSE 구독 생성
        final SseEmitter emitter = weatherService.subscribeToWeather(37.5665, 126.9780);
        assertNotNull(emitter);

        // when: 스케줄러 메서드인 sendWeatherUpdates()를 호출
        assertDoesNotThrow(() -> weatherService.sendWeatherUpdates());
    }
}