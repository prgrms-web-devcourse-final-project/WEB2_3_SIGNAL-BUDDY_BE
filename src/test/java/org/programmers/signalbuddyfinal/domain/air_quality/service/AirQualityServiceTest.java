package org.programmers.signalbuddyfinal.domain.air_quality.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.programmers.signalbuddyfinal.domain.air_quality.dto.AirQualityResponse;
import org.programmers.signalbuddyfinal.domain.air_quality.dto.CachedAirQuality;
import org.programmers.signalbuddyfinal.global.config.RedisConfig;
import org.programmers.signalbuddyfinal.global.db.RedisTestContainer;
import org.programmers.signalbuddyfinal.global.exception.BusinessException;
import org.programmers.signalbuddyfinal.global.support.ServiceTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@ContextConfiguration(initializers = AirQualityServiceTest.MockServerInitializer.class)
@Import(RedisConfig.class)
@TestPropertySource(properties = {
    "schedule.air-quality-api.cron=0 0 0/1 * * ?",
    "schedule.air-quality-api.lockAtMostFor=10m",
    "schedule.air-quality-api.lockAtLeastFor=50m"
})

public class AirQualityServiceTest extends ServiceTest implements RedisTestContainer {

    @Autowired
    private AirQualityService airQualityService;
    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;
    private static MockWebServer mockWebServer;
    private String key = "air-quality: ";
    private static CachedAirQuality cache;

    @BeforeAll
    static void setUp() throws Exception {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        AirQualityResponse airQualityResponse = AirQualityResponse.builder()
            .grade("보통")
            .pm10("61")
            .pm25("20")
            .build();
        cache = new CachedAirQuality(airQualityResponse, true);
    }

    @BeforeEach
    void clearRedis() {
        RedisConnectionFactory factory = redisTemplate.getConnectionFactory();
        if (factory != null) {
            factory.getConnection().serverCommands().flushAll();
        }
    }

    @AfterAll
    static void tearDown() throws Exception {
        mockWebServer.shutdown();
    }

    public static class MockServerInitializer implements
        ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            String baseUrl = "http://localhost:" + mockWebServer.getPort();
            TestPropertyValues.of("air-quality.base-url=" + baseUrl)
                .applyTo(applicationContext.getEnvironment());
        }
    }

    @DisplayName("첫 요청 청공 시 응답 반환 및 캐싱 테스트")
    @Test
    void successRequestTest() {
        createMockWebServer(createResponse());

        AirQualityResponse response = airQualityService.getAirQuality();
        CachedAirQuality cache = (CachedAirQuality) redisTemplate.opsForValue().get(key);

        assertThat(response.getGrade()).isEqualTo("보통");
        assertThat(cache).isNotNull();
        assertThat(cache.getData().getGrade()).isEqualTo("보통");
        assertThat(cache.isFresh()).isTrue();
    }

    @DisplayName("두번 째 요청시 캐싱된 데이터 반환")
    @Test
    void successSecondRequestTest() {

        int count = mockWebServer.getRequestCount();
        createMockWebServer(createResponse());
        redisTemplate.opsForValue().set(key, cache);

        AirQualityResponse response = airQualityService.getAirQuality();

        assertThat(response).isNotNull();
        assertThat(response.getGrade()).isEqualTo("보통");
        assertThat(response.getPm10()).isEqualTo("61");
        assertThat(response.getPm25()).isEqualTo("20");
        assertThat(mockWebServer.getRequestCount()).isEqualTo(count);
    }

    @DisplayName("failBack 실패 테스트")
    @Test
    void failBackRequestTest() {
        createMockWebServer(createFailResponse());
        assertThrows(BusinessException.class, () -> airQualityService.getAirQuality());
    }

    @DisplayName("failBack 성공 테스트")
    @Test
    void failBackSuccessTest() {

        createMockWebServer(createFailResponse());
        redisTemplate.opsForValue().set(key, cache);
        CachedAirQuality before = (CachedAirQuality) redisTemplate.opsForValue().get(key);

        airQualityService.updateAriQuality();
        CachedAirQuality after = (CachedAirQuality) redisTemplate.opsForValue().get(key);

        assertThat(after.isFresh()).isFalse();
        assertThat(after.getData()).isEqualTo(before.getData());

    }

    private void createMockWebServer(String response){
        mockWebServer.enqueue(new MockResponse()
            .setBody(response)
            .addHeader("Content-Type", "application/json")
            .setResponseCode(200)
        );
    }

    private String createResponse() {
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

    private String createFailResponse() {
        return "<RESULT>\n"
            + "<script/>\n"
            + "<script/>\n"
            + "<CODE>ERROR-300</CODE>\n"
            + "<MESSAGE>\n"
            + "<![CDATA[ 필수 값이 누락되어 있습니다. 요청인자를 참고 하십시오. ]]>\n"
            + "</MESSAGE>\n"
            + "</RESULT>";
    }
}
