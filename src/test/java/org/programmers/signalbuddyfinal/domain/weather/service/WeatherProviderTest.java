package org.programmers.signalbuddyfinal.domain.weather.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.programmers.signalbuddyfinal.domain.weather.dto.Weather;
import org.programmers.signalbuddyfinal.domain.weather.exception.WeatherErrorCode;
import org.programmers.signalbuddyfinal.global.exception.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

@SpringBootTest
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@AutoConfigureTestDatabase(replace = Replace.NONE)
class WeatherProviderTest {

    @Autowired
    private WeatherProvider weatherProvider;

    private MockWebServer mockWebServer;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        ReflectionTestUtils.setField(weatherProvider, "ultraSrtNcst", mockWebServer.url("/ultraSrtNcst").toString());
        ReflectionTestUtils.setField(weatherProvider, "apiKey", "test-api-key");
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @DisplayName("날씨 Open API 에 데이터 요청 성공")
    @Test
    void requestWeatherApiSuccess() throws IOException, InterruptedException {
        final String json = readFileFromResources("response/weather_api_success.json");
        mockWebServer.enqueue(new MockResponse()
            .setBody(json)
            .setResponseCode(200)
            .addHeader("Content-Type", "application/json"));

        final List<Weather> response = weatherProvider.requestWeatherApi(55, 127);

        final RecordedRequest recordedRequest = mockWebServer.takeRequest();
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(recordedRequest.getMethod()).isEqualTo("GET");
            softly.assertThat(recordedRequest.getPath()).contains("nx=55&ny=127");
            softly.assertThat(recordedRequest.getPath()).contains("serviceKey=test-api-key");
            softly.assertThat(response).isNotNull();
            softly.assertThat(response.size()).isEqualTo(8);
            softly.assertThat(response.get(0).getNx()).isEqualTo(55);
            softly.assertThat(response.get(0).getNy()).isEqualTo(127);
        });
    }

    @DisplayName("날씨 Open API 서비스 에러")
    @Test
    void requestWeatherApiFailure() throws IOException, InterruptedException {
        final String xml = readFileFromResources("response/weather_api_fail.xml");
        mockWebServer.enqueue(new MockResponse()
            .setBody(xml)
            .setResponseCode(200)
            .addHeader("Content-Type", "application/xml"));

        assertThatThrownBy(() -> weatherProvider.requestWeatherApi(55, 127))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining(WeatherErrorCode.WEATHER_API_SERVICE_ERROR.getMessage());

        final RecordedRequest recordedRequest = mockWebServer.takeRequest();
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(recordedRequest.getMethod()).isEqualTo("GET");
            softly.assertThat(recordedRequest.getPath()).contains("nx=55&ny=127");
        });
    }

    @DisplayName("날씨 Open API 요청 실패")
    @Test
    void requestWeatherApiFailure2() throws IOException, InterruptedException {
        mockWebServer.enqueue(new MockResponse()
            .setResponseCode(500));

        assertThatThrownBy(() -> weatherProvider.requestWeatherApi(55, 127))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining(WeatherErrorCode.WEATHER_API_REQUEST_FAILED.getMessage());

        final RecordedRequest recordedRequest = mockWebServer.takeRequest();
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(recordedRequest.getMethod()).isEqualTo("GET");
            softly.assertThat(recordedRequest.getPath()).contains("nx=55&ny=127");
        });
    }


    private String readFileFromResources(String filePath) throws IOException {
        return new String(Files.readAllBytes(new ClassPathResource(filePath).getFile().toPath()));
    }
}