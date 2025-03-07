package org.programmers.signalbuddyfinal.domain.recentpath.controller;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static com.epages.restdocs.apispec.Schema.schema;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.programmers.signalbuddyfinal.global.support.RestDocsFormatGenerators.commonResponseFormat;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.SimpleType;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.programmers.signalbuddyfinal.domain.recentpath.dto.RecentPathLinkRequest;
import org.programmers.signalbuddyfinal.domain.recentpath.dto.RecentPathResponse;
import org.programmers.signalbuddyfinal.domain.recentpath.service.RecentPathService;
import org.programmers.signalbuddyfinal.global.support.ControllerTest;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.shaded.org.apache.commons.lang3.ArrayUtils;

@WebMvcTest(RecentPathController.class)
class RecentPathControllerTest extends ControllerTest {

    private final String tag = "RecentPath API";

    @MockitoBean
    private RecentPathService recentPathService;

    @DisplayName("최근 경로 방문 시각 갱신")
    @Test
    void updateRecentPathTime() throws Exception {
        final Long recentPathId = 1L;

        final RecentPathResponse response = RecentPathResponse.builder().recentPathId(1L)
            .lat(37.501).lng(127.001).name("Recent Path").address("Address")
            .lastAccessedAt(LocalDateTime.now()).isBookmarked(false).build();

        given(recentPathService.updateRecentPathTime(recentPathId)).willReturn(response);

        final ResultActions result = mockMvc.perform(patch("/api/recent-path/{id}", recentPathId));

        result.andExpect(status().isOk()).andDo(
            document("최근 경로 방문 시각 갱신", preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()), resource(
                    ResourceSnippetParameters.builder().tag(tag).summary("최근 경로 방문 시각 갱신")
                        .pathParameters(
                            parameterWithName("id").type(SimpleType.NUMBER).description("최근 경로 ID"))
                        .responseSchema(schema("RecentPathResponse")).responseFields(
                            ArrayUtils.addAll(commonResponseFormat(),
                                fieldWithPath("data.recentPathId").type(JsonFieldType.NUMBER)
                                    .description("최근 경로 ID"),
                                fieldWithPath("data.lat").type(JsonFieldType.NUMBER).description("위도"),
                                fieldWithPath("data.lng").type(JsonFieldType.NUMBER).description("경도"),
                                fieldWithPath("data.name").type(JsonFieldType.STRING)
                                    .description("최근 경로 이름"),
                                fieldWithPath("data.address").type(JsonFieldType.STRING)
                                    .description("최근 경로 주소"),
                                fieldWithPath("data.lastAccessedAt").type(JsonFieldType.STRING)
                                    .description("최근 방문 시각"),
                                fieldWithPath("data.bookmarked").type(JsonFieldType.BOOLEAN)
                                    .description("나의 목적지와의 연관관계 여부"))).build())));
    }

    @DisplayName("최근 경로와 북마크 연관 관계 제거")
    @Test
    void unlinkBookmark() throws Exception {
        final Long recentPathId = 1L;

        doNothing().when(recentPathService).unlinkBookmark(recentPathId);

        final ResultActions result = mockMvc.perform(
            delete("/api/recent-path/{id}/bookmarks", recentPathId));

        result.andExpect(status().isOk()).andDo(
            document("최근 경로와 북마크 연관 관계 제거", preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()), resource(
                    ResourceSnippetParameters.builder().tag(tag).summary("최근 경로와 북마크 연관 관계 제거")
                        .pathParameters(
                            parameterWithName("id").type(SimpleType.NUMBER).description("최근 경로 ID"))
                        .build())));
    }

    @DisplayName("최근 경로와 북마크 연관 관계 생성")
    @Test
    void linkBookmark() throws Exception {
        final Long recentPathId = 1L;

        final RecentPathLinkRequest recentPathLinkRequest = new RecentPathLinkRequest(1L);
        final RecentPathResponse response = RecentPathResponse.builder().recentPathId(recentPathId)
            .lastAccessedAt(LocalDateTime.now()).isBookmarked(true).name("Recent Path")
            .address("Address").build();
        when(recentPathService.linkBookmark(recentPathId, recentPathLinkRequest)).thenReturn(
            response);

        final ResultActions result = mockMvc.perform(
            post("/api/recent-path/{id}/bookmarks", recentPathId).contentType(
                    MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(recentPathLinkRequest)));

        result.andExpect(status().isOk()).andDo(
            document("최근 경로와 북마크 연관 관계 생성", preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()), resource(
                    ResourceSnippetParameters.builder().tag(tag).summary("최근 경로와 북마크 연관 관계 생성")
                        .pathParameters(
                            parameterWithName("id").type(SimpleType.NUMBER).description("최근 경로 ID"))
                        .requestSchema(schema("RecentPathLinkRequest")).requestFields(
                            fieldWithPath("bookmarkId").type(JsonFieldType.NUMBER)
                                .description("북마크 ID")).responseSchema(schema("RecentPathResponse"))
                        .responseFields(ArrayUtils.addAll(commonResponseFormat(),
                            fieldWithPath("data.recentPathId").type(JsonFieldType.NUMBER)
                                .description("최근 경로 ID"),
                            fieldWithPath("data.lat").type(JsonFieldType.NUMBER).description("위도"),
                            fieldWithPath("data.lng").type(JsonFieldType.NUMBER).description("경도"),
                            fieldWithPath("data.name").type(JsonFieldType.STRING)
                                .description("최근 경로 이름"),
                            fieldWithPath("data.address").type(JsonFieldType.STRING)
                                .description("최근 경로 주소"),
                            fieldWithPath("data.lastAccessedAt").type(JsonFieldType.STRING)
                                .description("최근 방문 시각"),
                            fieldWithPath("data.bookmarked").type(JsonFieldType.BOOLEAN)
                                .description("나의 목적지와의 연관관계 여부"))).build())));
    }
}