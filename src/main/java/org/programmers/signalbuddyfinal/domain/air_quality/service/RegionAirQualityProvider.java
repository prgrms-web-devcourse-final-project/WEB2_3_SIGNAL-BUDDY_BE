package org.programmers.signalbuddyfinal.domain.air_quality.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.programmers.signalbuddyfinal.domain.air_quality.dto.RegionAirQuality;
import org.programmers.signalbuddyfinal.domain.air_quality.exception.AirQualityErrorCode;
import org.programmers.signalbuddyfinal.global.exception.BusinessException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class RegionAirQualityProvider {

    @Qualifier("allAirQualityApiWebClient")
    private final WebClient webClient;
    @Value("${observatory.api-key}")
    private String apiKey;
    private final ObjectMapper objectMapper;
    private final XmlMapper xmlMapper = (XmlMapper) new XmlMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public Optional<RegionAirQuality> geAllAirQuality(String stationName) {

        String body = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/{apiKey}/json/1/1/{stationName}/DAILY/1.0")
                        .build(apiKey, stationName)
                ).accept(MediaType.ALL)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        RegionAirQuality response = parserAirQuality(body);
        if (response != null) {
            return Optional.of(response);
        }
        return Optional.empty();
    }

    private RegionAirQuality parserAirQuality(String body){
        try {
            JsonNode jsonRoot = objectMapper.readTree(body);
            if (jsonRoot.has("response")) {
                return objectMapper.readValue(body, RegionAirQuality.class);
            }
        }catch (Exception e) {
            try {
                XmlMapper xmlMapper = new XmlMapper();
                JsonNode rootNode = xmlMapper.readTree(body);

                String code = rootNode.at("/cmmMsgHeader/returnReasonCode").asText();
                String message = rootNode.at("/cmmMsgHeader/errMsg").asText();

                log.info("측정소별 미세먼지 API 응답 실패 - CODE: {}, MESSAGE: {}", code,
                        message);
                return null;
            } catch (Exception xmlEx) {
                throw new BusinessException(AirQualityErrorCode.ALL_AIR_QUALITY_DATA_NOT_READ);
            }
        }
        return null;
    }

}
