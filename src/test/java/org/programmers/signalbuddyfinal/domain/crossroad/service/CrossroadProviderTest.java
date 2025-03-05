package org.programmers.signalbuddyfinal.domain.crossroad.service;

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
import org.programmers.signalbuddyfinal.domain.crossroad.dto.CrossroadApiResponse;
import org.programmers.signalbuddyfinal.domain.crossroad.dto.CrossroadStateApiResponse;
import org.programmers.signalbuddyfinal.domain.crossroad.dto.SignalState;
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
class CrossroadProviderTest {

    @Autowired
    private CrossroadProvider crossroadProvider;

    private MockWebServer mockWebServer;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        ReflectionTestUtils.setField(
            crossroadProvider, "crossroadApiUrl",
            mockWebServer.url("/crossroad").toString()
        );
        ReflectionTestUtils.setField(
            crossroadProvider, "signalStateUrl",
            mockWebServer.url("/signal").toString()
        );
        ReflectionTestUtils.setField(crossroadProvider, "apiKey", "test-api-key");
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @DisplayName("외부 API에 교차로 데이터 요청하기")
    @Test
    void requestCrossroadApi() throws Exception {
        // Given
        String jsonResponse = readJsonFromResources("response/crossroad_location_api.json");
        mockWebServer.enqueue(new MockResponse()
            .setBody(jsonResponse)
            .setResponseCode(200)
            .addHeader("Content-Type", "application/json")
        );

        // When
        List<CrossroadApiResponse> response = crossroadProvider.requestCrossroadApi(1, 10);

        // Then
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        SoftAssertions.assertSoftly(softAssertions -> {
            softAssertions.assertThat(recordedRequest.getMethod()).isEqualTo("GET");
            softAssertions.assertThat(recordedRequest.getPath()).contains("apiKey=test-api-key");
            softAssertions.assertThat(response).isNotNull();
            softAssertions.assertThat(response.size()).isEqualTo(10);
            softAssertions.assertThat(response.get(3).getCrossroadApiId()).isEqualTo("1014");
            softAssertions.assertThat(response.get(3).getName()).isEqualTo("선사사거리");
            softAssertions.assertThat(response.get(3).getLat()).isEqualTo(37.5547665);
        });
    }

    @DisplayName("외부 API에 특정 교차로의 신호등 잔여 시간 정보 요청하기")
    @Test
    void requestCrossroadStateApi() throws Exception {
        // Given
        String jsonResponse = readJsonFromResources("response/crossroad_state_api.json");
        mockWebServer.enqueue(new MockResponse()
                .setBody(jsonResponse)
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
        );

        // When
        List<CrossroadStateApiResponse> response = crossroadProvider.requestCrossroadStateApi("1029");

        // Then
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        SoftAssertions.assertSoftly(softAssertions -> {
            softAssertions.assertThat(recordedRequest.getMethod()).isEqualTo("GET");
            softAssertions.assertThat(recordedRequest.getPath()).contains("apiKey=test-api-key");
            softAssertions.assertThat(response).isNotNull();
            softAssertions.assertThat(response.size()).isOne();
            softAssertions.assertThat(response.get(0).getCrossroadApiId())
                .isEqualTo("1029");
            softAssertions.assertThat(response.get(0).getNorthTimeLeft()).isEqualTo(1497);
            softAssertions.assertThat(response.get(0).getEastState()).isEqualTo(SignalState.RED);
        });
    }

    private String readJsonFromResources(String filePath) throws IOException {
        return new String(Files.readAllBytes(new ClassPathResource(filePath).getFile().toPath()));
    }
}