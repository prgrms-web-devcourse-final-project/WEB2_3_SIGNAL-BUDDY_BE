package org.programmers.signalbuddyfinal.domain.admin.controller;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.programmers.signalbuddyfinal.global.support.RestDocsFormatGenerators.commonResponseFormat;
import static org.programmers.signalbuddyfinal.global.support.RestDocsFormatGenerators.getTokenExample;
import static org.programmers.signalbuddyfinal.global.support.RestDocsFormatGenerators.pageResponseFormat;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epages.restdocs.apispec.ResourceDocumentation;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.SimpleType;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.programmers.signalbuddyfinal.domain.admin.dto.AdminMemberResponse;
import org.programmers.signalbuddyfinal.domain.admin.dto.AdminPostItResponse;
import org.programmers.signalbuddyfinal.domain.admin.dto.MemberFilterRequest;
import org.programmers.signalbuddyfinal.domain.admin.dto.PostItFilterRequest;
import org.programmers.signalbuddyfinal.domain.admin.dto.enums.Deleted;
import org.programmers.signalbuddyfinal.domain.admin.service.AdminPostItService;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberRole;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberStatus;
import org.programmers.signalbuddyfinal.domain.postit.dto.PostItResponse;
import org.programmers.signalbuddyfinal.domain.postit.entity.Danger;
import org.programmers.signalbuddyfinal.global.anotation.WithMockCustomUser;
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

@WebMvcTest(AdminPostItController.class)
public class AdminPostItControllerTest extends ControllerTest {

    private final String tag = "Admin API";

    @MockitoBean
    private AdminPostItService adminPostItService;

    @DisplayName("전체 포스트잇 조회")
    @Test
    public void getAllPostIt() throws Exception {
        final List<AdminPostItResponse> postIts = List.of(
            new AdminPostItResponse(Danger.NOTICE, "제목1", "내용1", "user1@test.com",
                LocalDateTime.of(2025, 1, 20, 11, 35)),
            new AdminPostItResponse(Danger.NOTICE, "제목1", "내용1", "user1@test.com",
                LocalDateTime.of(2025, 1, 20, 11, 35))
            );

        final Pageable pageable = PageRequest.of(0, 10);
        final PageResponse<AdminPostItResponse> response = new PageResponse<>(
            new PageImpl<>(postIts, pageable,
                postIts.size()));
        final String content = "User1";

        given(adminPostItService.getAllPostIt(any(Pageable.class))).willReturn(response);

        mockMvc.perform(
                get("/api/admin/postits", content).param("page", "0")
                    .param("size", "10"))
            .andExpect(status().isOk())
            .andDo(print())
            .andDo(document("포스트잇 전체 조회",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                resource(
                    ResourceSnippetParameters.builder()
                        .tag(tag)
                        .summary("포스트잇 전체 조회")
                        .description("모든 포스트잇을 조회하는 API")
                        .queryParameters(
                            parameterWithName("page").description("페이지 번호"),
                            parameterWithName("size").description("페이지 크기")
                        )
                        .responseFields(
                            ArrayUtils.addAll(
                                pageResponseFormat(),
                                fieldWithPath("data.searchResults[].danger").description("""
                                    위험도
                                    - DANGER : 위험
                                    - WARNING : 경고
                                    - NOTICE : 안내
                                    """),
                                fieldWithPath("data.searchResults[].subject").description("제목"),
                                fieldWithPath("data.searchResults[].content").description("내용"),
                                fieldWithPath("data.searchResults[].email").description("작성자"),
                                fieldWithPath("data.searchResults[].expireDate").description("만료일")
                            )
                        ).build()
                )));
    }

    @Test
    @DisplayName("관리자 포스트잇 해결")
    @WithMockCustomUser
    public void completePostIt() throws Exception {
        PostItResponse postItResponse = createResponse(1L);

        given(adminPostItService.completePostIt(anyLong())).willReturn(postItResponse);

        final ResultActions result = mockMvc.perform(patch("/api/admin/postits/{postitId}",1L)
            .with(csrf())
            .header(HttpHeaders.AUTHORIZATION, getTokenExample())
        );

        result.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andDo(
                document(
                    "관리자 포스트잇 해결",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    resource(
                        ResourceSnippetParameters.builder()
                            .tag(tag)
                            .summary("포스트잇을 해결상태로 변경하는 API")
                            .pathParameters(
                                ResourceDocumentation.parameterWithName("postitId").type(SimpleType.NUMBER)
                                    .description("해결하려는 포스트잇 ID")
                            )
                            .build()
                    ),
                    responseFields(
                        ArrayUtils.addAll(
                            commonResponseFormat(),
                            fieldWithPath("data.postitId")
                                .type(JsonFieldType.NUMBER)
                                .description("생성된 포스트잇 ID"),
                            fieldWithPath("data.danger")
                                .type(JsonFieldType.STRING)
                                .description("""
                                    포스트잇 위험도
                                    - DANGER : 위험
                                    - WARNING : 경고
                                    - NOTICE : 안내
                                    """),
                            fieldWithPath("data.lat")
                                .type(JsonFieldType.NUMBER)
                                .description("등록 위도"),
                            fieldWithPath("data.lng")
                                .type(JsonFieldType.NUMBER)
                                .description("등록 경도"),
                            fieldWithPath("data.subject")
                                .type(JsonFieldType.STRING)
                                .description("포스트잇 제목"),
                            fieldWithPath("data.content")
                                .type(JsonFieldType.STRING)
                                .description("포스트잇 내용"),
                            fieldWithPath("data.imageUrl")
                                .type(JsonFieldType.STRING)
                                .description("등록된 이미지 URL"),
                            fieldWithPath("data.expiryDate")
                                .type(JsonFieldType.STRING)
                                .description("""
                                    포스트잇 삭제 예정일
                                    형식 : YYYY-MM-dd HH-mm
                                    """),
                            fieldWithPath("data.createDate")
                                .type(JsonFieldType.STRING)
                                .description("""
                                    포스트잇 등록일
                                    형식 : YYYY-MM-dd HH-mm
                                    """),
                            fieldWithPath("data.memberId")
                                .type(JsonFieldType.NUMBER)
                                .description("등록한 사용자 ID")
                        )
                    )
                )
            );
    }
    @DisplayName("포스트잇 필터링 조회")
    @Test
    public void FilteredPostIt() throws Exception {
        final PostItFilterRequest filter = PostItFilterRequest.builder()
            .startDate(LocalDateTime.of(2025, 1, 1, 0, 0, 0))
            .endDate(LocalDateTime.of(2025, 2, 20, 0, 0, 0))
            .search("검색어")
            .periods(null)
            .danger(Danger.NOTICE)
            .deleted(Deleted.NOTDELETED)
            .build();

        final List<AdminPostItResponse> postIts = List.of(
            new AdminPostItResponse(Danger.NOTICE, "제목1", "내용1", "user1@test.com",
                LocalDateTime.of(2025, 1, 20, 11, 35)),
            new AdminPostItResponse(Danger.NOTICE, "제목1", "내용1", "user1@test.com",
                LocalDateTime.of(2025, 1, 20, 11, 35))
        );

        final Pageable pageable = PageRequest.of(0, 10);
        final PageResponse<AdminPostItResponse> response = new PageResponse<>(
            new PageImpl<>(postIts, pageable,
                postIts.size()));

        given(adminPostItService.getAllPostItWithFilter(any(Pageable.class),
            any(PostItFilterRequest.class)))
            .willReturn(response);

        final ResultActions result = mockMvc.perform(
                get("/api/admin/postits/filter")
                    .param("page", "0")
                    .param("size", "10")
                    .param("startDate", filter.getStartDate().toString())
                    .param("endDate", filter.getEndDate().toString())
                    .param("period",  filter.getPeriods() != null ? filter.getPeriods().toString() : null)
                    .param("search", filter.getSearch())
                    .param("danger", filter.getSearch().toString())
                    .param("deleted", filter.getSearch().toString())
                    .header("Accept", "application/json"))
            .andExpect(status().isOk())
            .andDo(print())
            .andDo(document("관리자 포스트잇 필터링 조회",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                resource(
                    ResourceSnippetParameters.builder()
                        .tag(tag)
                        .summary("관리자 포스트잇 필터링 조회")
                        .description("관리자가 포스트잇을 필터링해 조회하는 API")
                        .queryParameters(
                            parameterWithName("page").description("페이지 번호"),
                            parameterWithName("size").description("페이지 크기"),
                            parameterWithName("startDate").description("""
                                    (선택) 가입 기간별 조회 1
                                          * 조회 기간 : startDate ~ endDate
                                          * 사용시 startDate endDate 둘 다 입력 필수
                                    - 검색 시작일
                                    - 형식 : YYYY-MM-dd
                                    """).optional(),
                            parameterWithName("endDate").description("""
                                    - 검색 종료일
                                    - 형식 : YYYY-MM-dd
                                    """)
                                .optional(),
                            parameterWithName("period").description(
                                    """
                                (선택) 가입 기간별 조회 2
                                      * 조회 기간 : 현재일 기준
                                - 형식 : YYYY-MM-dd
                                -- TODAY : 오늘
                                -- THREE_DAYS : 3일 전
                                -- WEEK : 일주일 전
                                -- MONTH : 한달 전
                                -- THREE_MONTH : 세달 전
                                
                                가입 기간별 조회1, 2 중복 사용 불가
                                """),
                            parameterWithName("danger").description("""
                                    (선택) 포스트잇 위험도
                                    - DANGER : 위험
                                    - WARNING : 경고
                                    - NOTICE : 안내
                                    """).optional(),
                            parameterWithName("deleted").description("""
                                (선택) 해결/만료 여부
                                형식
                                - DELETED : 해결
                                - NOTDELETED : 미해결
                                """)
                                .optional(),
                            parameterWithName("search").description(
                                    """
                                검색
                                - 제목, 내용
                                """)
                                .optional()
                        )
                        .responseFields(
                            ArrayUtils.addAll(
                                pageResponseFormat(),
                                fieldWithPath("data.searchResults[].danger").description("""
                                    위험도
                                    - DANGER : 위험
                                    - WARNING : 경고
                                    - NOTICE : 안내
                                    """),
                                fieldWithPath("data.searchResults[].subject").description("제목"),
                                fieldWithPath("data.searchResults[].content").description("내용"),
                                fieldWithPath("data.searchResults[].email").description("작성자"),
                                fieldWithPath("data.searchResults[].expireDate").description("만료일")
                            )
                        ).build()
                )));
    }

    private PostItResponse createResponse(Long memberId) {
        return PostItResponse.builder()
            .postitId(1L)
            .danger(Danger.WARNING) // 수정된 오타
            .lat(37.5206868)
            .lng(127.1171114)
            .subject("포스트잇 제목")
            .content("포스트잇 내용")
            .imageUrl("https://image.com/imageUrl")
            .expiryDate(LocalDateTime.of(2025, 1, 8, 1, 25))
            .createDate(LocalDateTime.of(2025, 1, 1, 1, 25))
            .memberId(memberId)
            .build();
    }

}
