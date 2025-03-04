package org.programmers.signalbuddyfinal.domain.traffic.controller;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.programmers.signalbuddyfinal.domain.trafficSignal.controller.TrafficController;
import org.programmers.signalbuddyfinal.domain.trafficSignal.service.TrafficCsvService;
import org.programmers.signalbuddyfinal.global.config.WebConfig;
import org.programmers.signalbuddyfinal.global.support.ControllerTest;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TrafficController.class)
@Import(WebConfig.class)
public class TrafficControllerTest extends ControllerTest {

    private final String tag = "Traffic API";

    @MockitoBean
    private TrafficCsvService trafficCsvService;

    @Test
    @DisplayName("데이터 저장")
    void saveTrafficData() throws Exception {

        // given
        String fileName = "seoul_traffic_light_test.csv";

        // when
        doNothing().when(trafficCsvService).saveCsvData(fileName);

        ResultActions result = mockMvc.perform(
                post("/api/traffic/save")
                        .param("fileName", fileName)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                ).andExpect(status().isOk())
                 .andExpect(jsonPath("$.data").value("파일이 성공적으로 저장되었습니다."));


        // then
        result.andExpect(status().isOk()).andDo(
            document("csv 파일 저장",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    resource(ResourceSnippetParameters.builder()
                            .tag(tag)
                            .formParameters(
                                    parameterWithName("fileName").description("CSV 파일 이름")
                            )
                            .responseFields(
                                    fieldWithPath("status").description("성공 여부"),
                                    fieldWithPath("data").description("응답 데이터"),
                                    fieldWithPath("message").description("음답 메세지")
                            )
                            .build()
                    )
            ));
    }
}
