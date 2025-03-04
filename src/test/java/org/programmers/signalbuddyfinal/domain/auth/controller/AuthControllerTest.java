package org.programmers.signalbuddyfinal.domain.auth.controller;

import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.programmers.signalbuddyfinal.domain.auth.dto.EmailRequest;
import org.programmers.signalbuddyfinal.domain.auth.dto.LoginRequest;
import org.programmers.signalbuddyfinal.domain.auth.dto.SocialLoginRequest;
import org.programmers.signalbuddyfinal.domain.auth.dto.VerifyCodeRequest;
import org.programmers.signalbuddyfinal.domain.auth.entity.Purpose;
import org.programmers.signalbuddyfinal.domain.auth.service.AuthService;
import org.programmers.signalbuddyfinal.domain.auth.service.EmailService;
import org.programmers.signalbuddyfinal.domain.member.dto.MemberResponse;
import org.programmers.signalbuddyfinal.domain.social.entity.Provider;
import org.programmers.signalbuddyfinal.global.response.ApiResponse;
import org.programmers.signalbuddyfinal.global.support.ControllerTest;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@WebMvcTest(AuthController.class)
class AuthControllerTest extends ControllerTest {

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private EmailService emailService;

    @DisplayName("기본 로그인 성공")
    @Test
    void successLogin() throws Exception {

        //given
        String testAccessToken = "testAccessToken";
        String testRefreshToken = "testRefreshToken";

        ApiResponse apiResponse = ApiResponse.createSuccessWithNoData();
        ResponseEntity<ApiResponse<MemberResponse>> response = ResponseEntity.ok()
            .header("Set-Cookie","refresh-token=" + testRefreshToken)
            .header("Authorization", "Bearer " + testAccessToken)
            .body(apiResponse);

        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        //when, then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":\"test@test.com\", \"password\":\"password\"}"))
            .andExpect(status().isOk())
            .andExpect(header().string("Authorization", "Bearer " + testAccessToken))
            .andExpect(header().string("Set-Cookie", "refresh-token="+testRefreshToken))
            .andExpect(cookie().value("refresh-token", testRefreshToken))
            .andDo(document("기본 로그인",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    resource(
                        ResourceSnippetParameters.builder()
                            .tag("Auth API")
                            .summary("기본 로그인")
                            .build()
                    )
                )
            );
    }

    @DisplayName("소셜 로그인 성공")
    @Test
    void successSocialLogin() throws Exception {

        //given
        String testAccessToken = "testAccessToken";
        String testRefreshToken = "testRefreshToken";
        SocialLoginRequest socialLoginRequest = new SocialLoginRequest(Provider.GOOGLE, "1234");

        ApiResponse apiResponse = ApiResponse.createSuccessWithNoData();
        ResponseEntity<ApiResponse<MemberResponse>> response = ResponseEntity.ok()
            .header("Set-Cookie","refresh-token=" + testRefreshToken)
            .header("Authorization", "Bearer " + testAccessToken)
            .body(apiResponse);

        when(authService.socialLogin(any(SocialLoginRequest.class))).thenReturn(response);

        //when, then
        mockMvc.perform(post("/api/auth/social-login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(socialLoginRequest)))
            .andExpect(status().isOk())
            .andExpect(header().string("Authorization", "Bearer " + testAccessToken))
            .andExpect(header().string("Set-Cookie", "refresh-token="+testRefreshToken))
            .andExpect(cookie().value("refresh-token", testRefreshToken))
            .andDo(document("소셜 로그인",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    resource(
                        ResourceSnippetParameters.builder()
                            .tag("Auth API")
                            .summary("소셜 로그인")
                            .build()
                    )
                )
            );
    }

    @DisplayName("액세스 토큰과 리프레시 토큰 재발행")
    @Test
    void successReissueTokens() throws Exception {

        //given
        String newRefreshToken = "newRefreshToken";
        String newAccessToken = "newAccessToken";

        ApiResponse apiResponse = ApiResponse.createSuccessWithNoData();
        ResponseEntity<ApiResponse<Object>> response = ResponseEntity.ok()
                .header("Set-Cookie","refresh-token=" + newRefreshToken)
                .header("Authorization", "Bearer " + newAccessToken)
                .body(apiResponse);

        when(authService.reissue(anyString(), anyString())).thenReturn(response);

        //when, then
        mockMvc.perform(post("/api/auth/reissue")
                .cookie(new Cookie("refresh-token", newRefreshToken))
                .header("Authorization", "Bearer " + newAccessToken))
            .andExpect(status().isOk())
            .andExpect(header().string("Authorization", "Bearer " + newAccessToken))
            .andExpect(header().string("Set-Cookie", "refresh-token="+newRefreshToken))
            .andExpect(cookie().value("refresh-token", newRefreshToken))
            .andDo(document("액세스 토큰 및 리프레시 토큰 재발행",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    resource(
                        ResourceSnippetParameters.builder()
                            .tag("Auth API")
                            .summary("토큰 재발행")
                            .build()
                    )
                )
            );
    }

    @DisplayName("인증 코드 이메일 전송")
    @Test
    void sendAuthenticationCode() throws Exception {
        // given
        String email = "test@test.com";
        EmailRequest emailRequest = new EmailRequest(email);

        ApiResponse<Object> apiResponse = ApiResponse.createSuccessWithNoData();
        doNothing().when(emailService).sendEmail(any(EmailRequest.class));

        //when, then
        mockMvc.perform(post("/api/auth/auth-code")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emailRequest)))
            .andExpect(status().isOk())
            .andDo(document("인증 코드 이메일 전송",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                resource(
                    ResourceSnippetParameters.builder()
                        .tag("Auth API")
                        .summary("인증 코드 이메일 전송")
                        .requestFields(
                            fieldWithPath("email")
                                .type(JsonFieldType.STRING)
                                .description("인증 코드를 받고자 하는 이메일")
                        ).build())));
    }

    @DisplayName("인증 코드 검증")
    @Test
    void verifyAuthenticationCode() throws Exception {
        // given
        VerifyCodeRequest verifyCodeRequest = new VerifyCodeRequest(Purpose.NEW_PASSWORD, "test@test.com", "123456");

        ApiResponse<Object> apiResponse = ApiResponse.createSuccessWithNoData();
        ResponseEntity<ApiResponse<Object>> responseEntity = ResponseEntity.ok().body(apiResponse);
        when(emailService.verifyCode(any(VerifyCodeRequest.class))).thenReturn(responseEntity);

        //when, then
        mockMvc.perform(post("/api/auth/verify-code")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(verifyCodeRequest)))
            .andExpect(status().isOk())
            .andDo(document("인증 코드 검증",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                resource(
                    ResourceSnippetParameters.builder()
                        .tag("Auth API")
                        .summary("인증 코드 검증")
                        .requestFields(
                            fieldWithPath("purpose")
                                .type(JsonFieldType.STRING)
                                .description("인증하는 목적(비밀번호 재설정, 계정 복귀)"),
                            fieldWithPath("email")
                                .type(JsonFieldType.STRING)
                                .description("인증 코드를 받고자 하는 이메일"),
                            fieldWithPath("code")
                                .type(JsonFieldType.STRING)
                                .description("인증 코드")
                        ).build())));
    }

}
