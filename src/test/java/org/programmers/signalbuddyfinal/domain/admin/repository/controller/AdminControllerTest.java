package org.programmers.signalbuddyfinal.domain.admin.repository.controller;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.programmers.signalbuddyfinal.global.support.RestDocsFormatGenerators.commonResponseFormat;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.programmers.signalbuddyfinal.domain.admin.controller.AdminController;
import org.programmers.signalbuddyfinal.domain.admin.dto.AdminMemberResponse;
import org.programmers.signalbuddyfinal.domain.admin.dto.MemberFilterRequest;
import org.programmers.signalbuddyfinal.domain.admin.service.AdminService;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberRole;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberStatus;
import org.programmers.signalbuddyfinal.global.support.ControllerTest;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.shaded.com.github.dockerjava.core.MediaType;
import org.testcontainers.shaded.org.apache.commons.lang3.ArrayUtils;

import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.web.servlet.function.RequestPredicates.param;

@WebMvcTest(AdminController.class)
public class AdminControllerTest extends ControllerTest {

    private final String tag = "Admin API";
    private Pageable pageable;

    @MockitoBean
    private AdminService adminService;

    @DisplayName("회원 필터링 조회")
    @Test
    public void getAllFilteredMember() throws Exception {
        pageable = PageRequest.of(0, 10);

        final MemberFilterRequest filter = MemberFilterRequest.builder()
            .role(MemberRole.USER)
            .status(MemberStatus.ACTIVITY)
            .oAuthProvider("KAKAO")
            .startDate(LocalDateTime.of(2025, 1, 1, 0, 0,0))
            .endDate(LocalDateTime.of(2025, 2, 20, 0, 0,0))
            .ago(null)
            .build();

        final Page<AdminMemberResponse> memberResponses = new PageImpl<>(List.of(
            new AdminMemberResponse(1L, "user1@test.com", "User1", "KAKAO", MemberRole.USER,
                MemberStatus.ACTIVITY,
                LocalDateTime.of(2024, 1, 22, 0, 0, 0)),
            new AdminMemberResponse(2L, "user2@test.com", "User2", "KAKAO", MemberRole.USER,
                MemberStatus.ACTIVITY,
                LocalDateTime.of(2024, 1, 5, 0, 0, 0))
        ));
        given(adminService.getAllMemberWithFilter(any(Pageable.class), any(MemberFilterRequest.class)))
            .willReturn(memberResponses);


        // 아래 엔드포인트에 param을 넣고 응답값이 200인지 확인
        final ResultActions result = mockMvc.perform(
                get("/api/admin/members/filter").param("page", "0")
                    .param("size", "10")
                    .param("role", filter.getRole().toString())
                    .param("status", filter.getStatus().toString())
                    .param("oAuthProvider", filter.getOAuthProvider())
                    .param("startDate", filter.getStartDate().toLocalDate().toString())
                    .param("endDate", filter.getEndDate().toLocalDate().toString())
                    .param("ago", filter.getAgo() != null ? filter.getAgo().toString() : "")
                    .header("Accept", MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andDo(document("회원 필터링 조회", preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                resource(
                    ResourceSnippetParameters.builder()
                        .tag(tag)
                        .summary("회원 필터링 조회")
                        .description("회원 정보를 필터 기준으로 조회하는 API")
                        .queryParameters(
                            parameterWithName("page").description("페이지 번호"),
                            parameterWithName("size").description("페이지 크기"),
                            parameterWithName("role").description("회원 역할 (USER, ADMIN)"),
                            parameterWithName("status").description("회원 상태 (ACTIVITY, WITHDRAWAL)"),
                            parameterWithName("oAuthProvider").description("oAuth 제공자"),
                            parameterWithName("startDate").description("검색 시작일 (YYYY-MM-DD)"),
                            parameterWithName("endDate").description("검색 종료일 (YYYY-MM-DD)"),
                            parameterWithName("ago").description(
                                "최근 기간 필터링 (TODAY, THREE_DAYS, WEEK, MONTH, THREE_MONTH)")
                        )
                        .responseFields(
                            ArrayUtils.addAll(commonResponseFormat(),
                                fieldWithPath("id").type(JsonFieldType.NUMBER).description("회원 ID"),
                                fieldWithPath("data.email").type(JsonFieldType.STRING).description("이메일"),
                                fieldWithPath("data.nickname").type(JsonFieldType.STRING).description("닉네임"),
                                fieldWithPath("data.oauthProvider").type(JsonFieldType.STRING).description("oAuth 제공자"),
                                fieldWithPath("data.role").type(JsonFieldType.STRING).description("회원 역할"),
                                fieldWithPath("data.status").type(JsonFieldType.STRING).description("회원 상태"),
                                fieldWithPath("data.createdAt").type(JsonFieldType.STRING).description("가입 날짜 (ISO 8601)"),
                                fieldWithPath("data.page").type(JsonFieldType.NUMBER).description("현재 페이지 번호"),
                                fieldWithPath("data.totalPages").type(JsonFieldType.NUMBER).description("전체 페이지 수"),
                                fieldWithPath("data.totalElements").type(JsonFieldType.NUMBER).description("전체 회원 수")
                            )
                        )
                        .build()
                )
            ));
    }
}
