package org.programmers.signalbuddyfinal.domain.air_quality.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.programmers.signalbuddyfinal.domain.air_quality.dto.ObservatoryResponse;
import org.programmers.signalbuddyfinal.domain.air_quality.exception.AirQualityErrorCode;
import org.programmers.signalbuddyfinal.global.exception.BusinessException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.StringTokenizer;

@Slf4j
@Component
@RequiredArgsConstructor
public class ObservatoryProvider {

  @Qualifier("observatoryApiWebClient")
  private final WebClient webClient;
  @Value("${observatory.api-key}")
  private String apiKey;
  private final ObjectMapper objectMapper;
  private final XmlMapper xmlMapper = (XmlMapper) new XmlMapper()
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);


  public Optional<ObservatoryResponse> getObservatory(double lat, double lng) {

    String body = webClient.get()
        .uri(uriBuilder -> uriBuilder
            .path("/{apiKey}/json/{lat}/{lng}/1.1")
            .build(apiKey, lat, lng)
        ).accept(MediaType.ALL)
        .retrieve()
        .bodyToMono(String.class)
        .block();

    ObservatoryResponse response = parserObservatory(body);
    if (response != null) {
      return Optional.of(response);
    }
    return Optional.empty();
  }

  private ObservatoryResponse parserObservatory(String body) {
    try {
      JsonNode jsonRoot = objectMapper.readTree(body);
      if (jsonRoot.has("response")) {
        JsonNode itemsNode = jsonRoot.get("response").get("body").get("items");
        String fullAddr = itemsNode.get(0).get("addr").asText();
        StringTokenizer st = new StringTokenizer(fullAddr, " ");
        String addr = st.nextToken();

        return ObservatoryResponse.builder()
            .addr(addr)
            .stationName(itemsNode.get(0).get("stationName").asText())
            .stationCode(itemsNode.get(0).get("stationCode").asText())
            .build();
      }
    } catch (Exception e) {
      try {
        JsonNode rootNode = xmlMapper.readTree(body);

        String code = rootNode.at("/cmmMsgHeader/returnReasonCode").asText();
        String message = rootNode.at("/cmmMsgHeader/errMsg").asText();

        log.info("측정소 API 응답 실패 - CODE: {}, MESSAGE: {}", code,
            message);
        return null;
      } catch (Exception xmlEx) {
        throw new BusinessException(AirQualityErrorCode.OBSERVATORY_DATA_NOT_READ);
      }
    }
    return null;
  }
}
