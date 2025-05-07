package org.programmers.signalbuddyfinal.domain.auth.controller;

import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.hamcrest.Matchers.containsString;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.programmers.signalbuddyfinal.domain.auth.dto.EmailRequest;
import org.programmers.signalbuddyfinal.domain.auth.dto.LoginRequest;
import org.programmers.signalbuddyfinal.domain.auth.dto.LoginResponse;
import org.programmers.signalbuddyfinal.domain.auth.dto.LogoutResponse;
import org.programmers.signalbuddyfinal.domain.auth.dto.ReissueResponse;
import org.programmers.signalbuddyfinal.domain.auth.dto.SocialLoginRequest;
import org.programmers.signalbuddyfinal.domain.auth.dto.VerifyCodeRequest;
import org.programmers.signalbuddyfinal.domain.auth.entity.Purpose;
import org.programmers.signalbuddyfinal.domain.auth.service.AuthService;
import org.programmers.signalbuddyfinal.domain.auth.service.EmailService;
import org.programmers.signalbuddyfinal.domain.member.dto.MemberResponse;
import org.programmers.signalbuddyfinal.domain.member.entity.Member;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberRole;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberStatus;
import org.programmers.signalbuddyfinal.domain.member.mapper.MemberMapper;
import org.programmers.signalbuddyfinal.domain.notification.service.FcmService;
import org.programmers.signalbuddyfinal.domain.social.entity.Provider;
import org.programmers.signalbuddyfinal.global.response.ApiResponse;
import org.programmers.signalbuddyfinal.global.support.ControllerTest;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
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

    @MockitoBean
    private FcmService fcmService;

    private String tag = "Auth API";
    private String deviceToken = "deviceToken";
    private HttpHeaders headers = new HttpHeaders();
    private MemberResponse memberResponse;
    private LoginResponse loginResponse;
    private Member member;

    @BeforeEach
    void setUp() {
        // Set-Cookie 정책을 작성해주어야 함.
        headers.set("Set-Cookie", "refresh-token=refreshTokenValue; Path=/; HttpOnly");
        headers.set("Authorization", "Bearer "+"accessToken");

        member = Member.builder()
            .memberId(1l)
            .email("loginMember@test.com")
            .nickname("로그인 테스트용")
            .profileImageUrl("프로필 경로")
            .role(MemberRole.USER)
            .memberStatus(MemberStatus.ACTIVITY)
            .password("password")
            .build();

        memberResponse = MemberMapper.INSTANCE.toDto(member);
        loginResponse = LoginResponse.success(headers, memberResponse);
    }

    @DisplayName("기본 로그인 성공")
    @Test
    void successLogin() throws Exception {

        //given
        LoginRequest loginRequest = new LoginRequest(member.getEmail(), member.getPassword());

        doNothing().when(fcmService).loginToken(anyString());
        when(authService.login(anyString(), any(LoginRequest.class))).thenReturn(loginResponse);

        //when, then
        mockMvc.perform(post("/api/auth/login")
                .cookie(new Cookie("device-token", deviceToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andExpect(header().string("Authorization", loginResponse.getHttpHeaders().getFirst("Authorization")))
            .andExpect(header().string("Set-Cookie", loginResponse.getHttpHeaders().getFirst("Set-Cookie")))
            .andDo(document("기본 로그인",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    resource(
                        ResourceSnippetParameters.builder()
                            .tag(tag)
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
        SocialLoginRequest socialLoginRequest = new SocialLoginRequest(Provider.GOOGLE, "1234");

        doNothing().when(fcmService).loginToken(anyString());
        when(authService.socialLogin(anyString(), any(SocialLoginRequest.class))).thenReturn(loginResponse);

        //when, then
        mockMvc.perform(post("/api/auth/social-login")
                .contentType(MediaType.APPLICATION_JSON)
                .cookie(new Cookie("device-token", deviceToken))
                .content(objectMapper.writeValueAsString(socialLoginRequest)))
            .andExpect(status().isOk())
            .andExpect(header().string("Authorization", loginResponse.getHttpHeaders().getFirst("Authorization")))
            .andExpect(header().string("Set-Cookie", loginResponse.getHttpHeaders().getFirst("Set-Cookie")))
            .andDo(document("소셜 로그인",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    resource(
                        ResourceSnippetParameters.builder()
                            .tag(tag)
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
        HttpHeaders afterReissueHeaders = new HttpHeaders();
        afterReissueHeaders.set("Set-Cookie", "refresh-token=newRefreshToken; Path=/; HttpOnly");
        afterReissueHeaders.set("Authorization", "newAccessToken");
        ReissueResponse reissueResponse = new ReissueResponse(afterReissueHeaders);

        when(authService.reissue(anyString(), anyString())).thenReturn(reissueResponse);

        //when, then
        mockMvc.perform(post("/api/auth/reissue")
                .contentType(MediaType.APPLICATION_JSON)
                .cookie(new Cookie("refresh-token", "refreshToken"))
                .header("Authorization", "Bearer " + headers.get("Authorization")))
            .andExpect(status().isOk())
            .andExpect(header().string("Authorization", reissueResponse.getHttpHeaders().getFirst("Authorization")))
            .andExpect(header().string("Set-Cookie", reissueResponse.getHttpHeaders().getFirst("Set-Cookie")))
            .andDo(document("액세스 토큰 및 리프레시 토큰 재발행",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    resource(
                        ResourceSnippetParameters.builder()
                            .tag(tag)
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
        EmailRequest emailRequest = new EmailRequest(member.getEmail());
        doNothing().when(emailService).sendEmail(anyString());

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
                        .tag(tag)
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
        VerifyCodeRequest verifyCodeRequest = new VerifyCodeRequest(Purpose.NEW_PASSWORD, member.getEmail(), "123456");

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
                        .tag(tag)
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

    @DisplayName("로그아웃")
    @Test
    void successLogout() throws Exception {
        //given
        HttpHeaders afterLogoutHeaders = new HttpHeaders();
        afterLogoutHeaders.add(HttpHeaders.SET_COOKIE, "refresh-token=deletedToken; Max-Age=0; Path=/; HttpOnly");
        LogoutResponse logoutResponse = new LogoutResponse(afterLogoutHeaders);

        doNothing().when(fcmService).logoutToken(anyString());
        when(authService.logout(anyString(), anyString(), anyString())).thenReturn(logoutResponse);

        //when, then
        mockMvc.perform(post("/api/auth/logout")
                .cookie(new Cookie("device-token", deviceToken))
                .cookie(new Cookie("refresh-token", loginResponse.getHttpHeaders().getFirst("Set-Cookie")))
                .header("Authorization", "Bearer " + loginResponse.getHttpHeaders().getFirst("Authorization")))
            .andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Max-Age=0")))
            .andDo(document("로그아웃",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    resource(
                        ResourceSnippetParameters.builder()
                            .tag(tag)
                            .summary("로그아웃")
                            .build()
                    )
                )
            );
    }
}
