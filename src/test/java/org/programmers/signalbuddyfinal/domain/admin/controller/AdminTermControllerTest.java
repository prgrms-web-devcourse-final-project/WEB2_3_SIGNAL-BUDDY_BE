package org.programmers.signalbuddyfinal.domain.admin.controller;

import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.programmers.signalbuddyfinal.global.support.RestDocsFormatGenerators.commonResponseFormat;
import static org.programmers.signalbuddyfinal.global.support.RestDocsFormatGenerators.getTokenExample;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.programmers.signalbuddyfinal.domain.admin.dto.AdminTermResponse;
import org.programmers.signalbuddyfinal.domain.admin.dto.CreateTermRequest;
import org.programmers.signalbuddyfinal.domain.admin.service.AdminTermService;
import org.programmers.signalbuddyfinal.domain.term.entity.enums.AgreementType;
import org.programmers.signalbuddyfinal.domain.term.entity.enums.TermCategory;
import org.programmers.signalbuddyfinal.global.anotation.WithMockCustomUser;
import org.programmers.signalbuddyfinal.global.response.ApiResponse;
import org.programmers.signalbuddyfinal.global.support.ControllerTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AdminTermController.class)
class AdminTermControllerTest extends ControllerTest {

    @MockitoBean
    private AdminTermService adminTermService;
    @Autowired
    private MockMvc mockMvc;

    private String tag = "Admin API";

    @DisplayName("약관 생성 성공")
    @Test
    @WithMockCustomUser(roleType = "ROLE_ADMIN")
    void successCreateTerm() throws Exception {

        CreateTermRequest createTermRequest = CreateTermRequest.builder()
            .title("test").version("1.0.0").agreementType(AgreementType.REQUIRED)
            .category(TermCategory.PRIVACY).content("test")
            .effectiveStartDate(LocalDate.of(2025, 1, 14))
            .effectiveEndDate(LocalDate.of(2025, 1, 15))
            .build();
        ResponseEntity<ApiResponse<Object>> response = ResponseEntity.ok(
            ApiResponse.createSuccessWithNoData());
        when(adminTermService.registerTerm(any(CreateTermRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/admin/terms")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.registerModule(new JavaTimeModule())
                    .writeValueAsString(createTermRequest))
                .header(HttpHeaders.AUTHORIZATION, "Bearer adminToken"))
            .andExpect(status().isOk())
            .andDo(document("관리자 약관 생성",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    resource(
                        ResourceSnippetParameters.builder()
                            .tag(tag)
                            .summary("관리자 약관 생성")
                            .requestFields(
                                fieldWithPath("title").description("약관 제목"),
                                fieldWithPath("version").description("약관 버전"),
                                fieldWithPath("agreementType").description("동의 타입(ex.REQUIRED or OPTIONAL)"),
                                fieldWithPath("category").description("약관 종류(ex.개인정보약관:PRIVACY, 이용약관:USE)"),
                                fieldWithPath("content").description("약관 내용"),
                                fieldWithPath("effectiveStartDate").description("시행 시작일"),
                                fieldWithPath("effectiveEndDate").description("시행 종료일")
                            )
                            .build()
                    )
                )
            );

    }

    @DisplayName("관리자 약관 상세 조회")
    @Test
    @WithMockCustomUser(roleType = "ROLE_ADMIN")
    void successGetDetailTermForAdmin() throws Exception {
        AdminTermResponse adminTermResponse = AdminTermResponse.builder()
            .termId(1l).title("test").category(TermCategory.PRIVACY)
            .content("test").termVersionId(2l).agreementType(AgreementType.REQUIRED)
            .version("1.0.1").effectiveStartDate(LocalDate.of(2025, 1, 14))
            .effectiveEndDate(LocalDate.of(2025, 1, 15))
            .build();

        ResponseEntity<ApiResponse<AdminTermResponse>> response = ResponseEntity.ok(
            ApiResponse.createSuccess(adminTermResponse));
        when(adminTermService.getDetailTerm(any(), any())).thenReturn(response);

        mockMvc.perform(get("/api/admin/terms/{termId}", 1)
                .param("termVersionId", "2")
                .header(HttpHeaders.AUTHORIZATION, getTokenExample()))
            .andExpect(status().isOk())
            .andDo(document("관리자 약관 상세 조회",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    resource(
                        ResourceSnippetParameters.builder()
                            .tag(tag)
                            .summary("관리자 약관 상세 조회")
                            .pathParameters(
                                parameterWithName("termId").description("term Id")
                            )
                            .queryParameters(
                                parameterWithName("termVersionId").description("termVersion Id")
                            )
                            .responseFields(
                                ArrayUtils.addAll(
                                    commonResponseFormat(),
                                    fieldWithPath("data.termId").description("term Id"),
                                    fieldWithPath("data.termVersionId").description("termVersion Id"),
                                    fieldWithPath("data.title").description("약관 제목"),
                                    fieldWithPath("data.agreementType").description("동의 타입(ex.REQUIRED or OPTIONAL)"),
                                    fieldWithPath("data.category").description("약관 종류(ex.개인정보약관:PRIVACY, 이용약관:USE)"),
                                    fieldWithPath("data.version").description("약관 버전"),
                                    fieldWithPath("data.content").description("약관 내용"),
                                    fieldWithPath("data.effectiveStartDate").description("시행 시작일"),
                                    fieldWithPath("data.effectiveEndDate").description("시행 종료일")
                                )
                            )
                            .build()
                    )
                )
            );
    }
}