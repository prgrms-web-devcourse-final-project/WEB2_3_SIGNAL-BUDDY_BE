package org.programmers.signalbuddyfinal.domain.admin.controller;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.programmers.signalbuddyfinal.global.support.RestDocsFormatGenerators.pageResponseFormat;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
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
import org.programmers.signalbuddyfinal.domain.admin.dto.AdminPostItResponse;
import org.programmers.signalbuddyfinal.domain.admin.service.AdminPostItService;
import org.programmers.signalbuddyfinal.domain.postit.entity.Danger;
import org.programmers.signalbuddyfinal.global.dto.PageResponse;
import org.programmers.signalbuddyfinal.global.support.ControllerTest;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
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

}
