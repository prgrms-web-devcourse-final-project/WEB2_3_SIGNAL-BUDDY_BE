package org.programmers.signalbuddyfinal.domain.feedback_summary.controller;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.programmers.signalbuddyfinal.global.support.RestDocsFormatGenerators.commonResponseFormat;
import static org.programmers.signalbuddyfinal.global.support.RestDocsFormatGenerators.getTokenExample;
import static org.programmers.signalbuddyfinal.global.support.RestDocsFormatGenerators.jwtFormat;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.SimpleType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.programmers.signalbuddyfinal.domain.feedback.entity.enums.FeedbackCategory;
import org.programmers.signalbuddyfinal.domain.feedback_summary.dto.FeedbackSummaryResponse;
import org.programmers.signalbuddyfinal.domain.feedback_summary.entity.CrossroadFeedbackCount;
import org.programmers.signalbuddyfinal.domain.feedback_summary.entity.FeedbackCategoryCount;
import org.programmers.signalbuddyfinal.domain.feedback_summary.service.FeedbackSummaryService;
import org.programmers.signalbuddyfinal.global.anotation.WithMockCustomUser;
import org.programmers.signalbuddyfinal.global.support.ControllerTest;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.shaded.org.apache.commons.lang3.ArrayUtils;

@WebMvcTest(FeedbackSummaryController.class)
class FeedbackSummaryControllerTest extends ControllerTest {

    @MockitoBean
    private FeedbackSummaryService feedbackSummaryService;

    private final String tag = "Feedback Summary API";

    @DisplayName("피드백 데이터의 통계 데이터를 가져온다.")
    @Test
    @WithMockCustomUser(roleType = "ROLE_ADMIN")
    void getFeedbackSummary() throws Exception {
        // Given
        LocalDate date = LocalDate.of(2025, 2, 2);

        List<FeedbackCategoryCount> categoryRanks = new ArrayList<>();
        categoryRanks.add(
            FeedbackCategoryCount.builder()
                .category(FeedbackCategory.ETC).count(2L)
                .build()
        );
        categoryRanks.add(
            FeedbackCategoryCount.builder()
                .category(FeedbackCategory.DELAY).count(2L)
                .build()
        );
        categoryRanks.add(
            FeedbackCategoryCount.builder()
                .category(FeedbackCategory.ADD_SIGNAL).count(2L)
                .build()
        );
        List<CrossroadFeedbackCount> crossroadRanks = new ArrayList<>();
        for (int i = 1; i < 3; i++) {
            crossroadRanks.add(
                CrossroadFeedbackCount.builder()
                    .crossroadId((long) i).name("00 사거리 - " + i).count((long) i)
                    .build()
            );
        }
        FeedbackSummaryResponse response = FeedbackSummaryResponse.builder()
            .date(date).todayCount(6L).categoryRanks(categoryRanks).crossroadRanks(crossroadRanks)
            .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
            .build();

        given(feedbackSummaryService.getFeedbackSummary(any(LocalDate.class)))
            .willReturn(response);

        // When
        ResultActions result = mockMvc.perform(
            get("/api/feedback-summary")
                .header(HttpHeaders.AUTHORIZATION, getTokenExample())
                .queryParam("date", "2025-02-02")
        );

        // Then
        result.andExpect(status().isOk())
            .andExpect(
                jsonPath("$.data.date").value("2025-02-02")
            )
            .andDo(
                document(
                    "피드백 통계치 조회",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    resource(
                        ResourceSnippetParameters.builder()
                            .tag(tag)
                            .summary("피드백 통계치 조회")
                            .requestHeaders(
                                jwtFormat()
                            )
                            .queryParameters(
                                parameterWithName("date").type(SimpleType.STRING)
                                    .description("""
                                        피드백 통계의 집계일
                                        형식 : `yyyy-MM-dd`
                                        """)
                            )
                            .responseFields(
                                ArrayUtils.addAll(
                                    commonResponseFormat(),
                                    fieldWithPath("data.date")
                                        .type(JsonFieldType.STRING)
                                        .description("피드백 통계의 집계일"),
                                    fieldWithPath("data.todayCount")
                                        .type(JsonFieldType.NUMBER)
                                        .description("해당일에 생성된 피드백의 총개수"),
                                    fieldWithPath("data.categoryRanks[]")
                                        .type(JsonFieldType.ARRAY)
                                        .description("피드백 유형별 개수 순위"),
                                    fieldWithPath("data.categoryRanks[].category")
                                        .type(JsonFieldType.STRING)
                                        .description("""
                                            피드백 유형
                                            - `DELAY` : 신호 지연
                                            - `MALFUNCTION` : 오작동
                                            - `ADD_SIGNAL` : 신호등 추가
                                            - `ETC` : 기타
                                            """),
                                    fieldWithPath("data.categoryRanks[].count")
                                        .type(JsonFieldType.NUMBER)
                                        .description("피드백 개수"),
                                    fieldWithPath("data.crossroadRanks[]")
                                        .type(JsonFieldType.ARRAY)
                                        .description("교차로별 피드백 개수 순위 (차례대로 1위 ~ 20위)"),
                                    fieldWithPath("data.crossroadRanks[].crossroadId")
                                        .type(JsonFieldType.NUMBER)
                                        .description("교차로 ID(PK)"),
                                    fieldWithPath("data.crossroadRanks[].name")
                                        .type(JsonFieldType.STRING)
                                        .description("교차로 이름"),
                                    fieldWithPath("data.crossroadRanks[].count")
                                        .type(JsonFieldType.NUMBER)
                                        .description("해당 교차로의 피드백 개수"),
                                    fieldWithPath("data.createdAt")
                                        .type(JsonFieldType.STRING)
                                        .description("피드백 통계 생성일"),
                                    fieldWithPath("data.updatedAt")
                                        .type(JsonFieldType.STRING)
                                        .description("피드백 통계 수정일")
                                )
                            )
                            .build()
                    )
                )
            );
    }
}