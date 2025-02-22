package org.programmers.signalbuddyfinal.domain.feedback.controller;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.programmers.signalbuddyfinal.global.support.RestDocsFormatGenerators.commonResponseFormat;
import static org.programmers.signalbuddyfinal.global.support.RestDocsFormatGenerators.getMockImageFile;
import static org.programmers.signalbuddyfinal.global.support.RestDocsFormatGenerators.getTokenExample;
import static org.programmers.signalbuddyfinal.global.support.RestDocsFormatGenerators.jwtFormat;
import static org.programmers.signalbuddyfinal.global.support.RestDocsFormatGenerators.pageResponseWithMemberFormat;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestPartFields;
import static org.springframework.restdocs.request.RequestDocumentation.partWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParts;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.SimpleType;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.programmers.signalbuddyfinal.domain.crossroad.dto.CrossroadResponse;
import org.programmers.signalbuddyfinal.domain.feedback.dto.FeedbackRequest;
import org.programmers.signalbuddyfinal.domain.feedback.dto.FeedbackResponse;
import org.programmers.signalbuddyfinal.domain.feedback.entity.enums.AnswerStatus;
import org.programmers.signalbuddyfinal.domain.feedback.entity.enums.FeedbackCategory;
import org.programmers.signalbuddyfinal.domain.feedback.service.FeedbackService;
import org.programmers.signalbuddyfinal.domain.member.dto.MemberResponse;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberRole;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberStatus;
import org.programmers.signalbuddyfinal.global.anotation.WithMockCustomUser;
import org.programmers.signalbuddyfinal.global.dto.CustomUser2Member;
import org.programmers.signalbuddyfinal.global.dto.PageResponse;
import org.programmers.signalbuddyfinal.global.support.ControllerTest;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.multipart.MultipartFile;
import org.testcontainers.shaded.org.apache.commons.lang3.ArrayUtils;

@WebMvcTest(FeedbackController.class)
class FeedbackControllerTest extends ControllerTest {

    @MockitoBean
    private FeedbackService feedbackService;

    private final String tag = "Feedback API";
    private final String imageFormName = "imageFile";

    @DisplayName("피드백을 작성한다.")
    @Test
    @WithMockCustomUser
    void writeFeedback() throws Exception {
        // Given
        FeedbackRequest request = FeedbackRequest.builder()
            .subject("test subject").content("test content").secret(Boolean.FALSE)
            .category(FeedbackCategory.ETC).crossroadId(1L)
            .build();
        MockMultipartFile requestPart = new MockMultipartFile(
            "request", "",
            MediaType.APPLICATION_JSON_VALUE,
            objectMapper.writeValueAsBytes(request)
        );
        MockMultipartFile imageFile = getMockImageFile(imageFormName);

        MemberResponse member = makeMemberResponse();
        CrossroadResponse crossroad = makeCrossroadResponse();
        FeedbackResponse response = makeFeedbackResponse(member, crossroad);

        given(
            feedbackService.writeFeedback(
                any(FeedbackRequest.class), any(MultipartFile.class), any(CustomUser2Member.class)
            )
        ).willReturn(response);

        // When
        ResultActions result = mockMvc.perform(
            multipart("/api/feedbacks")
                .file(imageFile)
                .file(requestPart)
                .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                .header(HttpHeaders.AUTHORIZATION, getTokenExample())
        );

        // Then
        result.andExpect(status().isCreated())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.data.feedbackId").isNumber())
            .andDo(
                document(
                    "피드백 작성",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    requestParts(
                        partWithName("request")
                            .description("피드백 작성 요청 JSON 데이터"),
                        partWithName("imageFile")
                            .description("첨부 이미지 파일 (선택 사항)").optional()
                    ),
                    requestPartFields(
                        "request",
                        fieldWithPath("subject")
                            .type(JsonFieldType.STRING)
                            .description("피드백 제목"),
                        fieldWithPath("content")
                            .type(JsonFieldType.STRING)
                            .description("피드백 내용"),
                        fieldWithPath("category")
                            .type(JsonFieldType.STRING)
                            .description("""
                                피드백 유형
                                - DELAY : 신호 지연
                                - MALFUNCTION : 오작동
                                - ADD_SIGNAL : 신호등 추가
                                - ETC : 기타
                                """),
                        fieldWithPath("secret")
                            .type(JsonFieldType.BOOLEAN)
                            .description("비밀글 여부"),
                        fieldWithPath("crossroadId")
                            .type(JsonFieldType.NUMBER)
                            .description("피드백하려는 교차로 ID")
                    ),
                    resource(
                        ResourceSnippetParameters.builder()
                            .tag(tag)
                            .summary("피드백 작성")
                            .requestHeaders(
                                jwtFormat()
                            )
                            .responseFields(
                                feedbackDetailResponseDocs()
                            )
                            .build()
                    )
                )
            );
    }

    @DisplayName("피드백 목록을 검색한다.")
    @Test
    void searchFeedbackList() throws Exception {
        // Given
        Pageable pageable = PageRequest.of(3, 10);
        List<FeedbackResponse> feedbacks = new ArrayList<>();
        MemberResponse member = makeMemberResponse();
        for (int i = 1; i <= 10; i++) {
            feedbacks.add(makeFeedbackResponse(member, i));
        }
        PageResponse<FeedbackResponse> response = new PageResponse<>(
            new PageImpl<>(feedbacks, pageable, 55)
        );

        given(feedbackService.searchFeedbackList(any(Pageable.class), any(AnswerStatus.class), anySet(), anyLong()))
            .willReturn(response);

        // When
        ResultActions result = mockMvc.perform(
            get("/api/feedbacks")
                .queryParam("page", String.valueOf(pageable.getPageNumber()))
                .queryParam("size", String.valueOf(pageable.getPageSize()))
                .queryParam("status", AnswerStatus.BEFORE.getValue())
                .queryParam("category",
                    FeedbackCategory.ETC.getValue(), FeedbackCategory.DELAY.getValue())
                .queryParam("crossroadId", String.valueOf(1L))
        );

        // Then
        result.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(
                jsonPath("$.data.searchResults[0].feedbackId")
                    .value(response.getSearchResults().get(0).getFeedbackId())
            )
            .andDo(
                document(
                    "피드백 목록",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    resource(
                        ResourceSnippetParameters.builder()
                            .tag(tag)
                            .summary("피드백 목록")
                            .queryParameters(
                                parameterWithName("page").type(SimpleType.NUMBER)
                                    .description("페이지 번호 (기본값 : 0, 0부터 시작)").optional(),
                                parameterWithName("size").type(SimpleType.NUMBER)
                                    .description("페이지 크기 (기본값 : 7)").optional(),
                                parameterWithName("status").type(SimpleType.STRING)
                                    .description("""
                                        피드백 답변 상태 (지정하지 않으면 전체 검색, 택 1)
                                        - before : 답변 전
                                        - completion : 답변 완료
                                        """).optional(),
                                parameterWithName("category").type(SimpleType.STRING)
                                    .description("""
                                        피드백 유형 (지정하지 않으면 전체 검색, 여러 개 선택 가능)
                                        - delay : 신호 지연
                                        - malfunction : 오작동
                                        - add-signal : 신호등 추가
                                        - etc : 기타
                                        """).optional(),
                                parameterWithName("crossroadId").type(SimpleType.NUMBER)
                                    .description("교차로 ID (지정하지 않으면 전체 검색)").optional()
                            )
                            .responseFields(
                                ArrayUtils.addAll(
                                    pageResponseWithMemberFormat(),
                                    fieldWithPath("data.searchResults[].feedbackId")
                                        .type(JsonFieldType.NUMBER)
                                        .description("피드백 ID"),
                                    fieldWithPath("data.searchResults[].subject")
                                        .type(JsonFieldType.STRING)
                                        .description("피드백 제목"),
                                    fieldWithPath("data.searchResults[].content")
                                        .type(JsonFieldType.STRING)
                                        .description("피드백 내용"),
                                    fieldWithPath("data.searchResults[].category")
                                        .type(JsonFieldType.STRING)
                                        .description("""
                                            피드백 유형
                                            - DELAY : 신호 지연
                                            - MALFUNCTION : 오작동
                                            - ADD_SIGNAL : 신호등 추가
                                            - ETC : 기타
                                            """),
                                    fieldWithPath("data.searchResults[].likeCount")
                                        .type(JsonFieldType.NUMBER)
                                        .description("피드백의 좋아요 개수"),
                                    fieldWithPath("data.searchResults[].secret")
                                        .type(JsonFieldType.BOOLEAN)
                                        .description("비밀글 여부"),
                                    fieldWithPath("data.searchResults[].answerStatus")
                                        .type(JsonFieldType.STRING)
                                        .description("""
                                            피드백의 답변 여부
                                            - BEFORE : 답변 전
                                            - COMPLETION : 답변 완료
                                            """),
                                    fieldWithPath("data.searchResults[].imageUrl")
                                        .type(JsonFieldType.STRING)
                                        .description("이미지 URL"),
                                    fieldWithPath("data.searchResults[].createdAt")
                                        .type(JsonFieldType.STRING)
                                        .description("피드백 작성일"),
                                    fieldWithPath("data.searchResults[].updatedAt")
                                        .type(JsonFieldType.STRING)
                                        .description("피드백 수정일")
                                )
                            )
                            .build()
                    )
                )
            );
    }

    @DisplayName("피드백을 상세 조회한다.")
    @Test
    @WithMockCustomUser
    void searchFeedbackDetail() throws Exception {
        // Given
        MemberResponse member = makeMemberResponse();
        CrossroadResponse crossroad = makeCrossroadResponse();
        FeedbackResponse response = makeFeedbackResponse(member, crossroad);

        given(feedbackService.searchFeedbackDetail(anyLong(), any(CustomUser2Member.class)))
            .willReturn(response);

        // When
        ResultActions result = mockMvc.perform(
            get("/api/feedbacks/{feedbackId}", response.getFeedbackId())
                .header("authorization", getTokenExample())
        );

        // Then
        result.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.data.feedbackId").isNumber())
            .andExpect(
                jsonPath("$.data.feedbackId")
                    .value(response.getFeedbackId())
            )
            .andDo(
                document(
                    "피드백 상세 조회",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    resource(
                        ResourceSnippetParameters.builder()
                            .tag(tag)
                            .summary("피드백 상세 조회")
                            .requestHeaders(
                                jwtFormat().optional()
                            )
                            .pathParameters(
                                parameterWithName("feedbackId").type(SimpleType.NUMBER)
                                    .description("상세 조회하려는 피드백 ID")
                            )
                            .responseFields(
                                feedbackDetailResponseDocs()
                            )
                            .build()
                    )
                )
            );
    }

    @DisplayName("피드백을 수정한다.")
    @Test
    @WithMockCustomUser
    void updateFeedback() throws Exception {
        // Given
        String updatedSubject = "update test subject";
        FeedbackRequest request = FeedbackRequest.builder()
            .subject(updatedSubject).content("test content").secret(Boolean.FALSE)
            .category(FeedbackCategory.DELAY).crossroadId(1L)
            .build();
        MockMultipartFile requestPart = new MockMultipartFile(
            "request", "",
            MediaType.APPLICATION_JSON_VALUE,
            objectMapper.writeValueAsBytes(request)
        );
        MockMultipartFile imageFile = getMockImageFile(imageFormName);

        MemberResponse member = makeMemberResponse();
        CrossroadResponse crossroad = makeCrossroadResponse();
        FeedbackResponse response = FeedbackResponse.builder()
            .feedbackId(1L).secret(Boolean.FALSE)
            .subject(updatedSubject).content("test content")
            .answerStatus(AnswerStatus.BEFORE).category(FeedbackCategory.DELAY)
            .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
            .imageUrl("https://image.com/dfsdfsdf").likeCount(0L)
            .member(member).crossroad(crossroad)
            .build();

        given(
            feedbackService.updateFeedback(
                anyLong(), any(FeedbackRequest.class),
                any(MultipartFile.class), any(CustomUser2Member.class)
            )
        ).willReturn(response);

        // When
        ResultActions result = mockMvc.perform(
            multipart(HttpMethod.PATCH, "/api/feedbacks/{feedbackId}", 1L)
                .file(imageFile)
                .file(requestPart)
                .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                .header(HttpHeaders.AUTHORIZATION, getTokenExample())
        );

        // Then
        result.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.data.subject").value(updatedSubject))
            .andExpect(jsonPath("$.data.category").value(FeedbackCategory.DELAY.name()))
            .andDo(
                document(
                    "피드백 수정",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    requestParts(
                        partWithName("request")
                            .description("피드백 수정 요청 JSON 데이터"),
                        partWithName("imageFile")
                            .description("첨부 이미지 파일 (선택 사항)").optional()
                    ),
                    requestPartFields(
                        "request",
                        fieldWithPath("subject")
                            .type(JsonFieldType.STRING)
                            .description("피드백 제목 (수정되지 않아도 원래 값 담기)"),
                        fieldWithPath("content")
                            .type(JsonFieldType.STRING)
                            .description("피드백 내용 (수정되지 않아도 원래 값 담기)"),
                        fieldWithPath("category")
                            .type(JsonFieldType.STRING)
                            .description("""
                                피드백 유형 (수정되지 않아도 원래 값 담기)
                                - DELAY : 신호 지연
                                - MALFUNCTION : 오작동
                                - ADD_SIGNAL : 신호등 추가
                                - ETC : 기타
                                """),
                        fieldWithPath("secret")
                            .type(JsonFieldType.BOOLEAN)
                            .description("비밀글 여부 (수정되지 않아도 원래 값 담기)"),
                        fieldWithPath("crossroadId")
                            .type(JsonFieldType.NUMBER)
                            .description("피드백하려는 교차로 ID (수정되지 않아도 원래 값 담기)")
                    ),
                    resource(
                        ResourceSnippetParameters.builder()
                            .tag(tag)
                            .summary("피드백 수정")
                            .requestHeaders(
                                jwtFormat()
                            )
                            .responseFields(
                                feedbackDetailResponseDocs()
                            )
                            .build()
                    )
                )
            );
    }

    @DisplayName("피드백을 삭제한다.")
    @Test
    @WithMockCustomUser
    void deleteFeedback() throws Exception {
        // Given
        doNothing().when(feedbackService).deleteFeedback(anyLong(), any(CustomUser2Member.class));

        // When
        ResultActions result = mockMvc.perform(
            delete("/api/feedbacks/{feedbackId}", 1L)
                .header(HttpHeaders.AUTHORIZATION, getTokenExample())
        );

        // Then
        result.andExpect(status().isOk())
            .andDo(
                document(
                    "피드백 삭제",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    resource(
                        ResourceSnippetParameters.builder()
                            .tag(tag)
                            .summary("피드백 삭제")
                            .requestHeaders(
                                jwtFormat()
                            )
                            .pathParameters(
                                parameterWithName("feedbackId").type(SimpleType.NUMBER)
                                    .description("삭제하려는 피드백 ID")
                            )
                            .build()
                    )
                )
            );
    }

    private MemberResponse makeMemberResponse() {
        return MemberResponse.builder()
            .memberId(1L).email("test@test.com").nickname("test")
            .profileImageUrl("https://image.com/sfdfs")
            .role(MemberRole.USER).memberStatus(MemberStatus.ACTIVITY)
            .build();
    }

    private FeedbackResponse makeFeedbackResponse(
        MemberResponse member, CrossroadResponse crossroad
    ) {
        return FeedbackResponse.builder()
            .feedbackId(1L).secret(Boolean.FALSE)
            .subject("test subject").content("test content")
            .answerStatus(AnswerStatus.BEFORE).category(FeedbackCategory.ETC)
            .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
            .imageUrl("https://image.com/dfsdfsdf").likeCount(0L)
            .member(member).crossroad(crossroad)
            .build();
    }

    private FeedbackResponse makeFeedbackResponse(MemberResponse member, int num) {
        return FeedbackResponse.builder()
            .feedbackId((long) num).secret(Boolean.FALSE)
            .subject("test subject" + num).content("test content" + num)
            .answerStatus(AnswerStatus.BEFORE).category(FeedbackCategory.ETC)
            .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
            .imageUrl("https://image.com/dfsdfsdf").member(member).likeCount(21L % num)
            .build();
    }

    private CrossroadResponse makeCrossroadResponse() {
        return CrossroadResponse.builder()
            .crossroadId(1L).lat(37.1212).lng(127.11212)
            .name("00사거리").status("TRUE")
            .build();
    }

    private FieldDescriptor[] feedbackDetailResponseDocs() {
        FieldDescriptor[] commonDocs = commonResponseFormat();

        FieldDescriptor[] feedbackDetailDocs = new FieldDescriptor[22];
        feedbackDetailDocs[0] = fieldWithPath("data.feedbackId")
            .type(JsonFieldType.NUMBER)
            .description("피드백 ID");
        feedbackDetailDocs[1] = fieldWithPath("data.subject")
            .type(JsonFieldType.STRING)
            .description("피드백 제목");
        feedbackDetailDocs[2] = fieldWithPath("data.content")
            .type(JsonFieldType.STRING)
            .description("피드백 내용");
        feedbackDetailDocs[3] = fieldWithPath("data.category")
            .type(JsonFieldType.STRING)
            .description("""
                피드백 유형
                - DELAY : 신호 지연
                - MALFUNCTION : 오작동
                - ADD_SIGNAL : 신호등 추가
                - ETC : 기타
                """);
        feedbackDetailDocs[4] = fieldWithPath("data.likeCount")
            .type(JsonFieldType.NUMBER)
            .description("피드백의 좋아요 개수");
        feedbackDetailDocs[5] = fieldWithPath("data.secret")
            .type(JsonFieldType.BOOLEAN)
            .description("비밀글 여부");
        feedbackDetailDocs[6] = fieldWithPath("data.answerStatus")
            .type(JsonFieldType.STRING)
            .description("""
                피드백의 답변 여부
                - BEFORE : 답변 전
                - COMPLETION : 답변 완료
                """);
        feedbackDetailDocs[7] = fieldWithPath("data.imageUrl")
            .type(JsonFieldType.STRING)
            .description("이미지 URL");
        feedbackDetailDocs[8] = fieldWithPath("data.createdAt")
            .type(JsonFieldType.STRING)
            .description("피드백 작성일");
        feedbackDetailDocs[9] = fieldWithPath("data.updatedAt")
            .type(JsonFieldType.STRING)
            .description("피드백 수정일");
        feedbackDetailDocs[10] = fieldWithPath("data.member")
            .type(JsonFieldType.OBJECT)
            .description("작성자 정보");
        feedbackDetailDocs[11] = fieldWithPath("data.member.memberId")
            .type(JsonFieldType.NUMBER)
            .description("작성자 ID(PK)");
        feedbackDetailDocs[12] = fieldWithPath("data.member.email")
            .type(JsonFieldType.STRING)
            .description("작성자의 이메일");
        feedbackDetailDocs[13] = fieldWithPath("data.member.nickname")
            .type(JsonFieldType.STRING)
            .description("작성자의 닉네임");
        feedbackDetailDocs[14] = fieldWithPath("data.member.profileImageUrl")
            .type(JsonFieldType.STRING)
            .description("작성자의 프로필 이미지 URL");
        feedbackDetailDocs[15] = fieldWithPath("data.member.role")
            .type(JsonFieldType.STRING)
            .description("""
                작성자의 권한
                - USER : 일반 사용자
                - ADMIN : 관리자
                """);
        feedbackDetailDocs[16] = fieldWithPath("data.member.memberStatus")
            .type(JsonFieldType.STRING)
            .description("""
                작성자의 탈퇴 여부
                - ACTIVITY : 활동 상태
                - WITHDRAWAL : 탈퇴 상태
                """);
        feedbackDetailDocs[17] = fieldWithPath("data.crossroad.crossroadId")
            .type(JsonFieldType.NUMBER)
            .description("교차로 ID(PK)");
        feedbackDetailDocs[18] = fieldWithPath("data.crossroad.lat")
            .type(JsonFieldType.NUMBER)
            .description("교차로의 위도 좌표");
        feedbackDetailDocs[19] = fieldWithPath("data.crossroad.lng")
            .type(JsonFieldType.NUMBER)
            .description("교차로의 경도 좌표");
        feedbackDetailDocs[20] = fieldWithPath("data.crossroad.name")
            .type(JsonFieldType.STRING)
            .description("교차로 이름");
        feedbackDetailDocs[21] = fieldWithPath("data.crossroad.status")
            .type(JsonFieldType.STRING)
            .description("잔여 시간 API 제공 여부");

        return Stream.concat(Arrays.stream(commonDocs), Arrays.stream(feedbackDetailDocs))
            .toArray(FieldDescriptor[]::new);
    }
}