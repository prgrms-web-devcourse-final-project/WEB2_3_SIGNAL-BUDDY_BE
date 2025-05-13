package org.programmers.signalbuddyfinal.domain.air_quality.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.List;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.programmers.signalbuddyfinal.domain.air_quality.dto.AirQualityResponse;
import org.programmers.signalbuddyfinal.domain.air_quality.dto.RegionAirQuality;
import org.programmers.signalbuddyfinal.domain.air_quality.dto.CachedAirQuality;
import org.programmers.signalbuddyfinal.domain.air_quality.dto.ObservatoryResponse;
import org.programmers.signalbuddyfinal.domain.air_quality.dto.SeoulAirQuality;
import org.programmers.signalbuddyfinal.global.config.RedisConfig;
import org.programmers.signalbuddyfinal.global.db.RedisTestContainer;
import org.programmers.signalbuddyfinal.global.exception.BusinessException;
import org.programmers.signalbuddyfinal.global.support.ServiceTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;

import java.io.IOException;
import java.util.Optional;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@Import(RedisConfig.class)
@TestPropertySource(properties = {
    "schedule.air-quality-api.cron=0 0 0/1 * * ?",
    "schedule.air-quality-api.lockAtMostFor=10m",
    "schedule.air-quality-api.lockAtLeastFor=50m"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class AirQualityServiceTest extends ServiceTest implements RedisTestContainer {

    @Autowired
    private AirQualityService airQualityService;

    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;

    @MockitoBean
    private static ObservatoryProvider observatoryProvider;

    @MockitoBean
    private RegionAirQualityProvider regionAirQualityProvider;

    @MockitoBean
    private SeoulAirQualityProvider seoulAirQualityProvider;

    private static MockWebServer mockWebServer;

    private final String key = "air-quality:";

    @BeforeAll
    static void startMockServer() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @BeforeEach
    void setup() {
        flushRedis();
    }

    @AfterEach
    void shutDown() throws IOException {
        mockWebServer.shutdown();
    }

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("air-quality.base-url", () -> "http://localhost:" + mockWebServer.getPort());
    }

    @DisplayName("서울 첫 요청 성공 시 응답 반환 및 캐싱 테스트")
    @Test
    void successRequestTest() {
        createMockWebServer(createSeoulApiResponse());

        when(observatoryProvider.getObservatory(anyDouble(), anyDouble()))
            .thenReturn(createObservatoryResponse("서울역", "서울 XXX OOO"));
        when(seoulAirQualityProvider.getAirQuality())
            .thenReturn(Optional.of(createSeoulAirQuality()));

        AirQualityResponse response = airQualityService.getAirQuality(127.4170933, 36.35218384);
        CachedAirQuality cache = getCache("seoul");

        assertThat(response.getGrade()).isEqualTo("보통");
        assertThat(cache).isNotNull();
        assertThat(cache.getData().getGrade()).isEqualTo("보통");
        assertThat(cache.isFresh()).isTrue();
    }

    @DisplayName("서울 두 번째 요청 시 캐싱된 데이터 반환")
    @Test
    void successSecondRequestTest() {
        int count = mockWebServer.getRequestCount();
        createMockWebServer(createSeoulApiResponse());
        setCache("seoul", true);
        when(observatoryProvider.getObservatory(anyDouble(), anyDouble()))
            .thenReturn(createObservatoryResponse("서울역", "서울 XXX OOO"));

        AirQualityResponse response = airQualityService.getAirQuality(127.4170933, 36.35218384);

        assertThat(response).isNotNull();
        assertThat(response.getGrade()).isEqualTo("보통");
        assertThat(response.getPm10()).isEqualTo("61");
        assertThat(response.getPm25()).isEqualTo("20");
        assertThat(mockWebServer.getRequestCount()).isEqualTo(count);
    }

    @DisplayName("서울 failBack 실패 테스트")
    @Test
    void failBackRequestTest() {
        createMockWebServer(createFailSeoulApiResponse());
        when(observatoryProvider.getObservatory(anyDouble(), anyDouble()))
            .thenReturn(createObservatoryResponse("서울역", "서울 XXX OOO"));

        assertThrows(BusinessException.class,
            () -> airQualityService.getAirQuality(127.4170933, 36.35218384));
    }

    @DisplayName("서울 failBack 성공 테스트")
    @Test
    void failBackSuccessTest() {
        createMockWebServer(createFailSeoulApiResponse());
        setCache("seoul", true);
        CachedAirQuality before = getCache("seoul");
        when(observatoryProvider.getObservatory(anyDouble(), anyDouble()))
            .thenReturn(createObservatoryResponse("서울역", "서울 XXX OOO"));

        airQualityService.updateAriQuality();
        CachedAirQuality after = getCache("seoul");

        assertThat(after.isFresh()).isFalse();
        assertThat(after.getData()).usingRecursiveComparison().isEqualTo(before.getData());

    }

    @Test
    @DisplayName("서울 외 지역 응답 성공 테스트")
    void successRegionRequestTest() {
        createMockWebServer(createFailRegionApiResponse());
        when(observatoryProvider.getObservatory(anyDouble(), anyDouble()))
            .thenReturn(createObservatoryResponse("설성면", "경기 이천시 설성면"));
        when(regionAirQualityProvider.geAllAirQuality(anyString()))
            .thenReturn(Optional.of(createRegionAirQuality("13", "23")));

        AirQualityResponse response = airQualityService.getAirQuality(127.0421, 37.5716);

        assertThat(response).isNotNull();
        assertThat(response.getGrade()).isEqualTo("보통");
        assertThat(response.getPm10()).isEqualTo("23");
        assertThat(response.getPm25()).isEqualTo("13");
    }

    @DisplayName("서울외 지역 두 번째 요청 시 캐싱된 데이터 반환")
    @Test
    void successSecondRegionRequestTest() {
        int count = mockWebServer.getRequestCount();
        createMockWebServer(createRegionApiResponse());
        setCache("111123", true);
        when(observatoryProvider.getObservatory(anyDouble(), anyDouble()))
            .thenReturn(createObservatoryResponse("설성면", "경기 이천시 설성면"));

        AirQualityResponse response = airQualityService.getAirQuality(127.4170933, 36.35218384);

        assertThat(response).isNotNull();
        assertThat(response.getGrade()).isEqualTo("보통");
        assertThat(response.getPm10()).isEqualTo("61");
        assertThat(response.getPm25()).isEqualTo("20");
        assertThat(mockWebServer.getRequestCount()).isEqualTo(count);
    }

    @DisplayName("서울외 지역 failBack 실패 테스트")
    @Test
    void RegionFailBackRequestTest() {
        createMockWebServer(createFailRegionApiResponse());
        when(observatoryProvider.getObservatory(anyDouble(), anyDouble()))
            .thenReturn(createObservatoryResponse("설성면", "경기 이천시 설성면"));

        assertThrows(BusinessException.class,
            () -> airQualityService.getAirQuality(127.4170933, 36.35218384));
    }

    @DisplayName("서울외 지역 failBack 성공 테스트")
    @Test
    void RegionFailBackSuccessTest() {
        createMockWebServer(createRegionApiResponse());
        setCache("111123", true);
        CachedAirQuality before = getCache("111123");
        when(observatoryProvider.getObservatory(anyDouble(), anyDouble()))
            .thenReturn(createObservatoryResponse("설성면", "경기 이천시 설성면"));

        airQualityService.updateRegionAriQuality(
            createObservatoryResponse("설성면", "경기 이천시 설성면").get());
        CachedAirQuality after = getCache("111123");

        assertThat(after.isFresh()).isFalse();
        assertThat(after.getData()).usingRecursiveComparison().isEqualTo(before.getData());

    }

    @DisplayName("Observatory null 입력시 예외 발생 테스트")
    @Test
    void observatoryNullTest() {
        when(observatoryProvider.getObservatory(anyDouble(), anyDouble()))
            .thenReturn(Optional.empty());

        assertThrows(BusinessException.class,
            () -> airQualityService.getAirQuality(127.4170933, 36.35218384));
    }

    private void flushRedis() {
        RedisConnectionFactory factory = redisTemplate.getConnectionFactory();
        if (factory != null) {
            factory.getConnection().serverCommands().flushAll();
        }
    }

    private void createMockWebServer(String response) {
        mockWebServer.enqueue(new MockResponse()
            .setBody(response)
            .addHeader("Content-Type", "application/json")
            .setResponseCode(200));
    }

    private String createSeoulApiResponse() {
        return """
            {
              "ListAvgOfSeoulAirQualityService": {
                "list_total_count": 1,
                "RESULT": {
                  "CODE": "INFO-000",
                  "MESSAGE": "정상 처리되었습니다"
                },
                "row": [
                  {
                    "GRADE": "보통",
                    "IDEX_MVL": "55",
                    "POLLUTANT": "O3",
                    "NITROGEN": 0.017,
                    "OZONE": 0.036,
                    "CARBON": 0.4,
                    "SULFUROUS": 0.003,
                    "PM10": 23,
                    "PM25": 12
                  }
                ]
              }
            }
            """;
    }


    private String createFailSeoulApiResponse() {
        return """
                  <RESULT>
                  <script/>
                  <script/>
                  <CODE>ERROR-300</CODE>
                  <MESSAGE>
                  <![CDATA[ 필수 값이 누락되어 있습니다. 요청인자를 참고 하십시오. ]]>
                  </MESSAGE>
                  </RESULT>
            """;
    }

    private String createRegionApiResponse() {
        return """
                "response": {
              "body": {
                "totalCount": 22,
                    "items": [
                {
                  "so2Grade": "1",
                    "coFlag": null,
                    "khaiValue": "46",
                    "so2Value": "0.002",
                    "coValue": "0.3",
                    "pm25Flag": null,
                    "pm10Flag": null,
                    "pm10Value": "4",
                    "o3Grade": "1",
                    "khaiGrade": "1",
                    "pm25Value": "2",
                    "no2Flag": null,
                    "no2Grade": "1",
                    "o3Flag": null,
                    "pm25Grade": "1",
                    "so2Flag": null,
                    "dataTime": "2025-05-09 24:00",
                    "coGrade": "1",
                    "no2Value": "0.016",
                    "pm10Grade": "1",
                    "o3Value": "0.028"
                }
                        ],
                "pageNo": 1,
                    "numOfRows": 1
              },
              "header": {
                "resultMsg": "NORMAL_CODE",
                    "resultCode": "00"
              }
            }
            """;
    }

    private String createFailRegionApiResponse() {
        return """
                  <OpenAPI_ServiceResponse>
                  <script/>
                  <script/>
                  <cmmMsgHeader>
            <errMsg>SERVICE ERROR</errMsg>
                  <returnAuthMsg>SERVICE_KEY_IS_NOT_REGISTERED_ERROR</returnAuthMsg>
                  <returnReasonCode>30</returnReasonCode>
                  </cmmMsgHeader>
                  </OpenAPI_ServiceResponse>
            """;
    }

    private SeoulAirQuality createSeoulAirQuality() {
        SeoulAirQuality.Item item = SeoulAirQuality.Item.builder()
            .grade("보통")
            .mvl("55")
            .pollutant("O3")
            .nitrogen("0.017")
            .ozone("0.036")
            .carbon("0.4")
            .sulfurous("0.003")
            .pm10("23")
            .pm25("12")
            .build();

        SeoulAirQuality.Result result = SeoulAirQuality.Result.builder()
            .code("INFO-000")
            .message("정상 처리되었습니다")
            .build();

        return SeoulAirQuality.builder()
            .totalCount(1)
            .result(result)
            .row(List.of(item))
            .build();
    }

    private RegionAirQuality createRegionAirQuality(String pm25, String pm10) {
        RegionAirQuality.Item item = RegionAirQuality.Item.builder()
            .so2Grade("1")
            .pm10Value(pm10)
            .pm25Value(pm25)
            .pm10Grade("2")
            .build();

        RegionAirQuality.Body body = RegionAirQuality.Body.builder()
            .totalCount(1)
            .items(List.of(item))
            .pageNo(1)
            .numOfRows(1)
            .build();

        RegionAirQuality.Header header = RegionAirQuality.Header.builder()
            .resultMsg("NORMAL_CODE")
            .resultCode("00")
            .build();

        RegionAirQuality.Response response = RegionAirQuality.Response.builder()
            .body(body)
            .header(header)
            .build();

        return RegionAirQuality.builder()
            .response(response)
            .build();
    }

    private Optional<ObservatoryResponse> createObservatoryResponse(String stationName,
        String addr) {
        return Optional.of(ObservatoryResponse.builder()
            .stationCode("111123")
            .stationName(stationName)
            .addr(addr)
            .build());
    }

    private AirQualityResponse createAirQualityResponse() {
        return AirQualityResponse.builder()
            .grade("보통")
            .pm10("61")
            .pm25("20")
            .build();
    }

    private CachedAirQuality getCache(String value) {
        return (CachedAirQuality) redisTemplate.opsForValue().get(key + value);
    }

    private void setCache(String value, boolean fresh) {
        redisTemplate.opsForValue()
            .set(key + value, new CachedAirQuality(createAirQualityResponse(), fresh));
    }

}
