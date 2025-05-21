package org.programmers.signalbuddyfinal.domain.air_quality.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.programmers.signalbuddyfinal.domain.air_quality.dto.SeoulAirQuality;
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
public class SeoulAirQualityProvider {

    @Qualifier("airQualityApiWebClient")
    private final WebClient webClient;
    @Value("${air-quality.api-key}")
    private String apiKey;
    private final ObjectMapper objectMapper;
    private final XmlMapper xmlMapper = (XmlMapper) new XmlMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);


    public Optional<SeoulAirQuality> getAirQuality() {

            String body = webClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path("/{apiKey}/json/ListAvgOfSeoulAirQualityService/{start}/{end}")
                    .build(apiKey, 1, 1)
                )
                .accept(MediaType.ALL)
                .retrieve()
                .bodyToMono(String.class)
                .block();

            SeoulAirQuality seoulAirQuality = parseAirQuality(body);
        if(seoulAirQuality != null) {
            return Optional.of(seoulAirQuality);
        }
        return Optional.empty();
    }

    private SeoulAirQuality parseAirQuality(String body) {
        try {
            JsonNode jsonRoot = objectMapper.readTree(body);
            if (jsonRoot.has("ListAvgOfSeoulAirQualityService")) {
                JsonNode service = jsonRoot.get("ListAvgOfSeoulAirQualityService");
                return objectMapper.treeToValue(service, SeoulAirQuality.class);
            }
        } catch (Exception e) {
            try {
                JsonNode rootNode = xmlMapper.readTree(body);
                String code = rootNode.has("CODE") ?rootNode.get("CODE").asText() : "UNKNOWN";
                String message = rootNode.has("MESSAGE") ? rootNode.get("MESSAGE").asText() : "UNKNOWN";
                log.info("미세먼지 API 응답 실패 - CODE: {}, MESSAGE: {}", code,
                    message);
                return null;
            } catch (Exception xmlEx) {
                throw new BusinessException(AirQualityErrorCode.AIR_QUALITY_DATA_NOT_READ);
            }
        }
        return null;
    }
}
