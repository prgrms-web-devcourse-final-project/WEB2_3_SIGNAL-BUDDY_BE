package org.programmers.signalbuddyfinal.domain.comment.controller;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.headerWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.programmers.signalbuddyfinal.global.support.RestDocsFormatGenerators.getTokenExample;
import static org.programmers.signalbuddyfinal.global.support.RestDocsFormatGenerators.jwtFormat;
import static org.programmers.signalbuddyfinal.global.support.RestDocsFormatGenerators.pageResponseWithMemberFormat;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.SimpleType;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.programmers.signalbuddyfinal.domain.comment.dto.CommentRequest;
import org.programmers.signalbuddyfinal.domain.comment.dto.CommentResponse;
import org.programmers.signalbuddyfinal.domain.comment.service.CommentService;
import org.programmers.signalbuddyfinal.domain.member.dto.MemberResponse;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberRole;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberStatus;
import org.programmers.signalbuddyfinal.global.anotation.WithMockCustomUser;
import org.programmers.signalbuddyfinal.global.dto.CustomUser2Member;
import org.programmers.signalbuddyfinal.global.dto.PageResponse;
import org.programmers.signalbuddyfinal.global.support.ControllerTest;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.shaded.org.apache.commons.lang3.ArrayUtils;

@WebMvcTest(CommentController.class)
class CommentControllerTest extends ControllerTest {

    @MockitoBean
    private CommentService commentService;

    private final String tag = "Comment API";

    @DisplayName("댓글을 작성한다.")
    @Test
    @WithMockCustomUser
    void writeComment() throws Exception {
        // Given
        Long feedbackId = 1L;
        CommentRequest request = new CommentRequest("test comment");
        doNothing().when(commentService)
            .writeComment(eq(feedbackId), eq(request), any(CustomUser2Member.class));

        // When
        ResultActions result = mockMvc.perform(
            post("/api/feedbacks/{feedbackId}/comments", feedbackId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header(HttpHeaders.AUTHORIZATION, getTokenExample())
        );

        // Then
        result.andExpect(status().isCreated())
            .andDo(
                document(
                    "댓글 작성",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    resource(
                        ResourceSnippetParameters.builder()
                            .tag(tag)
                            .summary("댓글 작성")
                            .requestHeaders(
                                jwtFormat()
                            )
                            .pathParameters(
                                parameterWithName("feedbackId").type(SimpleType.NUMBER)
                                    .description("댓글을 작성할 피드백 ID")
                            )
                            .requestFields(
                                fieldWithPath("content").type(JsonFieldType.STRING)
                                    .description("댓글 내용")
                            )
                            .build()
                    )
                )
            );
    }

    @DisplayName("댓글 목록을 페이징하여 가져온다.")
    @Test
    void searchCommentList() throws Exception {
        // Given
        Long feedbackId = 1L;
        String content = "test comment";
        Pageable pageable = PageRequest.of(3, 10);
        List<CommentResponse> comments = new ArrayList<>();
        for (int i = 1; i < 11; i++) {
            MemberResponse member = MemberResponse.builder()
                .memberId((long) i).email(i + "aaaaaa@aaaa.com").nickname("aaaa" + i)
                .profileImageUrl("https://image.com/sfdfs")
                .role(MemberRole.USER).memberStatus(MemberStatus.ACTIVITY)
                .build();
            CommentResponse comment = CommentResponse.builder()
                .commentId((long) i).content(content)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .member(member)
                .build();
            comments.add(comment);
        }
        PageResponse<CommentResponse> response = new PageResponse<>(
            new PageImpl<>(comments, pageable, 55)
        );

        given(commentService.searchCommentList(anyLong(), eq(pageable))).willReturn(response);

        // When
        ResultActions result = mockMvc.perform(
            get("/api/feedbacks/{feedbackId}/comments", feedbackId)
                .param("page", String.valueOf(pageable.getPageNumber()))
                .param("size", String.valueOf(pageable.getPageSize()))
        );

        // Then
        result.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(
                jsonPath("$.data.searchResults[0].commentId")
                    .value(response.getSearchResults().get(0).getCommentId())
            )
            .andDo(
                document(
                    "댓글 목록",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    resource(
                        ResourceSnippetParameters.builder()
                            .tag(tag)
                            .summary("댓글 목록")
                            .pathParameters(
                                parameterWithName("feedbackId").type(SimpleType.NUMBER)
                                    .description("댓글을 작성할 피드백 ID")
                            )
                            .queryParameters(
                                parameterWithName("page").type(SimpleType.NUMBER)
                                    .description("페이지 번호 (기본값 : 0, 0부터 시작)").optional(),
                                parameterWithName("size").type(SimpleType.NUMBER)
                                    .description("페이지 크기 (기본값 : 7)").optional()
                            )
                            .responseFields(
                                ArrayUtils.addAll(
                                    pageResponseWithMemberFormat(),
                                    fieldWithPath("data.searchResults[].commentId")
                                        .type(JsonFieldType.NUMBER)
                                        .description("댓글 ID"),
                                    fieldWithPath("data.searchResults[].content")
                                        .type(JsonFieldType.STRING)
                                        .description("댓글 내용"),
                                    fieldWithPath("data.searchResults[].createdAt")
                                        .type(JsonFieldType.STRING)
                                        .description("댓글 생성일"),
                                    fieldWithPath("data.searchResults[].updatedAt")
                                        .type(JsonFieldType.STRING)
                                        .description("댓글 수정일")
                                )
                            )
                            .build()
                    )
                )
            );
    }

    @DisplayName("댓글을 수정한다.")
    @Test
    @WithMockCustomUser
    void modifyComment() throws Exception {
        // Given
        Long feedbackId = 1L;
        Long commentId = 1L;
        CommentRequest request = new CommentRequest("test updated comment");
        doNothing().when(commentService)
            .updateComment(eq(commentId), eq(request), any(CustomUser2Member.class));

        // When
        ResultActions result = mockMvc.perform(
            patch("/api/feedbacks/{feedbackId}/comments/{commentId}",
                feedbackId, commentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header(HttpHeaders.AUTHORIZATION, getTokenExample())
        );

        // Then
        result.andExpect(status().isOk())
            .andDo(
                document(
                    "댓글 수정",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    resource(
                        ResourceSnippetParameters.builder()
                            .tag(tag)
                            .summary("댓글 수정")
                            .requestHeaders(
                                jwtFormat()
                            )
                            .pathParameters(
                                parameterWithName("feedbackId").type(SimpleType.NUMBER)
                                    .description("댓글을 수정할 피드백 ID"),
                                parameterWithName("commentId").type(SimpleType.NUMBER)
                                    .description("수정할 댓글 ID")
                            )
                            .requestFields(
                                fieldWithPath("content").type(JsonFieldType.STRING)
                                    .description("수정한 댓글 내용")
                            )
                            .build()
                    )
                )
            );
    }

    @DisplayName("댓글을 삭제한다.")
    @Test
    @WithMockCustomUser
    void deleteComment() throws Exception{
        // Given
        Long feedbackId = 1L;
        Long commentId = 1L;
        doNothing().when(commentService)
            .deleteComment(eq(commentId), any(CustomUser2Member.class));

        // When
        ResultActions result = mockMvc.perform(
            delete(
                "/api/feedbacks/{feedbackId}/comments/{commentId}",
                feedbackId, commentId)
                .header(HttpHeaders.AUTHORIZATION, getTokenExample())
        );

        // Then
        result.andExpect(status().isOk())
            .andDo(
                document(
                    "댓글 삭제",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    resource(
                        ResourceSnippetParameters.builder()
                            .tag(tag)
                            .summary("댓글 삭제")
                            .requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).type(SimpleType.STRING)
                                    .description("JWT")
                            )
                            .pathParameters(
                                parameterWithName("feedbackId").type(SimpleType.NUMBER)
                                    .description("댓글을 삭제할 피드백 ID"),
                                parameterWithName("commentId").type(SimpleType.NUMBER)
                                    .description("삭제할 댓글 ID")
                            )
                            .build()
                    )
                )
            );
    }
}