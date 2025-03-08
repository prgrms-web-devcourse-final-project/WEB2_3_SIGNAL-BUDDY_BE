package org.programmers.signalbuddyfinal.domain.crossroad.service;

import static net.bytebuddy.matcher.ElementMatchers.is;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.stubbing.OngoingStubbing;
import org.programmers.signalbuddyfinal.domain.crossroad.dto.CrossroadApiResponse;
import org.programmers.signalbuddyfinal.domain.crossroad.dto.CrossroadResponse;
import org.programmers.signalbuddyfinal.domain.crossroad.dto.CrossroadStateApiResponse;
import org.programmers.signalbuddyfinal.domain.crossroad.dto.CrossroadStateResponse;
import org.programmers.signalbuddyfinal.domain.crossroad.dto.SignalState;
import org.programmers.signalbuddyfinal.domain.crossroad.entity.Crossroad;
import org.programmers.signalbuddyfinal.domain.crossroad.repository.CrossroadRedisRepository;
import org.programmers.signalbuddyfinal.domain.crossroad.repository.CrossroadRepository;
import org.programmers.signalbuddyfinal.global.db.RedisTestContainer;
import org.programmers.signalbuddyfinal.global.monitoring.HttpRequestManager;
import org.programmers.signalbuddyfinal.global.support.ServiceTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.RestTemplate;

class CrossroadServiceTest extends ServiceTest implements RedisTestContainer {

    @Autowired
    private CrossroadService crossroadService;


    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;

    @Autowired
    private CrossroadRepository crossroadRepository;

    @MockitoBean
    private CrossroadProvider crossroadProvider;

    @MockitoBean
    private HttpRequestManager httpRequestManager;

    private Crossroad crossroad;

    @BeforeEach
    void setUp() {
        crossroad = saveCrossroad("13214", "00사거리", 37.12222, 127.12132);
    }

    @DisplayName("신호등 잔여시간 정보를 Redis에 캐싱한 뒤 반환한다.")
    @Test
    void checkSignalState() {
        // Given
        Long crossroadId = crossroad.getCrossroadId();
        String crossroadApiId = crossroad.getCrossroadApiId();
        CrossroadStateApiResponse apiResponse = CrossroadStateApiResponse.builder()
            .crossroadApiId(crossroadApiId).transTimestamp(1741054218628L)
            .northTimeLeft(349).northState(SignalState.RED)
            .eastTimeLeft(9).eastState(SignalState.YELLOW)
            .southTimeLeft(349).eastState(SignalState.RED)
            .build();

        given(crossroadProvider.requestCrossroadStateApi(crossroadApiId))
            .willReturn(List.of(apiResponse));

        // When
        CrossroadStateResponse actual = crossroadService.checkSignalState(crossroadId);

        // Then
        CrossroadStateResponse cache = (CrossroadStateResponse) redisTemplate.opsForValue()
            .get("crossroad-state:" + crossroadId);
        SoftAssertions.assertSoftly(softAssertions -> {
           softAssertions.assertThat(actual).isNotNull();
           softAssertions.assertThat(actual.getCrossroadId()).isEqualTo(crossroadId);
           softAssertions.assertThat(actual.getTransTimestamp()).isEqualTo(1741054218628L);
           softAssertions.assertThat(actual.getNorthState()).isEqualTo(SignalState.RED);
           softAssertions.assertThat(actual.getEastTimeLeft()).isEqualTo(9);
           softAssertions.assertThat(cache).isNotNull();
           softAssertions.assertThat(actual.getSouthTimeLeft()).isEqualTo(cache.getSouthTimeLeft());
        });
    }

    @DisplayName("캐싱된 신호등 잔여시간 정보를 바로 반환한다.")
    @Test
    void checkSignalState_Cache() {
        // Given
        Long crossroadId = crossroad.getCrossroadId();
        String crossroadApiId = crossroad.getCrossroadApiId();
        CrossroadStateApiResponse apiResponse = CrossroadStateApiResponse.builder()
            .crossroadApiId(crossroadApiId).transTimestamp(1741054218628L)
            .northTimeLeft(349).northState(SignalState.RED)
            .eastTimeLeft(9).eastState(SignalState.YELLOW)
            .southTimeLeft(349).eastState(SignalState.RED)
            .build();

        given(crossroadProvider.requestCrossroadStateApi(crossroadApiId))
            .willReturn(List.of(apiResponse));

        // When
        CrossroadStateResponse actual = crossroadService.checkSignalState(crossroadId);
        CrossroadStateResponse cache = crossroadService.checkSignalState(crossroadId);

        // Then
        assertThat(actual).isEqualTo(cache);
    }

    private Crossroad saveCrossroad(String apiId, String name, double lat, double lng) {
        return crossroadRepository.save(new Crossroad(
            CrossroadApiResponse.builder().crossroadApiId(apiId).name(name).lat(lat).lng(lng)
                .build()));
    }

}