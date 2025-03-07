package org.programmers.signalbuddyfinal.domain.weather.controller;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.Mockito.doNothing;
import static org.programmers.signalbuddyfinal.global.support.RestDocsFormatGenerators.getTokenExample;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.programmers.signalbuddyfinal.domain.weather.repository.GridCoordinateRepository;
import org.programmers.signalbuddyfinal.domain.weather.service.WeatherService;
import org.programmers.signalbuddyfinal.global.support.ControllerTest;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest(WeatherController.class)
class WeatherControllerTest extends ControllerTest {

    private final String tag = "Weather API";
    @MockitoBean
    private WeatherService weatherService;
    @MockitoBean
    private GridCoordinateRepository gridCoordinateRepository;

    @DisplayName("기상청 제공 엑셀 데이터 저장")
    @Test
    void saveExcelTest() throws Exception {
        doNothing().when(weatherService).saveExcel();

        final ResultActions result = mockMvc.perform(
            post("/api/weather").header(HttpHeaders.AUTHORIZATION, getTokenExample()));

        result.andExpect(status().isCreated()).andDo(
            document("기상청 제공 엑셀 데이터 저장", preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()), resource(
                    ResourceSnippetParameters.builder().tag(tag).summary("기상청 제공 엑셀 데이터 저장")
                        .build())));
    }
}