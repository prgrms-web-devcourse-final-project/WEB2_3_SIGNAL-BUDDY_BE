package org.programmers.signalbuddyfinal.domain.member.controller;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.headerWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static com.epages.restdocs.apispec.Schema.schema;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.programmers.signalbuddyfinal.global.support.RestDocsFormatGenerators.commonResponse;
import static org.programmers.signalbuddyfinal.global.support.RestDocsFormatGenerators.commonResponseFormat;
import static org.programmers.signalbuddyfinal.global.support.RestDocsFormatGenerators.pageResponseFormat;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.partWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParts;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.SimpleType;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.programmers.signalbuddyfinal.domain.feedback.dto.FeedbackResponse;
import org.programmers.signalbuddyfinal.domain.feedback.entity.enums.AnswerStatus;
import org.programmers.signalbuddyfinal.domain.feedback.service.FeedbackService;
import org.programmers.signalbuddyfinal.domain.member.dto.MemberResponse;
import org.programmers.signalbuddyfinal.domain.member.dto.MemberUpdateRequest;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberRole;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberStatus;
import org.programmers.signalbuddyfinal.domain.member.service.MemberService;
import org.programmers.signalbuddyfinal.global.anotation.WithMockCustomUser;
import org.programmers.signalbuddyfinal.global.config.WebConfig;
import org.programmers.signalbuddyfinal.global.dto.PageResponse;
import org.programmers.signalbuddyfinal.global.support.ControllerTest;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.multipart.MultipartFile;
import org.testcontainers.shaded.org.apache.commons.lang3.ArrayUtils;

@WebMvcTest(MemberController.class)
@Import(WebConfig.class) // @EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO) 적용
class MemberControllerTest extends ControllerTest {

    private final String tag = "Member API";

    @MockitoBean
    private MemberService memberService;

    @MockitoBean
    private FeedbackService feedbackService;

    @DisplayName("ID로 유저 조회")
    @Test
    @WithMockCustomUser
    void getMemberById() throws Exception {
        final Long memberId = 1L;
        final MemberResponse memberResponse = MemberResponse.builder().memberId(memberId)
            .memberStatus(MemberStatus.ACTIVITY).role(MemberRole.USER).email("test@example.com")
            .nickname("Test").build();

        given(memberService.getMember(memberId)).willReturn(memberResponse);

        ResultActions result = mockMvc.perform(get("/api/members/{id}", memberId));

        result.andExpect(status().isOk()).andDo(
            document("유저 정보 조회", preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()), resource(
                    ResourceSnippetParameters.builder().tag(tag).summary("유저 정보 조회").pathParameters(
                            parameterWithName("id").type(SimpleType.NUMBER).description("유저 ID"))
                        .responseSchema(schema("MemberResponse")) // Schema 이름
                        .responseFields(ArrayUtils.addAll(commonResponseFormat(),  // 공통 응답 필드 추가
                            fieldWithPath("data.memberId").type(JsonFieldType.NUMBER)
                                .description("유저 ID"),
                            fieldWithPath("data.email").type(JsonFieldType.STRING)
                                .description("유저 이메일"),
                            fieldWithPath("data.nickname").type(JsonFieldType.STRING)
                                .description("유저 닉네임"),
                            fieldWithPath("data.profileImageUrl").type(JsonFieldType.STRING)
                                .optional().description("프로필 이미지 URL"),
                            fieldWithPath("data.role").type(JsonFieldType.STRING)
                                .description("유저 역할"),
                            fieldWithPath("data.memberStatus").type(JsonFieldType.STRING)
                                .description("유저 상태"))).build())));
    }

    @DisplayName("유저 정보 수정")
    @Test
    @WithMockCustomUser
    void updateMember() throws Exception {
        // Given
        final Long memberId = 1L;
        final MemberUpdateRequest request = MemberUpdateRequest.builder().email("test2@example.com")
            .nickname("Test2").password("newPassword").build();

        final MemberResponse memberResponse = MemberResponse.builder().memberId(memberId)
            .memberStatus(MemberStatus.ACTIVITY).role(MemberRole.USER).email(request.getEmail())
            .nickname(request.getNickname()).build();

        given(memberService.updateMember(eq(memberId), any(MemberUpdateRequest.class),
            any(HttpServletRequest.class))).willReturn(memberResponse);

        // When
        ResultActions result = mockMvc.perform(patch("/api/members/{id}", memberId).contentType(
                MediaType.APPLICATION_JSON) // JSON 요청임을 명시
            .content(new ObjectMapper().writeValueAsString(request)) // 요청 본문 추가
        );

        // Then
        result.andExpect(status().isOk()).andDo(
            document("유저 정보 수정", preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()), resource(
                    ResourceSnippetParameters.builder().tag(tag).summary("유저 정보 수정").pathParameters(
                            parameterWithName("id").type(SimpleType.NUMBER).description("유저 ID"))
                        .requestSchema(schema("MemberUpdateRequest")).requestFields( // 요청 필드 추가
                            fieldWithPath("email").type(JsonFieldType.STRING).description("이메일"),
                            fieldWithPath("nickname").type(JsonFieldType.STRING).description("닉네임"),
                            fieldWithPath("password").type(JsonFieldType.STRING).description("비밀번호"))
                        .responseSchema(schema("MemberResponse")).responseFields(
                            ArrayUtils.addAll(commonResponseFormat(),
                                fieldWithPath("data.memberId").type(JsonFieldType.NUMBER)
                                    .description("유저 ID"),
                                fieldWithPath("data.email").type(JsonFieldType.STRING)
                                    .description("유저 이메일"),
                                fieldWithPath("data.nickname").type(JsonFieldType.STRING)
                                    .description("유저 닉네임"),
                                fieldWithPath("data.profileImageUrl").type(JsonFieldType.STRING)
                                    .optional().description("프로필 이미지 URL"),
                                fieldWithPath("data.role").type(JsonFieldType.STRING)
                                    .description("유저 역할"),
                                fieldWithPath("data.memberStatus").type(JsonFieldType.STRING)
                                    .description("유저 상태"))).build())));
    }

    @DisplayName("유저 프로필 이미지 변경")
    @Test
    void updateProfileImage() throws Exception {
        final Long memberId = 1L;
        MockMultipartFile profileImage = new MockMultipartFile("imageFile", // 요청 필드명
            "profile.png", // 파일명
            MediaType.IMAGE_PNG_VALUE, new byte[]{1, 2, 3, 4} // 임의의 바이트 데이터
        );

        final String uploadedImageUrl = "https://s3.amazonaws.com/test/profile.png";

        given(memberService.saveProfileImage(eq(memberId), any(MultipartFile.class))).willReturn(
            uploadedImageUrl);

        // When
        ResultActions result = mockMvc.perform(
            multipart(HttpMethod.PATCH, "/api/members/{id}/profile-image", memberId).file(
                profileImage).contentType(MediaType.MULTIPART_FORM_DATA));

        // TODO : Request Part 알아봐야함.
        result.andExpect(status().isOk()).andDo(
            document("유저 프로필 이미지 변경", preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestParts(partWithName("imageFile").description("업로드할 이미지 파일")), resource(
                    ResourceSnippetParameters.builder().tag(tag).summary("유저 프로필 이미지 변경")
                        .pathParameters(
                            parameterWithName("id").type(SimpleType.NUMBER).description("유저 ID"))
                        .responseSchema(commonResponse).responseFields(commonResponseFormat())
                        .build())));
    }

    @DisplayName("유저 프로필 이미지 조회")
    @Test
    void getProfileImage() throws Exception {
        final Long memberId = 1L;

        // 가상의 이미지 데이터를 가진 ByteArrayResource 생성
        byte[] imageData = "dummy image data".getBytes();
        Resource imageResource = new ByteArrayResource(imageData);

        given(memberService.getProfileImage(memberId)).willReturn(imageResource);

        ResultActions result = mockMvc.perform(get("/api/members/{id}/profile-image", memberId));

        result.andExpect(status().isOk()).andDo(
            document("유저 프로필 이미지 조회", preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()), resource(
                    ResourceSnippetParameters.builder().tag(tag).summary("유저 프로필 이미지 조회")
                        .pathParameters(
                            parameterWithName("id").type(SimpleType.NUMBER).description("유저 ID"))
                        .responseHeaders(
                            headerWithName(HttpHeaders.CONTENT_TYPE).description("응답 콘텐츠 타입"))
                        .build())));
    }

    @DisplayName("유저 탈퇴")
    @Test
    void deleteMember() throws Exception {
        final Long memberId = 1L;
        final MemberResponse memberResponse = MemberResponse.builder().memberId(memberId)
            .memberStatus(MemberStatus.WITHDRAWAL).role(MemberRole.USER).email("test@example.com")
            .nickname("Test").build();

        given(memberService.deleteMember(memberId)).willReturn(memberResponse);
        ResultActions result = mockMvc.perform(delete("/api/members/{id}", memberId));

        result.andExpect(status().isOk()).andDo(
            document("유저 탈퇴", preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint()),
                resource(ResourceSnippetParameters.builder().tag(tag).summary("유저 탈퇴")
                    .pathParameters(
                        parameterWithName("id").type(SimpleType.NUMBER).description("유저 ID"))
                    .responseSchema(schema("MemberResponse")).responseFields(
                        ArrayUtils.addAll(commonResponseFormat(),
                            fieldWithPath("data.memberId").type(JsonFieldType.NUMBER)
                                .description("유저 ID"),
                            fieldWithPath("data.email").type(JsonFieldType.STRING)
                                .description("유저 이메일"),
                            fieldWithPath("data.nickname").type(JsonFieldType.STRING)
                                .description("유저 닉네임"),
                            fieldWithPath("data.profileImageUrl").type(JsonFieldType.STRING)
                                .optional().description("프로필 이미지 URL"),
                            fieldWithPath("data.role").type(JsonFieldType.STRING)
                                .description("유저 역할"),
                            fieldWithPath("data.memberStatus").type(JsonFieldType.STRING)
                                .description("유저 상태"))).build())));
    }

    @DisplayName("비밀번호 확인")
    @Test
    void verifyPassword() throws Exception {
        final Long memberId = 1L;
        final String currentPassword = "currentPassword";

        given(memberService.verifyPassword(currentPassword, memberId)).willReturn(true);

        ResultActions result = mockMvc.perform(
            post("/api/members/{id}/verify-password", memberId).param("password", currentPassword));

        result.andExpect(status().isOk()).andDo(
            document("현재 비밀번호 확인", preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()), resource(
                    ResourceSnippetParameters.builder().tag(tag).summary("현재 비밀번호 확인")
                        .pathParameters(
                            parameterWithName("id").type(SimpleType.NUMBER).description("유저 ID"))
                        .formParameters(parameterWithName("password").type(SimpleType.STRING)
                            .description("현재 비밀번호")).responseSchema(commonResponse)
                        .responseFields(commonResponseFormat()).build())));
    }

    @DisplayName("본인이 쓴 피드백 목록 조회")
    @Test
    void getFeedbacks() throws Exception {
        final Long memberId = 1L;
        final List<FeedbackResponse> feedbackList = List.of(
            FeedbackResponse.builder().feedbackId(1L).subject("좋은 피드백").content("이 기능이 매우 유용했습니다!")
                .likeCount(2L).secret(Boolean.TRUE).answerStatus(AnswerStatus.BEFORE)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
            FeedbackResponse.builder().feedbackId(2L).subject("개선 요청").content("이 부분을 좀 더 개선해 주세요.")
                .likeCount(2L).secret(Boolean.FALSE).answerStatus(AnswerStatus.COMPLETION)
                .createdAt(LocalDateTime.now().minusDays(1))
                .updatedAt(LocalDateTime.now().minusDays(1)).build());
        final Page<FeedbackResponse> feedbackPage = new PageImpl<>(feedbackList,
            PageRequest.of(0, 10), feedbackList.size());

        given(
            feedbackService.findPagedExcludingMember(eq(memberId), any(Pageable.class))).willReturn(
            new PageResponse<>(feedbackPage));

        final ResultActions result = mockMvc.perform(
            get("/api/members/{id}/feedbacks", memberId).param("page", "0").param("size", "10"));

        result.andExpect(status().isOk()).andDo(
            document("유저의 피드백 목록 조회", preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()), resource(
                    ResourceSnippetParameters.builder().tag(tag).summary("유저의 피드백 목록 조회")
                        .pathParameters(parameterWithName("id").description("유저 ID"))
                        .queryParameters(parameterWithName("page").optional()
                                .description("조회할 페이지 번호 (기본 값: 0)"),
                            parameterWithName("size").optional()
                                .description("한 페이지당 항목 수 (기본 값: 10)"))
                        .responseSchema(schema("PagedFeedbackResponse")).responseFields(
                            ArrayUtils.addAll(pageResponseFormat(),
                                fieldWithPath("data.searchResults[].feedbackId").type(
                                    JsonFieldType.NUMBER).description("피드백 ID"),
                                fieldWithPath("data.searchResults[].subject").type(JsonFieldType.STRING)
                                    .description("피드백 제목"),
                                fieldWithPath("data.searchResults[].content").type(JsonFieldType.STRING)
                                    .description("피드백 내용"),
                                fieldWithPath("data.searchResults[].likeCount").type(
                                    JsonFieldType.NUMBER).description("피드백 좋아요 수"),
                                fieldWithPath("data.searchResults[].secret").type(JsonFieldType.BOOLEAN)
                                    .description("피드백 비밀글 여부"),
                                fieldWithPath("data.searchResults[].answerStatus").type(
                                    JsonFieldType.STRING).description("피드백 상태 (BEFORE, COMPLETION)"),
                                fieldWithPath("data.searchResults[].createdAt").type(
                                    JsonFieldType.STRING).description("피드백 작성 날짜"),
                                fieldWithPath("data.searchResults[].updatedAt").type(
                                    JsonFieldType.STRING).description("피드백 수정 날짜"))).build())));
    }
}