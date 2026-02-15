package org.programmers.signalbuddyfinal.domain.air_quality.controller;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.BDDMockito.given;
import static org.programmers.signalbuddyfinal.global.support.RestDocsFormatGenerators.commonResponseFormat;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.programmers.signalbuddyfinal.domain.air_quality.dto.AirQualityResponse;
import org.programmers.signalbuddyfinal.domain.air_quality.service.AirQualityService;
import org.programmers.signalbuddyfinal.global.support.ControllerTest;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
@WebMvcTest(AirQualityController.class)
class AirQualityControllerTest extends ControllerTest {

    private final String tag = "AirQuality API";

    @MockitoBean
    private AirQualityService airQualityService;

    @DisplayName("미세먼지 조회")
    @Test
    void getAirQuality() throws Exception {
        final AirQualityResponse response = AirQualityResponse.builder()
            .grade("보통")
            .pm25("20")
            .pm10("25")
            .build();

        given(airQualityService.getAirQuality()).willReturn(response);

        mockMvc.perform(get("/api/air-quality"))
            .andExpect(status().isOk())
            .andDo(print())
            .andDo(document("미세먼지 조회",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                resource(
                    ResourceSnippetParameters.builder()
                        .tag(tag)
                        .summary("미세먼지 조회")
                        .description("미세먼지 API를 요청하는 API")
                        .responseFields(
                            ArrayUtils.addAll(
                                commonResponseFormat(),
                                fieldWithPath("data.grade").description("미세먼지 등급"),
                                fieldWithPath("data.pm10").description("미세먼지(PM10) 수치"),
                                fieldWithPath("data.pm25").description("초미세먼지(PM2.5) 수치")
                            )
                        )
                        .build()
                )
            ));
    }
}
