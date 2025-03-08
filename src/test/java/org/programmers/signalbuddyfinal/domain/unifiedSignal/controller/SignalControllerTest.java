package org.programmers.signalbuddyfinal.domain.unifiedSignal.controller;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.SimpleType;
import java.util.Collections;
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

    private Long crossId = 0L;
    private Long trafficId = 0L;
    private CrossroadResponse responseCrossroad;
    private TrafficResponse responseTraffic;

    @BeforeEach
    void setUp() throws NullPointerException {
        crossId = 10L;
        trafficId = 10L;

        responseCrossroad = CrossroadResponse.builder()
                .crossroadId(crossId)
                .crossroadApiId(String.valueOf(crossId))
                .name("올림픽대로")
                .lat(35.241443)
                .lng(127.5346)
                .status("FALSE")
                .build();

        responseTraffic = TrafficResponse.builder()
                .trafficSignalId(trafficId)
                .serialNumber(10L)
                .district("강남구")
                .signalType("보행등")
                .address("강남구 대치동 973-14 대")
                .lat(35.241443)
                .lng(127.5455)
                .build();
    }

    @DisplayName("교차로 정보 캐싱")
    @Test
    void saveCrossInfoToRedis() throws Exception {

        given( crossroadService.searchAndSaveCrossroad(anyDouble(),anyDouble(),anyInt())).willReturn( Collections.singletonList(responseCrossroad) );

        //When
        ResultActions result = mockMvc.perform(
            get("/api/unifiedSignal/search_around/crossroad")
                .queryParam("lat", "35.241443")
                .queryParam("lng", "127.5346")
                .queryParam("radius", "1000")
        );

        //Then
        result.andExpect(status().isOk())
            .andDo(print())
            .andDo(
                document(
                   "교차로 정보 캐싱",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    resource(
                        ResourceSnippetParameters.builder()
                            .tag(tag)
                            .summary("교차로 정보 캐싱")
                            .queryParameters(
                                parameterWithName("lat").type(SimpleType.STRING).description("위도"),
                                parameterWithName("lng").type(SimpleType.STRING).description("경도"),
                                parameterWithName("radius").type(SimpleType.STRING).description("사용자 주변 radius 미터")
                            )
                            .responseFields(
                                subsectionWithPath("data").description("교차로 정보 리스트"),
                                fieldWithPath("data[].crossroadId").description("id 값 (PK)"),
                                fieldWithPath("data[].crossroadApiId").description("api_id 값"),
                                fieldWithPath("data[].name").description("올림픽대로와 같은 교차로명"),
                                fieldWithPath("data[].lat").description("위도 값"),
                                fieldWithPath("data[].lng").description("경도 값"),
                                fieldWithPath("data[].status").description("데이터 상태 값"),
                                fieldWithPath("status").description("성공 여부"),
                                fieldWithPath("message").description("메시지 (null 일 수 있음)")
                            ).build()
                    )
                )
            );

    }

    @DisplayName("보행등 정보 캐싱")
    @Test
    void saveTrafficInfoToRedis() throws Exception {

        given( trafficService.searchAndSaveTraffic(anyDouble(),anyDouble(),anyInt())).willReturn( Collections.singletonList(responseTraffic) );

        //When
        ResultActions result = mockMvc.perform(
            get("/api/unifiedSignal/search_around/traffic")
                .queryParam("lat", "35.241443")
                .queryParam("lng", "127.5346")
                .queryParam("radius", "1000")
        );

        //Then
        result.andExpect(status().isOk())
            .andDo(print())
            .andDo(
                document(
                    "보행등 정보 캐싱",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    resource(
                        ResourceSnippetParameters.builder()
                            .tag(tag)
                            .summary("보행등 정보 캐싱")
                            .queryParameters(
                                parameterWithName("lat").type(SimpleType.STRING).description("위도"),
                                parameterWithName("lng").type(SimpleType.STRING).description("경도"),
                                parameterWithName("radius").type(SimpleType.STRING).description("사용자 주변 radius 미터")
                            )
                            .responseFields(
                                subsectionWithPath("data").description("보행등 정보 리스트").optional(),
                                fieldWithPath("data[].trafficSignalId").description("보행등 id (PK)"),
                                fieldWithPath("data[].serialNumber").description("연번"),
                                fieldWithPath("data[].district").description("자치구"),
                                fieldWithPath("data[].address").description("주소"),
                                fieldWithPath("data[].signalType").description("신호등 타입"),
                                fieldWithPath("data[].lat").description("위도 값"),
                                fieldWithPath("data[].lng").description("경도 값"),
                                fieldWithPath("status").description("성공 여부"),
                                fieldWithPath("message").description("메시지 (null 일 수 있음)")
                            ).build()
                    )
                )
            );

    }

    @DisplayName("캐싱된 교차로 정보 호출")
    @Test
    void findCrossroadByApiId() throws Exception {

        //Given
        given(crossroadService.crossroadFindById(anyLong())).willReturn(responseCrossroad);

        //When
        ResultActions resultCross = mockMvc.perform(
                get("/api/unifiedSignal/find-info/crossroad/{id}", crossId)
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
                                        parameterWithName("id").description("id값")
                                )
                                .responseFields(
                                        fieldWithPath("data.crossroadId").description("id 값"),
                                        fieldWithPath("data.crossroadApiId").description("api_id 값"),
                                        fieldWithPath("data.name").description("올림픽대로와 같은 교차로명"),
                                        fieldWithPath("data.lat").description("위도 값"),
                                        fieldWithPath("data.lng").description("경도 값"),
                                        fieldWithPath("data.lng").description("데이터 상태 값"),
                                        fieldWithPath("data.status").description("결과 상태"),
                                        fieldWithPath("status").description("성공 여부"),
                                        fieldWithPath("message").description("메시지 (null 일 수 있음)")
                                ).build()
                    )
                )
            );

    }

    @DisplayName("캐싱된 보행등 정보 호출")
    @Test
    void findCrossroadBySerialNumber() throws Exception {

        //Given
        given(trafficService.trafficFindById(anyLong())).willReturn(responseTraffic);

        //When
        ResultActions resultTraffic = mockMvc.perform(
                get("/api/unifiedSignal/find-info/traffic/{id}", trafficId)
        );

        //Then
        resultTraffic.andExpect(status().isOk())
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
                                            parameterWithName("id").description("연번")
                                    )
                                .responseFields(
                                    fieldWithPath("data.trafficSignalId").description("id값 (PK)"),
                                    fieldWithPath("data.serialNumber").description("연번"),
                                    fieldWithPath("data.district").description("자치구"),
                                    fieldWithPath("data.address").description("주소"),
                                    fieldWithPath("data.signalType").description("신호등 타입"),
                                    fieldWithPath("data.lat").description("위도 값"),
                                    fieldWithPath("data.lng").description("경도 값"),
                                    fieldWithPath("status").description("성공 여부"),
                                    fieldWithPath("message").description("메시지 (null 일 수 있음)")
                                ).build()
                    )
                )
            );
    }

}
