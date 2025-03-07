package org.programmers.signalbuddyfinal.domain.admin.controller;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestPartFields;
import static org.springframework.restdocs.request.RequestDocumentation.partWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParts;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.programmers.signalbuddyfinal.domain.admin.dto.AdminJoinRequest;
import org.programmers.signalbuddyfinal.domain.admin.service.AdminService;
import org.programmers.signalbuddyfinal.global.support.ControllerTest;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@WebMvcTest(AdminController.class)
class AdminControllerTest extends ControllerTest {

    @MockitoBean
    private AdminService adminService;
    private final String tag = "Admin API";

    @DisplayName("관리자 회원가입 성공")
    @Test
    void successJoin() throws Exception {
        // given
        AdminJoinRequest adminJoinRequest = AdminJoinRequest.builder()
            .email("newtest@test.com")
            .password("1234")
            .nickname("newnickname")
            .build();

        MockMultipartFile file = new MockMultipartFile(
            "profileImageUrl",
            "image.png",
            "image/png",
            "test".getBytes()
        );

        MockMultipartFile requestPart = new MockMultipartFile(
            "adminJoinRequest", "",
            MediaType.APPLICATION_JSON_VALUE,
            objectMapper.writeValueAsBytes(adminJoinRequest)
        );

        // when, then
        mockMvc.perform(multipart("/api/admins/join")
                .file(requestPart)
                .file(file))
            .andExpect(status().isOk())
            .andDo(document("관리자 회원가입",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                resource(
                    ResourceSnippetParameters.builder()
                        .tag(tag)
                        .summary("관리자 회원가입")
                        .build()
                ),
                requestParts(
                    partWithName("profileImageUrl").description("프로필 이미지 파일"),
                    partWithName("adminJoinRequest").description("회원 가입 정보")
                ),
                requestPartFields("adminJoinRequest",
                    fieldWithPath("email").description("이메일"),
                    fieldWithPath("password").description("비밀번호"),
                    fieldWithPath("nickname").description("닉네임")
                )
            ));
    }
}
