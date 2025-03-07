package org.programmers.signalbuddyfinal.domain.crossroad.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.programmers.signalbuddyfinal.domain.crossroad.dto.CrossroadApiResponse;
import org.programmers.signalbuddyfinal.domain.crossroad.dto.CrossroadStateApiResponse;
import org.programmers.signalbuddyfinal.domain.crossroad.exception.CrossroadErrorCode;
import org.programmers.signalbuddyfinal.global.exception.BusinessException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CrossroadProvider {

    @Value("${t-data-api.api-key}")
    private String apiKey;
    @Value("${t-data-api.crossroad-api}")
    private String crossroadApiUrl;
    @Value("${t-data-api.traffic-light-api}")
    private String signalStateUrl;

    @Qualifier("seoulApiWebClient")
    private final WebClient webClient;


    public List<CrossroadApiResponse> requestCrossroadApi(int page, int pageSize) {
        return webClient.get()
            .uri(crossroadApiUrl,
                uriBuilder -> uriBuilder
                    .queryParam("apiKey", apiKey)
                    .queryParam("pageNo", page)
                    .queryParam("numOfRows", pageSize)
                    .build())
            .retrieve()
            .bodyToMono(new ParameterizedTypeReference<List<CrossroadApiResponse>>() {})
            .onErrorMap(e -> {
                log.error("{}\n", e.getMessage(), e.getCause());
                throw new BusinessException(CrossroadErrorCode.CROSSROAD_API_REQUEST_FAILED);
            })
            .block();
    }

    public List<CrossroadStateApiResponse> requestCrossroadStateApi(String crossroadApiId) {
        return webClient.get()
            .uri(signalStateUrl,
                uriBuilder -> uriBuilder
                    .queryParam("apiKey", apiKey)
                    .queryParam("itstId", crossroadApiId)
                    .queryParam("pageNo", 1)
                    .queryParam("numOfRows", 1)
                    .build())
            .retrieve()
            .bodyToMono(new ParameterizedTypeReference<List<CrossroadStateApiResponse>>() {})
            .onErrorMap(e->{
                log.error("{}\n", e.getMessage(), e.getCause());
                throw new BusinessException(CrossroadErrorCode.CROSSROAD_API_REQUEST_FAILED);
            })
            .block();
    }
}
