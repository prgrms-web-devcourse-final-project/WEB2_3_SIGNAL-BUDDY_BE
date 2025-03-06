package org.programmers.signalbuddyfinal.domain.unifiedSignal.controller;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.SimpleType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.programmers.signalbuddyfinal.domain.crossroad.dto.CrossroadResponse;
import org.programmers.signalbuddyfinal.domain.crossroad.service.CrossroadService;
import org.programmers.signalbuddyfinal.domain.trafficSignal.dto.TrafficResponse;
import org.programmers.signalbuddyfinal.domain.trafficSignal.service.TrafficService;
import org.programmers.signalbuddyfinal.global.support.ControllerTest;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;


@WebMvcTest(SignalController.class)
public class SignalControllerTest extends ControllerTest {

    @MockitoBean
    private CrossroadService crossroadService;

    @MockitoBean
    private TrafficService trafficService;

    private final String tag = "SignalUnified API";

    private String crossId = "";
    private String trafficId = "";
    private CrossroadResponse responseCrossroad;
    private TrafficResponse responseTraffic;

    @BeforeEach
    void setUp() throws NullPointerException {
        crossId = "Crossroad_10";
        trafficId = "Traffic_10";

        responseCrossroad = CrossroadResponse.builder()
                .crossroadId(Long.valueOf( crossId.replaceAll("\\D+", "")))
                .name("올림픽대로")
                .lat(35.241443)
                .lng(127.5346)
                .status("FALSE")
                .build();

        responseTraffic = TrafficResponse.builder()
                .serialNumber(Long.valueOf(trafficId.replaceAll("\\D+", "")))
                .district("강남구")
                .signalType("보행등")
                .address("강남구 대치동 973-14 대")
                .lat(35.241443)
                .lng(127.5455)
                .build();
    }

    @DisplayName("신호등 정보 캐싱")
    @Test
    void saveSignalInfoToRedis() throws Exception {

        doNothing().when(crossroadService).saveAroundCrossroad(anyDouble(),anyDouble());

        //When
        ResultActions result = mockMvc.perform(
                post("/api/unifiedSignal/save")
                        .param("lat", String.valueOf(responseCrossroad.getLat()))
                        .param("lng", String.valueOf(responseCrossroad.getLng()))
        );

        //Then
        result.andExpect(status().isOk())
            .andDo(print())
            .andDo(
                document(
                   "신호등 정보 캐싱",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    resource(
                        ResourceSnippetParameters.builder()
                            .tag(tag)
                            .summary("신호등 정보 캐싱")
                            .queryParameters(
                                parameterWithName("lat").type(SimpleType.NUMBER).description("위도"),
                                parameterWithName("lng").type(SimpleType.NUMBER).description("경도")
                            )
                            .responseFields(
                                fieldWithPath("status").description("성공 여부"),
                                fieldWithPath("message").description("음답 메세지")
                            ).build()
                    )
                )
            );

    }

    @DisplayName("캐싱된 신호등 정보 호출")
    @Test
    void findById() throws Exception {

        given(crossroadService.crossraodFindById(anyString())).willReturn(responseCrossroad);

        //When
        ResultActions resultCross = mockMvc.perform(
                get("/api/unifiedSignal/find-info/{id}", crossId)
        );

        ResultActions resultTraffic = mockMvc.perform(
                get("/api/unifiedSignal/find-info/{id}", trafficId)
        );

        //Then
        resultCross.andExpect(status().isOk())
            .andExpect(jsonPath("$.data.name").value(responseCrossroad.getName()))
            .andDo(print())
            .andDo(
                document(
                    "캐싱된 신호등 정보 호출",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    resource(
                        ResourceSnippetParameters.builder()
                                .tag(tag)
                                .summary("신호등 정보 호출")
                                .pathParameters(
                                        parameterWithName("id").type(SimpleType.NUMBER).description("Redis Id -> Crossroad_api_id")
                                )
                                .responseFields(
                                        fieldWithPath("data.crossroadApiId").description("api_id 값"),
                                        fieldWithPath("data.name").description("올림픽대로와 같은 교차로명"),
                                        fieldWithPath("data.lat").description("위도 값"),
                                        fieldWithPath("data.lng").description("경도 값"),
                                        fieldWithPath("data.lng").description("데이터 상태 값")
                                ).build()
                    )
                )
            );

        resultCross.andExpect(status().isOk())
            .andExpect(jsonPath("$.data.serialNumber").value(responseTraffic.getSerialNumber()))
            .andDo(print())
            .andDo(
                document(
                    "캐싱된 보행등 정보 호출",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    resource(
                        ResourceSnippetParameters.builder()
                                .tag(tag)
                                .summary("보행등 정보 호출")
                                .pathParameters(
                                        parameterWithName("id").type(SimpleType.NUMBER).description("Redis Id -> Traffic_serialNumber")
                                )
                                .responseFields(
                                        fieldWithPath("data.serialNumber").description("serialNumber 값"),
                                        fieldWithPath("data.district").description("자치구"),
                                        fieldWithPath("data.address").description("주소"),
                                        fieldWithPath("data.signalType").description("신호등 타입"),
                                        fieldWithPath("data.lat").description("위도 값"),
                                        fieldWithPath("data.lng").description("경도 값")
                                ).build()
                    )
                )
            );

    }

}
