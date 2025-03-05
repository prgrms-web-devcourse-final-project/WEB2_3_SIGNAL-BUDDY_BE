package org.programmers.signalbuddyfinal.domain.crossroad.controller;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.programmers.signalbuddyfinal.global.support.RestDocsFormatGenerators.commonResponseFormat;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.SimpleType;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.programmers.signalbuddyfinal.domain.crossroad.dto.CrossroadStateResponse;
import org.programmers.signalbuddyfinal.domain.crossroad.dto.SignalState;
import org.programmers.signalbuddyfinal.domain.crossroad.repository.CrossroadRepository;
import org.programmers.signalbuddyfinal.domain.crossroad.service.CrossroadService;
import org.programmers.signalbuddyfinal.global.support.ControllerTest;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest(CrossroadController.class)
class CrossroadControllerTest extends ControllerTest {

    @MockitoBean
    private CrossroadService crossroadService;

    @MockitoBean
    private CrossroadRepository crossroadRepository;

    private final String tag = "Crossroad API";

    @DisplayName("신호등 잔여시간 정보를 반환한다.")
    @Test
    void checkSignalState() throws Exception {
        // Given
        Long crossroadId = 1L;
        CrossroadStateResponse response = CrossroadStateResponse.builder()
            .transTimestamp(1741054218628L)
            .northTimeLeft(349).northState(SignalState.RED)
            .eastTimeLeft(9).eastState(SignalState.YELLOW)
            .southTimeLeft(349).eastState(SignalState.RED)
            .build();

        given(crossroadService.checkSignalState(anyLong())).willReturn(response);

        // When
        ResultActions result = mockMvc.perform(
            get("/api/crossroads/{crossroadId}/state", crossroadId)
        );

        // Then
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.data.eastTimeLeft").value(9))
            .andDo(
                document(
                    "신호등 잔여시간 확인",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    resource(
                        ResourceSnippetParameters.builder()
                            .tag(tag)
                            .summary("신호등 잔여시간 확인")
                            .pathParameters(
                                parameterWithName("crossroadId").type(SimpleType.NUMBER)
                                    .description("교차로 ID")
                            )
                            .responseFields(
                                ArrayUtils.addAll(
                                    commonResponseFormat(),
                                    fieldWithPath("data.crossroadId")
                                        .description("해당 교차로 ID(PK)"),
                                    fieldWithPath("data.transTimestamp")
                                        .description("전송 시간 (UTC 기준, timestamp 형식)"),
                                    fieldWithPath("data.northTimeLeft")
                                        .description("북쪽 보행 신호 잔여 시간 (1/10초)"),
                                    fieldWithPath("data.eastTimeLeft")
                                        .description("동쪽 보행 신호 잔여 시간 (1/10초)"),
                                    fieldWithPath("data.southTimeLeft")
                                        .description("남쪽 보행 신호 잔여 시간 (1/10초)"),
                                    fieldWithPath("data.westTimeLeft")
                                        .description("서쪽 보행 신호 잔여 시간 (1/10초)"),
                                    fieldWithPath("data.northeastTimeLeft")
                                        .description("북동쪽 보행 신호 잔여 시간 (1/10초)"),
                                    fieldWithPath("data.northwestTimeLeft")
                                        .description("북서쪽 보행 신호 잔여 시간 (1/10초)"),
                                    fieldWithPath("data.southwestTimeLeft")
                                        .description("남서쪽 보행 신호 잔여 시간 (1/10초)"),
                                    fieldWithPath("data.southeastTimeLeft")
                                        .description("남동쪽 보행 신호 잔여 시간 (1/10초)"),
                                    fieldWithPath("data.northState")
                                        .description("북쪽 보행 신호 상태\n" + signalStateFormat()),
                                    fieldWithPath("data.eastState")
                                        .description("동쪽 보행 신호 상태\n" + signalStateFormat()),
                                    fieldWithPath("data.westState")
                                        .description("서쪽 보행 신호 상태\n" + signalStateFormat()),
                                    fieldWithPath("data.southState")
                                        .description("남쪽 보행 신호 상태\n" + signalStateFormat()),
                                    fieldWithPath("data.northeastState")
                                        .description("북동쪽 보행 신호 상태\n" + signalStateFormat()),
                                    fieldWithPath("data.northwestState")
                                        .description("북서쪽 보행 신호 상태\n" + signalStateFormat()),
                                    fieldWithPath("data.southeastState")
                                        .description("남동쪽 보행 신호 상태\n" + signalStateFormat()),
                                    fieldWithPath("data.southwestState")
                                        .description("남서쪽 보행 신호 상태\n" + signalStateFormat())
                                )
                            )
                            .build()
                    )
                )
            );
    }

    private String signalStateFormat() {
        return """
            - RED : 빨간불
            - YELLOW : 깜빡이는 초록불 (건널 때 주의)
            - GREEN : 초록불
            """;
    }
}