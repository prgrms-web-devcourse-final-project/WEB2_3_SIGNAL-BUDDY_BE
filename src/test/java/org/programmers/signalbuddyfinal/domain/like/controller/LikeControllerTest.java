package org.programmers.signalbuddyfinal.domain.like.controller;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.programmers.signalbuddyfinal.global.support.RestDocsFormatGenerators.commonResponseFormat;
import static org.programmers.signalbuddyfinal.global.support.RestDocsFormatGenerators.getTokenExample;
import static org.programmers.signalbuddyfinal.global.support.RestDocsFormatGenerators.jwtFormat;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.SimpleType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.programmers.signalbuddyfinal.domain.like.dto.LikeExistResponse;
import org.programmers.signalbuddyfinal.domain.like.service.LikeService;
import org.programmers.signalbuddyfinal.global.anotation.WithMockCustomUser;
import org.programmers.signalbuddyfinal.global.dto.CustomUser2Member;
import org.programmers.signalbuddyfinal.global.support.ControllerTest;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.shaded.org.apache.commons.lang3.ArrayUtils;

@WebMvcTest(LikeController.class)
class LikeControllerTest extends ControllerTest {

    @MockitoBean
    private LikeService likeService;

    private final String tag = "Like API";

    @DisplayName("좋아요를 추가한다.")
    @Test
    @WithMockCustomUser
    void addLike() throws Exception {
        // Given
        Long feedbackId = 1L;
        doNothing().when(likeService).addLike(eq(feedbackId), any(CustomUser2Member.class));

        // When
        ResultActions result = mockMvc.perform(
            post("/api/feedbacks/{feedbackId}/like", feedbackId)
                .header(HttpHeaders.AUTHORIZATION, getTokenExample())
        );

        // Then
        result.andExpect(status().isCreated())
            .andDo(
                document(
                    "좋아요 추가",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    resource(
                        ResourceSnippetParameters.builder()
                            .tag(tag)
                            .summary("좋아요 추가")
                            .requestHeaders(
                                jwtFormat()
                            )
                            .pathParameters(
                                parameterWithName("feedbackId").type(SimpleType.NUMBER)
                                    .description("좋아요를 추가할 피드백 ID")
                            )
                            .build()
                    )
                )
            );
    }

    @DisplayName("좋아요 여부를 확인한다.")
    @Test
    @WithMockCustomUser
    void existsLike() throws Exception {
        // Given
        Long feedbackId = 1L;
        LikeExistResponse response = new LikeExistResponse(true);

        given(likeService.existsLike(eq(feedbackId), any(CustomUser2Member.class))).willReturn(response);

        // When
        ResultActions result = mockMvc.perform(
            get("/api/feedbacks/{feedbackId}/like/exist", feedbackId)
        );

        // Then
        result.andExpect(status().isOk())
            .andDo(
                document(
                    "좋아요 여부 확인",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    resource(
                        ResourceSnippetParameters.builder()
                            .tag(tag)
                            .summary("좋아요 여부 확인")
                            .pathParameters(
                                parameterWithName("feedbackId").type(SimpleType.NUMBER)
                                    .description("좋아요 여부를 확인할 피드백 ID")
                            )
                            .responseFields(
                                ArrayUtils.add(
                                    commonResponseFormat(),
                                    fieldWithPath("data.status").type(JsonFieldType.BOOLEAN)
                                        .description("해당 피드백에 대한 좋아요 여부")
                                )
                            )
                            .build()
                    )
                )
            );
    }

    @DisplayName("좋아요를 취소한다.")
    @Test
    @WithMockCustomUser
    void deleteLike() throws Exception {
        // Given
        Long feedbackId = 1L;
        doNothing().when(likeService).addLike(eq(feedbackId), any(CustomUser2Member.class));

        // When
        ResultActions result = mockMvc.perform(
            delete("/api/feedbacks/{feedbackId}/like", feedbackId)
                .header(HttpHeaders.AUTHORIZATION, getTokenExample())
        );

        // Then
        result.andExpect(status().isOk())
            .andDo(
                document(
                    "좋아요 삭제",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    resource(
                        ResourceSnippetParameters.builder()
                            .tag(tag)
                            .summary("좋아요 삭제")
                            .requestHeaders(
                                jwtFormat()
                            )
                            .pathParameters(
                                parameterWithName("feedbackId").type(SimpleType.NUMBER)
                                    .description("좋아요를 삭제할 피드백 ID")
                            )
                            .build()
                    )
                )
            );
    }
}