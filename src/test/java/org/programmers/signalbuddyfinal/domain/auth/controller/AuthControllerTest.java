package org.programmers.signalbuddyfinal.domain.auth.controller;

import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.programmers.signalbuddyfinal.domain.auth.dto.LoginRequest;
import org.programmers.signalbuddyfinal.domain.auth.service.AuthService;
import org.programmers.signalbuddyfinal.global.response.ApiResponse;
import org.programmers.signalbuddyfinal.global.support.ControllerTest;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@WebMvcTest(AuthController.class)
public class AuthControllerTest extends ControllerTest {

    @MockitoBean
    private AuthService authService;

    @DisplayName("로그인 성공")
    @Test
    void successLogin() throws Exception {

        //given
        String testAccessToken = "testAccessToken";
        String testRefreshToken = "testRefreshToken";

        ApiResponse<?> apiResponse = ApiResponse.createSuccessWithNoData();
        ResponseEntity<ApiResponse<?>> response = ResponseEntity.ok()
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
                            .summary("로그인")
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

        ApiResponse<?> apiResponse = ApiResponse.createSuccessWithNoData();
        ResponseEntity<ApiResponse<?>> response = ResponseEntity.ok()
                .header("Set-Cookie","refresh-token=" + newRefreshToken)
                .header("Authorization", "Bearer " + newAccessToken)
                .body(apiResponse);

        when(authService.reissue(anyString())).thenReturn(response);

        //when, then
        mockMvc.perform(post("/api/auth/reissue")
                .cookie(new Cookie("refresh-token", newRefreshToken)))
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
}
