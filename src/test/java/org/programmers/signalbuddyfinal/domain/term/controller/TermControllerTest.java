package org.programmers.signalbuddyfinal.domain.term.controller;

import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.programmers.signalbuddyfinal.domain.term.dto.TermResponse;
import org.programmers.signalbuddyfinal.domain.term.entity.enums.TermCategory;
import org.programmers.signalbuddyfinal.domain.term.service.TermService;
import org.programmers.signalbuddyfinal.global.response.ApiResponse;
import org.programmers.signalbuddyfinal.global.support.ControllerTest;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@WebMvcTest(TermController.class)
class TermControllerTest extends ControllerTest {

    @MockitoBean
    private TermService termService;

    private String tag = "Term API";

    @DisplayName("사용자가 회원가입할 때, 약관 조회")
    @Test
    void successGetDetailTermForMember() throws Exception {

        TermResponse termResponse = TermResponse.builder()
            .termVersionId(1l)
            .termId(2l)
            .content("testTerm")
            .build();

        ResponseEntity<ApiResponse<TermResponse>> response = ResponseEntity.ok(ApiResponse.createSuccess(termResponse));
        when(termService.getTerm(any(TermCategory.class))).thenReturn(response);

        mockMvc.perform(get("/api/terms")
            .param("category", "PRIVACY"))
            .andExpect(status().isOk())
            .andDo(document("사용자 약관 조회",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    resource(
                        ResourceSnippetParameters.builder()
                            .tag(tag)
                            .summary("사용자 약관 조회")
                            .build()
                    )
                )
            );
    }
}
