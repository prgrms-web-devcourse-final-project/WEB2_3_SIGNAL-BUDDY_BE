package org.programmers.signalbuddyfinal.domain.admin.controller;

import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.programmers.signalbuddyfinal.global.support.RestDocsFormatGenerators.getTokenExample;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
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
import org.springframework.security.test.context.support.WithMockUser;
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
            .termId(1l).termTitle("test").termCategory(TermCategory.PRIVACY)
            .termContent("test").termVersionId(2l).agreementType(AgreementType.REQUIRED)
            .version("1.0.1").effectiveStartDate(LocalDate.of(2025, 1, 14))
            .effectiveEndDate(LocalDate.of(2025, 1, 15))
            .build();

        ResponseEntity<ApiResponse<AdminTermResponse>> response = ResponseEntity.ok(
            ApiResponse.createSuccess(adminTermResponse));
        when(adminTermService.getDetailTerm(any(), any())).thenReturn(response);

        mockMvc.perform(get("/api/admin/terms/{termId}", 1)
                .with(csrf())
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
                            .build()
                    )
                )
            );
    }
}
