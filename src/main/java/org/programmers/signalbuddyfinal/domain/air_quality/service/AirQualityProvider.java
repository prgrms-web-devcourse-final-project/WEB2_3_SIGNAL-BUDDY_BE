package org.programmers.signalbuddyfinal.domain.air_quality.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.sl.draw.geom.GuideIf.Op;
import org.programmers.signalbuddyfinal.domain.air_quality.dto.AirQuality;
import org.programmers.signalbuddyfinal.domain.air_quality.dto.AirQualityItems;
import org.programmers.signalbuddyfinal.domain.air_quality.dto.Result;
import org.programmers.signalbuddyfinal.domain.air_quality.exception.AirQualityErrorCode;
import org.programmers.signalbuddyfinal.global.exception.BusinessException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class AirQualityProvider {

    @Qualifier("airQualityApiWebClient")
    private final WebClient webClient;
    @Value("${air-quality.api-key}")
    private String apiKey;
    private final ObjectMapper objectMapper;
    private final XmlMapper xmlMapper = (XmlMapper) new XmlMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);


    public Optional<AirQuality> getAirQuality() {

            String body = webClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path("/{apiKey}/json/ListAvgOfSeoulAirQualityService/{start}/{end}")
                    .build(apiKey, 1, 1)
                )
                .accept(MediaType.ALL)
                .retrieve()
                .bodyToMono(String.class)
                .block();

            AirQuality airQuality = parseAirQuality(body);
        if(airQuality != null) {
            return Optional.of(airQuality);
        }
        return Optional.empty();
    }

    private AirQuality parseAirQuality(String body) {
        try {
            JsonNode jsonRoot = objectMapper.readTree(body);
            if (jsonRoot.has("ListAvgOfSeoulAirQualityService")) {
                JsonNode service = jsonRoot.get("ListAvgOfSeoulAirQualityService");
                return objectMapper.treeToValue(service, AirQuality.class);
            }
        } catch (Exception e) {
            try {
                Result error = xmlMapper.readValue(body, Result.class);
                log.info("미세먼지 API 응답 실패 - CODE: {}, MESSAGE: {}", error.getCode(),
                    error.getMessage());
                return null;
            } catch (Exception xmlEx) {
                throw new BusinessException(AirQualityErrorCode.AIR_QUALITY_DATA_NOT_READ);
            }
        }
        return null;
    }
}
