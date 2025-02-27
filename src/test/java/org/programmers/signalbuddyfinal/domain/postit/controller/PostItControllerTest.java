package org.programmers.signalbuddyfinal.domain.postit.controller;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.programmers.signalbuddyfinal.global.support.RestDocsFormatGenerators.commonResponseFormat;
import static org.programmers.signalbuddyfinal.global.support.RestDocsFormatGenerators.getMockImageFile;
import static org.programmers.signalbuddyfinal.global.support.RestDocsFormatGenerators.getTokenExample;
import static org.programmers.signalbuddyfinal.global.support.RestDocsFormatGenerators.jwtFormat;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestPartFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.partWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.requestParts;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.SimpleType;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberRole;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberStatus;
import org.programmers.signalbuddyfinal.domain.postit.dto.PostItCreateRequest;
import org.programmers.signalbuddyfinal.domain.postit.dto.PostItRequest;
import org.programmers.signalbuddyfinal.domain.postit.dto.PostItResponse;
import org.programmers.signalbuddyfinal.domain.postit.entity.Danger;
import org.programmers.signalbuddyfinal.domain.postit.service.PostItService;
import org.programmers.signalbuddyfinal.global.anotation.WithMockCustomUser;
import org.programmers.signalbuddyfinal.global.dto.CustomUser2Member;
import org.programmers.signalbuddyfinal.global.security.basic.CustomUserDetails;
import org.programmers.signalbuddyfinal.global.support.ControllerTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.multipart.MultipartFile;
import org.testcontainers.shaded.org.apache.commons.lang3.ArrayUtils;

@WebMvcTest(PostItController.class)
public class PostItControllerTest extends ControllerTest {

    @MockitoBean
    private PostItService postItService;

    private final String tag = "PostIt API";
    private final String imageFormName = "imageFile";

    @Autowired
    private MockMvc mockMvc;

    @DisplayName("포스트잇 등록")
    @Test
    @WithMockCustomUser
    public void createPostIt() throws Exception {

        PostItCreateRequest request = PostItCreateRequest.builder()
            .danger(Danger.DANGER)
            .lng(127.1171114)
            .lat(37.5206868)
            .subject("포스트잇 제목")
            .content("포스트잇 내용")
            .createDate(null)
            .build();
        MockMultipartFile requestPart = new MockMultipartFile(
            "request", "",
            MediaType.APPLICATION_JSON_VALUE,
            objectMapper.writeValueAsBytes(request)
        );
        MockMultipartFile imageFile = getMockImageFile(imageFormName);

        CustomUserDetails customUserDetails = new CustomUserDetails(1L, "user1@gmamil.com", "1234",
            "url2.jpg", "user1", MemberRole.USER, MemberStatus.ACTIVITY);
        CustomUser2Member user = new CustomUser2Member(customUserDetails); // 생성자 호출
        PostItResponse postItResponse = createResponse(user.getMemberId());

        given(postItService.createPostIt(any(PostItCreateRequest.class), any(MultipartFile.class),
            any(CustomUser2Member.class))).willReturn(postItResponse);

        final ResultActions result = mockMvc.perform(
            multipart("/api/postits")
                .file(imageFile)
                .file(requestPart)
                .with(csrf())
                .header(HttpHeaders.AUTHORIZATION, getTokenExample())
                .contentType(MediaType.MULTIPART_FORM_DATA)
        );

        result.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andDo(
                document(
                    "포스트잇 등록",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    resource(
                        ResourceSnippetParameters.builder()
                            .tag(tag)
                            .description("포스트잇을 등록하는 API")
                            .build()
                    ),
                    requestParts(
                        partWithName("request")
                            .description("포스트잇 등록 요청 JSON 데이터"),
                        partWithName("imageFile")
                            .description("첨부 이미지 파일 (필수)")
                    ),
                    requestPartFields(
                        "request",
                        fieldWithPath("danger")
                            .type(JsonFieldType.STRING)
                            .description("""
                                포스트잇 위험도
                                - DANGER : 위험
                                - WARNING : 주의
                                - NOTICE : 알림
                                """),
                        fieldWithPath("lat")
                            .type(JsonFieldType.NUMBER)
                            .description("등록 위도"),
                        fieldWithPath("lng")
                            .type(JsonFieldType.NUMBER)
                            .description("등록 경도"),
                        fieldWithPath("subject")
                            .type(JsonFieldType.STRING)
                            .description("포스트잇 제목"),
                        fieldWithPath("content")
                            .type(JsonFieldType.STRING)
                            .description("포스트잇 내용"),
                        fieldWithPath("createDate")
                            .type(JsonFieldType.STRING)
                            .description("""
                                포스트잇 등록일
                                형식 : YYYY-MM-dd HH-mm
                                """).optional()
                    ),
                    responseFields(
                        ArrayUtils.addAll(
                            commonResponseFormat(),
                            fieldWithPath("data.postitId")
                                .type(JsonFieldType.NUMBER)
                                .description("생성된 포스트잇 ID"),
                            fieldWithPath("data.danger")
                                .type(JsonFieldType.STRING)
                                .description("포스트잇 위험도"),
                            fieldWithPath("data.lat")
                                .type(JsonFieldType.NUMBER)
                                .description("등록 위도"),
                            fieldWithPath("data.lng")
                                .type(JsonFieldType.NUMBER)
                                .description("등록 경도"),
                            fieldWithPath("data.subject")
                                .type(JsonFieldType.STRING)
                                .description("포스트잇 제목"),
                            fieldWithPath("data.content")
                                .type(JsonFieldType.STRING)
                                .description("포스트잇 내용"),
                            fieldWithPath("data.imageUrl")
                                .type(JsonFieldType.STRING)
                                .description("등록된 이미지 URL"),
                            fieldWithPath("data.expiryDate")
                                .type(JsonFieldType.STRING)
                                .description("""
                                    포스트잇 삭제 예정일
                                    형식 : YYYY-MM-dd HH-mm
                                    """),
                            fieldWithPath("data.createDate")
                                .type(JsonFieldType.STRING)
                                .description("""
                                    포스트잇 등록일
                                    형식 : YYYY-MM-dd HH-mm
                                    """),
                            fieldWithPath("data.memberId")
                                .type(JsonFieldType.NUMBER)
                                .description("등록한 사용자 ID")
                        )
                    )
                )
            );
    }

    @Test
    @DisplayName("포스트잇 수정")
    @WithMockCustomUser
    public void updatePostIt() throws Exception {

        PostItRequest request = PostItRequest.builder()
            .danger(Danger.DANGER)
            .lng(127.1171114)
            .lat(37.5206868)
            .subject("수정 포스트잇 제목")
            .content("수정 포스트잇 내용")
            .imageUrl("https://beforImage.com/beforeImageUrl")
            .build();
        MockMultipartFile requestPart = new MockMultipartFile(
            "request", "",
            MediaType.APPLICATION_JSON_VALUE,
            objectMapper.writeValueAsBytes(request)
        );
        MockMultipartFile imageFile = getMockImageFile(imageFormName);

        CustomUserDetails customUserDetails = new CustomUserDetails(1L, "user1@gmamil.com", "1234",
            "url2.jpg", "user1", MemberRole.USER, MemberStatus.ACTIVITY);
        CustomUser2Member user = new CustomUser2Member(customUserDetails);
        PostItResponse postItResponse = createResponse(user.getMemberId());

        given(postItService.updatePostIt(anyLong(), any(PostItRequest.class),
            any(MultipartFile.class),
            any(CustomUser2Member.class))).willReturn(postItResponse);

        final ResultActions result = mockMvc.perform(
            multipart(HttpMethod.PATCH, "/api/postits/{postitId}", 1L)
                .file(imageFile)
                .file(requestPart)
                .with(csrf())
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .header(HttpHeaders.AUTHORIZATION, getTokenExample())

        );

        result.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andDo(
                document(
                    "포스트잇 수정",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    resource(
                        ResourceSnippetParameters.builder()
                            .tag(tag)
                            .summary("포스트잇을 수정하는 API")
                            .pathParameters(
                                parameterWithName("postitId").type(SimpleType.NUMBER)
                                    .description("수정하려는 포스트잇 ID")
                            )
                            .build()
                    ),
                    requestParts(
                        partWithName("request")
                            .description("포스트잇 수정 요청 JSON 데이터"),
                        partWithName("imageFile")
                            .description("첨부 이미지 파일 (선택사항)").optional()
                    ),
                    requestPartFields(
                        "request",
                        fieldWithPath("danger")
                            .type(JsonFieldType.STRING)
                            .description("""
                                포스트잇 위험도
                                - DANGER : 위험
                                - WARNING : 주의
                                - NOTICE : 알림
                                """),
                        fieldWithPath("lat")
                            .type(JsonFieldType.NUMBER)
                            .description("등록 위도 (수정되지 않아도 원래 값 담기)"),
                        fieldWithPath("lng")
                            .type(JsonFieldType.NUMBER)
                            .description("등록 경도 (수정되지 않아도 원래 값 담기)"),
                        fieldWithPath("subject")
                            .type(JsonFieldType.STRING)
                            .description("포스트잇 제목 (수정되지 않아도 원래 값 담기)"),
                        fieldWithPath("content")
                            .type(JsonFieldType.STRING)
                            .description("포스트잇 내용 (수정되지 않아도 원래 값 담기)"),
                        fieldWithPath("imageUrl")
                            .type(JsonFieldType.STRING)
                            .description("이미지 url (수정되지 않아도 원래 값 담기)")
                    ),
                    responseFields(
                        ArrayUtils.addAll(
                            commonResponseFormat(),
                            fieldWithPath("data.postitId")
                                .type(JsonFieldType.NUMBER)
                                .description("생성된 포스트잇 ID"),
                            fieldWithPath("data.danger")
                                .type(JsonFieldType.STRING)
                                .description("포스트잇 위험도"),
                            fieldWithPath("data.lat")
                                .type(JsonFieldType.NUMBER)
                                .description("등록 위도"),
                            fieldWithPath("data.lng")
                                .type(JsonFieldType.NUMBER)
                                .description("등록 경도"),
                            fieldWithPath("data.subject")
                                .type(JsonFieldType.STRING)
                                .description("포스트잇 제목"),
                            fieldWithPath("data.content")
                                .type(JsonFieldType.STRING)
                                .description("포스트잇 내용"),
                            fieldWithPath("data.imageUrl")
                                .type(JsonFieldType.STRING)
                                .description("등록된 이미지 URL"),
                            fieldWithPath("data.expiryDate")
                                .type(JsonFieldType.STRING)
                                .description("""
                                    포스트잇 삭제 예정일
                                    형식 : YYYY-MM-dd HH-mm
                                    """),
                            fieldWithPath("data.createDate")
                                .type(JsonFieldType.STRING)
                                .description("""
                                    포스트잇 등록일
                                    형식 : YYYY-MM-dd HH-mm
                                    """),
                            fieldWithPath("data.memberId")
                                .type(JsonFieldType.NUMBER)
                                .description("등록한 사용자 ID")
                        )
                    )
                )
            );
    }

    @Test
    @DisplayName("포스트잇 삭제")
    @WithMockCustomUser
    public void deletePostIt() throws Exception {

        CustomUserDetails customUserDetails = new CustomUserDetails(1L, "user1@gmamil.com", "12345",
            "url2.jpg", "user1", MemberRole.USER, MemberStatus.ACTIVITY);

        doNothing().when(postItService).deletePostIt(anyLong(), any(CustomUser2Member.class));

        final ResultActions result = mockMvc.perform(
            delete("/api/postits/{postitId}", 1L)
                .with(csrf())
                .header(HttpHeaders.AUTHORIZATION, getTokenExample())
        );

        result.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andDo(
                document(
                    "포스트잇 삭제",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    resource(
                        ResourceSnippetParameters.builder()
                            .tag(tag)
                            .summary("포스트잇을 삭제하는 API")
                            .requestHeaders(jwtFormat())
                            .pathParameters(
                                parameterWithName("postitId").type(SimpleType.NUMBER)
                                    .description("삭제하려는 포스트잇 ID")
                            )
                            .build()
                    )
                )
            );
    }

    @Test
    @DisplayName("포스트잇 해결")
    @WithMockCustomUser
    public void completePostIt() throws Exception {
        PostItResponse postItResponse = createResponse(1L);

        given(postItService.completePostIt(anyLong())).willReturn(postItResponse);

        final ResultActions result = mockMvc.perform(patch("/api/postits/complete/{postitId}",1L)
                .with(csrf())
                .header(HttpHeaders.AUTHORIZATION, getTokenExample())
        );

        result.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andDo(
                document(
                    "포스트잇 해결",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    resource(
                        ResourceSnippetParameters.builder()
                            .tag(tag)
                            .summary("포스트잇을 해결상태로 변경하는 API")
                            .pathParameters(
                                parameterWithName("postitId").type(SimpleType.NUMBER)
                                    .description("해결하려는 포스트잇 ID")
                            )
                            .build()
                    ),
                    responseFields(
                        ArrayUtils.addAll(
                            commonResponseFormat(),
                            fieldWithPath("data.postitId")
                                .type(JsonFieldType.NUMBER)
                                .description("생성된 포스트잇 ID"),
                            fieldWithPath("data.danger")
                                .type(JsonFieldType.STRING)
                                .description("포스트잇 위험도"),
                            fieldWithPath("data.lat")
                                .type(JsonFieldType.NUMBER)
                                .description("등록 위도"),
                            fieldWithPath("data.lng")
                                .type(JsonFieldType.NUMBER)
                                .description("등록 경도"),
                            fieldWithPath("data.subject")
                                .type(JsonFieldType.STRING)
                                .description("포스트잇 제목"),
                            fieldWithPath("data.content")
                                .type(JsonFieldType.STRING)
                                .description("포스트잇 내용"),
                            fieldWithPath("data.imageUrl")
                                .type(JsonFieldType.STRING)
                                .description("등록된 이미지 URL"),
                            fieldWithPath("data.expiryDate")
                                .type(JsonFieldType.STRING)
                                .description("""
                                    포스트잇 삭제 예정일
                                    형식 : YYYY-MM-dd HH-mm
                                    """),
                            fieldWithPath("data.createDate")
                                .type(JsonFieldType.STRING)
                                .description("""
                                    포스트잇 등록일
                                    형식 : YYYY-MM-dd HH-mm
                                    """),
                            fieldWithPath("data.memberId")
                                .type(JsonFieldType.NUMBER)
                                .description("등록한 사용자 ID")
                        )
                    )
                )
            );
    }

    private PostItResponse createResponse(Long memberId) {
        return PostItResponse.builder()
            .postitId(1L)
            .danger(Danger.WARNING) // 수정된 오타
            .lat(37.5206868)
            .lng(127.1171114)
            .subject("포스트잇 제목")
            .content("포스트잇 내용")
            .imageUrl("https://image.com/imageUrl")
            .expiryDate(LocalDateTime.of(2025, 1, 8, 1, 25))
            .createDate(LocalDateTime.of(2025, 1, 1, 1, 25))
            .memberId(memberId)
            .build();
    }
}
