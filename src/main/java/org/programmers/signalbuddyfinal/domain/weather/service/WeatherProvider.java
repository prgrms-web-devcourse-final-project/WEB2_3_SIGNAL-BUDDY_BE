package org.programmers.signalbuddyfinal.domain.weather.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.programmers.signalbuddyfinal.domain.weather.dto.Weather;
import org.programmers.signalbuddyfinal.domain.weather.exception.WeatherErrorCode;
import org.programmers.signalbuddyfinal.global.exception.BusinessException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class WeatherProvider {

    private static final String NO_DATA = "NO_DATA";

    private final ObjectMapper objectMapper;
    @Qualifier("weatherApiWebClient")
    private final WebClient webClient;
    // 초단기 실황 조회
    @Value("${weather.ultra-srt-ncst}")
    private String ultraSrtNcst;
    @Value("${weather.api-key}")
    private String apiKey;

    public List<Weather> requestWeatherApi(int nx, int ny) {
        final String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        // 정시에 맞춰서 요청하면 아직 데이터가 존재하지 않아서 6분전 데이터 요청
        // 15:05 인데 15:00 데이터 존재하지 않음.
        final String currentTime = LocalTime.now().minusMinutes(6).format(DateTimeFormatter.ofPattern("HHmm"));
        final String responseJson = webClient.get()
            .uri(ultraSrtNcst,
                uriBuilder -> uriBuilder
                    .queryParam("serviceKey", apiKey)
                    .queryParam("pageNo", 1)
                    .queryParam("numOfRows", 1000)
                    .queryParam("dataType", "JSON")
                    .queryParam("base_date", currentDate)
                    .queryParam("base_time", currentTime)
                    .queryParam("nx", nx)
                    .queryParam("ny", ny)
                    .build()
            ).retrieve()
            .bodyToMono(String.class)
            .onErrorMap(e -> {
                log.error("{}\n", e.getMessage(), e.getCause());
                throw new BusinessException(WeatherErrorCode.WEATHER_API_REQUEST_FAILED);
            })
            .block();

        log.info("RESPONSE: {}", responseJson);

        if (responseJson != null && (responseJson.startsWith("<") || responseJson.contains(NO_DATA))) { // XML 형태의 응답이면 예외 발생
            log.error("Weather API service error: {}", responseJson);
            throw new BusinessException(WeatherErrorCode.WEATHER_API_SERVICE_ERROR);
        }

        return parseWeatherData(responseJson);
    }

    private List<Weather> parseWeatherData(String json) {
        try {
            final JsonNode rootNode = objectMapper.readTree(json);
            final JsonNode itemsNode = rootNode.path("response").path("body").path("items")
                .path("item");

            return objectMapper.readValue(itemsNode.toString(), new TypeReference<>() {
            });
        } catch (Exception e) {
            log.error("Weather API JSON Parsing Error: {}", e.getMessage(), e);
            throw new BusinessException(WeatherErrorCode.WEATHER_API_RESPONSE_ERROR);
        }
    }
}
