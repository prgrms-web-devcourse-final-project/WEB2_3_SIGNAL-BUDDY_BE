package org.programmers.signalbuddyfinal.domain.traffic.controller;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.programmers.signalbuddyfinal.domain.trafficSignal.controller.TrafficController;
import org.programmers.signalbuddyfinal.domain.trafficSignal.repository.TrafficRepository;
import org.programmers.signalbuddyfinal.domain.trafficSignal.service.TrafficCsvService;
import org.programmers.signalbuddyfinal.global.config.WebConfig;
import org.programmers.signalbuddyfinal.global.support.ControllerTest;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.util.Map;

import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.Mockito.doNothing;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TrafficController.class)
@Import(WebConfig.class)
public class TrafficControllerTest extends ControllerTest {

    private final String tag = "Traffic API";

    @MockitoBean
    private TrafficCsvService trafficCsvService;

    private File testCsvFile;


    @Test
    @DisplayName("데이터 저장")
    void saveTrafficData() throws Exception {

        // given
        testCsvFile = new File(getClass()
                .getClassLoader()
                .getResource("static/traffic/seoul_traffic_light_test.csv")
                .toURI());

        String filePath = testCsvFile.getAbsolutePath();

        Map<String, String> requestBody = Map.of("filePath", filePath);

        // when
        doNothing().when(trafficCsvService).saveCsvData(testCsvFile);

        ResultActions result = mockMvc.perform(
                multipart("/api/traffic/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(requestBody))
        );

        // then
        result.andExpect(status().isOk()).andDo(
            document("csv 파일 저장",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    resource(ResourceSnippetParameters.builder()
                            .tag(tag)
                            .requestFields(
                                    fieldWithPath("filePath").description("CSV 파일의 절대 경로")
                            )
                            .responseFields(
                                    fieldWithPath("status").description("성공 여부"),
                                    fieldWithPath("data").description("응답 데이터"),
                                    fieldWithPath("message").description("응답 메시지 내용")
                            )
                            .build()
                    )
            ));
    }
}
