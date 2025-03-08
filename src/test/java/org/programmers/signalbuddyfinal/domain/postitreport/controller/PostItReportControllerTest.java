package org.programmers.signalbuddyfinal.domain.postitreport.controller;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.programmers.signalbuddyfinal.global.support.RestDocsFormatGenerators.getTokenExample;
import static org.programmers.signalbuddyfinal.global.support.RestDocsFormatGenerators.jwtFormat;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.SimpleType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.programmers.signalbuddyfinal.domain.postit_report.controller.PostItReportController;
import org.programmers.signalbuddyfinal.domain.postit_report.service.PostItReportService;
import org.programmers.signalbuddyfinal.global.anotation.WithMockCustomUser;
import org.programmers.signalbuddyfinal.global.dto.CustomUser2Member;
import org.programmers.signalbuddyfinal.global.support.ControllerTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest(PostItReportController.class)
public class PostItReportControllerTest extends ControllerTest {

    private final String tag = "PostItReport API";

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private PostItReportService postItReportService;

    @DisplayName("포스트잇 신고")
    @Test
    @WithMockCustomUser
    public void addPostItReport() throws Exception {

        Long postitId = 1L;

        doNothing().when(postItReportService).addReport(anyLong(), any(CustomUser2Member.class));

        final ResultActions result = mockMvc.perform(
            post("/api/postit-report/add/{postitId}", postitId)
                .with(csrf())
                .header(HttpHeaders.AUTHORIZATION, getTokenExample())
        );

        result.andExpect(status().isOk())
            .andDo(
                document(
                    "포스트잇 신고 등록",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    resource(
                        ResourceSnippetParameters.builder()
                            .tag(tag)
                            .summary("포스트잇 신고하는 API")
                            .requestHeaders(
                                jwtFormat()
                            )
                            .pathParameters(
                                parameterWithName("postitId").type(SimpleType.NUMBER)
                                    .description("신고할 포스트잇 ID")
                            )
                            .build()
                    )
                )
            );
    }

    @DisplayName("포스트잇 신고 취소")
    @Test
    @WithMockCustomUser
    public void cancelPostItReport() throws Exception {

        Long postitId = 1L;

        doNothing().when(postItReportService).cancelReport(anyLong(), any(CustomUser2Member.class));

        final ResultActions result = mockMvc.perform(
            delete("/api/postit-report/cancel/{postitId}", postitId)
                .with(csrf())
                .header(HttpHeaders.AUTHORIZATION, getTokenExample())
        );

        result.andExpect(status().isOk())
            .andDo(
                document(
                    "포스트잇 신고 취소",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    resource(
                        ResourceSnippetParameters.builder()
                            .tag(tag)
                            .summary("포스트잇 신고를 취소하는 API")
                            .requestHeaders(
                                jwtFormat()
                            )
                            .pathParameters(
                                parameterWithName("postitId").type(SimpleType.NUMBER)
                                    .description("신고 취소할 포스트잇 ID")
                            )
                            .build()
                    )
                )
            );
    }
}
