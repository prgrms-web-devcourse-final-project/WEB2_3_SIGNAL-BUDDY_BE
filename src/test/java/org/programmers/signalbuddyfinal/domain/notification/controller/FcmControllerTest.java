package org.programmers.signalbuddyfinal.domain.notification.controller;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.programmers.signalbuddyfinal.global.support.RestDocsFormatGenerators.getTokenExample;
import static org.programmers.signalbuddyfinal.global.support.RestDocsFormatGenerators.jwtFormat;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.programmers.signalbuddyfinal.domain.notification.dto.FcmTokenRequest;
import org.programmers.signalbuddyfinal.domain.notification.service.FcmService;
import org.programmers.signalbuddyfinal.global.anotation.WithMockCustomUser;
import org.programmers.signalbuddyfinal.global.dto.CustomUser2Member;
import org.programmers.signalbuddyfinal.global.support.ControllerTest;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest(FcmController.class)
class FcmControllerTest extends ControllerTest {
    
    @MockitoBean
    private FcmService fcmService;
    
    private String tag = "FCM Notification Controller";

    @DisplayName("디바이스 토큰을 사용자와 매핑하여 저장한다.")
    @Test
    @WithMockCustomUser
    void registerToken() throws Exception {
        // Given
        FcmTokenRequest request = new FcmTokenRequest("test token");
        doNothing().when(fcmService).registerToken(anyString(), any(CustomUser2Member.class));
        
        // When
        ResultActions result = mockMvc.perform(
            post("/api/fcm/token")
                .header(HttpHeaders.AUTHORIZATION, getTokenExample())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        );
        
        result.andExpect(status().isCreated())
            .andDo(
                document(
                    "디바이스 토큰 저장",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    resource(
                        ResourceSnippetParameters.builder()
                            .tag(tag)
                            .summary("디바이스 토큰 저장")
                            .requestHeaders(
                                jwtFormat()
                            )
                            .requestFields(
                                fieldWithPath("deviceToken").type(JsonFieldType.STRING)
                                    .description("FCM에서 받은 디바이스 토큰")
                            )
                            .build()
                    )
                )
            );
    }

    @DisplayName("디바이스 토큰을 수정한다.")
    @Test
    @WithMockCustomUser
    void updateToken() throws Exception {
        // Given
        FcmTokenRequest request = new FcmTokenRequest("test update token");
        doNothing().when(fcmService).updateToken(anyString(), any(CustomUser2Member.class));

        // When
        ResultActions result = mockMvc.perform(
            patch("/api/fcm/token")
                .header(HttpHeaders.AUTHORIZATION, getTokenExample())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        );

        result.andExpect(status().isOk())
            .andDo(
                document(
                    "디바이스 토큰 수정",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    resource(
                        ResourceSnippetParameters.builder()
                            .tag(tag)
                            .summary("디바이스 토큰 수정")
                            .requestHeaders(
                                jwtFormat()
                            )
                            .requestFields(
                                fieldWithPath("deviceToken").type(JsonFieldType.STRING)
                                    .description("FCM에서 받은 새로운 디바이스 토큰")
                            )
                            .build()
                    )
                )
            );
    }

    @DisplayName("해당 사용자의 디바이스 토큰을 삭제한다.")
    @Test
    @WithMockCustomUser
    void deleteToken() throws Exception {
        // Given
        doNothing().when(fcmService).deleteToken(any(CustomUser2Member.class));

        // When
        ResultActions result = mockMvc.perform(
            delete("/api/fcm/token")
                .header(HttpHeaders.AUTHORIZATION, getTokenExample())
        );

        result.andExpect(status().isOk())
            .andDo(
                document(
                    "디바이스 토큰 삭제",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    resource(
                        ResourceSnippetParameters.builder()
                            .tag(tag)
                            .summary("디바이스 토큰 삭제")
                            .requestHeaders(
                                jwtFormat()
                            )
                            .build()
                    )
                )
            );
    }
}