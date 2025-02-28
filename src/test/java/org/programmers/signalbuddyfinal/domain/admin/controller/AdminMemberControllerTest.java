package org.programmers.signalbuddyfinal.domain.admin.controller;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.any;
import static org.programmers.signalbuddyfinal.global.support.RestDocsFormatGenerators.pageResponseFormat;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.programmers.signalbuddyfinal.domain.admin.dto.AdminMemberResponse;
import org.programmers.signalbuddyfinal.domain.admin.dto.MemberFilterRequest;
import org.programmers.signalbuddyfinal.domain.admin.service.AdminMemberService;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberRole;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberStatus;
import org.programmers.signalbuddyfinal.global.dto.PageResponse;
import org.programmers.signalbuddyfinal.global.support.ControllerTest;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.shaded.org.apache.commons.lang3.ArrayUtils;

import static org.mockito.BDDMockito.*;

import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;

@WebMvcTest(AdminMemberController.class)
public class AdminMemberControllerTest extends ControllerTest {

    private final String tag = "Admin API";

    @MockitoBean
    private AdminMemberService adminService;

    @DisplayName("전체 회원 조회")
    @Test
    public void getAllMember() throws Exception {
        final List<AdminMemberResponse> members = List.of(
            new AdminMemberResponse(1L, "user1@test.com", "User1", "kakao", MemberRole.USER,
                MemberStatus.ACTIVITY, LocalDateTime.of(2025, 1, 22, 0, 0, 0)),
            new AdminMemberResponse(2L, "user2@test.com", "User2", "google", MemberRole.USER,
                MemberStatus.ACTIVITY, LocalDateTime.of(2024, 2, 22, 0, 0, 0))
        );

        final Pageable pageable = PageRequest.of(0, 10);
        final PageResponse<AdminMemberResponse> response = new PageResponse<>(
            new PageImpl<>(members, pageable,
                members.size()));
        final String content = "User1";

        given(adminService.getAllMembers(any(Pageable.class))).willReturn(response);

        final ResultActions result = mockMvc.perform(
                get("/api/admin/members", content).param("page", "0")
                    .param("size", "10"))
            .andExpect(status().isOk())
            .andDo(print())
            .andDo(document("회원 전체 조회", preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                resource(
                    ResourceSnippetParameters.builder()
                        .tag(tag)
                        .summary("회원 전체 조회")
                        .description("모든 회원을 조회하는 API")
                        .queryParameters(
                            parameterWithName("page").description("페이지 번호"),
                            parameterWithName("size").description("페이지 크기")
                        )
                        .responseFields(
                            ArrayUtils.addAll(
                                pageResponseFormat(),
                                fieldWithPath("data.searchResults[].memberId").description("회원 ID"),
                                fieldWithPath("data.searchResults[].email").description("이메일"),
                                fieldWithPath("data.searchResults[].nickname").description("닉네임"),
                                fieldWithPath("data.searchResults[].oauthProvider").description(
                                    "OAuth 제공자"),
                                fieldWithPath("data.searchResults[].role").description("역할"),
                                fieldWithPath("data.searchResults[].status").description("상태"),
                                fieldWithPath("data.searchResults[].createdAt").description("""
                                    가입일
                                    - 형식 : yyyy-MM-dd
                                    """)
                            )
                        ).build()
                )));
    }

    @DisplayName("회원 필터링 조회")
    @Test
    public void FilteredMember() throws Exception {
        final MemberFilterRequest filter = MemberFilterRequest.builder()
            .role(MemberRole.USER)
            .status(MemberStatus.ACTIVITY)
            .oAuthProvider("kakao")
            .startDate(LocalDateTime.of(2025, 1, 1, 0, 0, 0))
            .endDate(LocalDateTime.of(2025, 2, 20, 0, 0, 0))
            .search("user1@test.com")
            .build();

        final List<AdminMemberResponse> members = List.of(
            new AdminMemberResponse(1L, "user1@test.com", "User1", "kakao", MemberRole.USER,
                MemberStatus.ACTIVITY, LocalDateTime.of(2025, 1, 22, 0, 0, 0)),
            new AdminMemberResponse(2L, "user2@test.com", "User2", "google", MemberRole.USER,
                MemberStatus.ACTIVITY, LocalDateTime.of(2024, 2, 22, 0, 0, 0)));

        final Pageable pageable = PageRequest.of(0, 10);
        final PageResponse<AdminMemberResponse> page = new PageResponse<>(
            new PageImpl<>(members, pageable, members.size()));

        given(adminService.getAllMemberWithFilter(any(Pageable.class),
            any(MemberFilterRequest.class)))
            .willReturn(page);

        final ResultActions result = mockMvc.perform(
                get("/api/admin/members/filter")
                    .param("page", "0")
                    .param("size", "10")
                    .param("role", filter.getRole().toString())
                    .param("status", filter.getStatus().toString())
                    .param("oAuthProvider", filter.getOAuthProvider())
                    .param("startDate", filter.getStartDate().toString())
                    .param("endDate", filter.getEndDate().toString())
                    .param("search", filter.getSearch())
                    .header("Accept", "application/json"))
            .andExpect(status().isOk())
            .andDo(print())
            .andDo(document("회원 필터링 조회",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                resource(
                    ResourceSnippetParameters.builder()
                        .tag(tag)
                        .summary("회원 필터링 조회")
                        .description("회원을 필터링해 조회하는 API")
                        .queryParameters(
                            parameterWithName("page").description("페이지 번호"),
                            parameterWithName("size").description("페이지 크기"),
                            parameterWithName("role").description("""
                            (선택) 회원 역할
                            형식
                            - USER
                            - ADMIN
                            """).optional(),
                            parameterWithName("status").description("""
                                (선택) 회원 상태
                                형식
                                - ACTIVITY
                                - WITHDRAWAL
                                """)
                                .optional(),
                            parameterWithName("oAuthProvider").description("""
                                    (선택) oAuth 제공자
                                    형식
                                    - kakao
                                    - naver
                                    - google)
                                    """).optional(),
                            parameterWithName("startDate").description("""
                                    (선택) 가입 기간별 조회
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
                            parameterWithName("search").description(
                                    """
                                검색
                                - 이메일, 닉네임
                                """)
                                .optional()
                        )
                        .responseFields(
                            ArrayUtils.addAll(
                                pageResponseFormat(),
                                fieldWithPath("data.searchResults[].memberId").description("회원 ID"),
                                fieldWithPath("data.searchResults[].email").description("이메일"),
                                fieldWithPath("data.searchResults[].nickname").description("닉네임"),
                                fieldWithPath("data.searchResults[].oauthProvider").description(
                                    "OAuth 제공자"),
                                fieldWithPath("data.searchResults[].role").description("역할"),
                                fieldWithPath("data.searchResults[].status").description("상태"),
                                fieldWithPath("data.searchResults[].createdAt").description("""
                                    가입일
                                    - 형식 : yyyy-MM-dd
                                    """)
                            )
                        ).build()
                )));
    }
}
