package org.programmers.signalbuddyfinal.domain.member.controller;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static com.epages.restdocs.apispec.Schema.schema;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.programmers.signalbuddyfinal.global.support.RestDocsFormatGenerators.commonResponse;
import static org.programmers.signalbuddyfinal.global.support.RestDocsFormatGenerators.commonResponseFormat;
import static org.programmers.signalbuddyfinal.global.support.RestDocsFormatGenerators.pageResponseFormat;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.SimpleType;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.programmers.signalbuddyfinal.domain.bookmark.dto.BookmarkRequest;
import org.programmers.signalbuddyfinal.domain.bookmark.dto.BookmarkResponse;
import org.programmers.signalbuddyfinal.domain.bookmark.dto.BookmarkSequenceUpdateRequest;
import org.programmers.signalbuddyfinal.domain.bookmark.service.BookmarkService;
import org.programmers.signalbuddyfinal.domain.feedback.dto.FeedbackResponse;
import org.programmers.signalbuddyfinal.domain.feedback.entity.enums.AnswerStatus;
import org.programmers.signalbuddyfinal.domain.feedback.service.FeedbackService;
import org.programmers.signalbuddyfinal.domain.member.dto.MemberJoinRequest;
import org.programmers.signalbuddyfinal.domain.member.dto.MemberResponse;
import org.programmers.signalbuddyfinal.domain.member.dto.MemberRestoreRequest;
import org.programmers.signalbuddyfinal.domain.member.dto.MemberUpdateRequest;
import org.programmers.signalbuddyfinal.domain.member.dto.ResetPasswordRequest;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberRole;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberStatus;
import org.programmers.signalbuddyfinal.domain.member.service.MemberService;
import org.programmers.signalbuddyfinal.domain.recentpath.dto.RecentPathRequest;
import org.programmers.signalbuddyfinal.domain.recentpath.dto.RecentPathResponse;
import org.programmers.signalbuddyfinal.domain.recentpath.service.RecentPathService;
import org.programmers.signalbuddyfinal.global.anotation.WithMockCustomUser;
import org.programmers.signalbuddyfinal.global.config.WebConfig;
import org.programmers.signalbuddyfinal.global.dto.PageResponse;
import org.programmers.signalbuddyfinal.global.response.ApiResponse;
import org.programmers.signalbuddyfinal.global.support.ControllerTest;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
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

    @MockitoBean
    private BookmarkService bookmarkService;

    @MockitoBean
    private RecentPathService recentPathService;

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

        given(memberService.updateMember(eq(memberId), any(MemberUpdateRequest.class)
        )).willReturn(memberResponse);

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
            multipart(HttpMethod.POST, "/api/members/{id}/profile-image", memberId).file(
                profileImage).contentType(MediaType.MULTIPART_FORM_DATA));

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

    @DisplayName("유저 탈퇴")
    @Test
    void deleteMember() throws Exception {
        final Long memberId = 1L;

        doNothing().when(memberService).deleteMember(memberId);

        ResultActions result = mockMvc.perform(delete("/api/members/{id}", memberId));

        result.andExpect(status().isOk()).andDo(
            document("유저 탈퇴", preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint()),
                resource(ResourceSnippetParameters.builder().tag(tag).summary("유저 탈퇴")
                    .pathParameters(
                        parameterWithName("id").type(SimpleType.NUMBER).description("유저 ID"))
                    .build())));
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

    @DisplayName("나의 장소 목록 조회")
    @Test
    void getBookmarks() throws Exception {
        final Long memberId = 1L;
        final List<BookmarkResponse> bookmarks = List.of(
            BookmarkResponse.builder().bookmarkId(1L).lat(37.501).lng(127.001)
                .address("서울특별시 강남구 테헤란로 123").name("강남역").build(),

            BookmarkResponse.builder().bookmarkId(2L).lat(37.566).lng(126.978)
                .address("서울특별시 중구 을지로 1가").name("서울시청").build(),

            BookmarkResponse.builder().bookmarkId(3L).lat(35.179).lng(129.075)
                .address("부산광역시 해운대구 해운대로 200").name("해운대 해수욕장").build(),

            BookmarkResponse.builder().bookmarkId(4L).lat(37.497).lng(127.027)
                .address("서울특별시 강남구 역삼동").name("역삼역").build());
        final Page<BookmarkResponse> page = new PageImpl<>(bookmarks, PageRequest.of(0, 10),
            bookmarks.size());

        given(bookmarkService.findPagedBookmarks(any(Pageable.class), eq(memberId))).willReturn(
            new PageResponse<>(page));

        final ResultActions result = mockMvc.perform(
            get("/api/members/{id}/bookmarks", memberId).param("page", "0").param("size", "10"));

        result.andExpect(status().isOk()).andDo(
            document("나의 장소 목록 조회", preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()), resource(
                    ResourceSnippetParameters.builder().tag(tag).summary("나의 장소 목록 조회")
                        .pathParameters(parameterWithName("id").description("유저 ID"))
                        .queryParameters(parameterWithName("page").optional()
                                .description("조회할 페이지 번호 (기본 값: 0)"),
                            parameterWithName("size").optional()
                                .description("한 페이지당 항목 수 (기본 값: 10)"))
                        .responseSchema(schema("PagedBookmarkResponse")).responseFields(
                            ArrayUtils.addAll(pageResponseFormat(),
                                fieldWithPath("data.searchResults[].bookmarkId").type(
                                    JsonFieldType.NUMBER).description("북마크 ID"),
                                fieldWithPath("data.searchResults[].lat").type(JsonFieldType.NUMBER)
                                    .description("위도"),
                                fieldWithPath("data.searchResults[].lng").type(JsonFieldType.NUMBER)
                                    .description("경도"),
                                fieldWithPath("data.searchResults[].address").type(JsonFieldType.STRING)
                                    .description("주소"),
                                fieldWithPath("data.searchResults[].name").type(JsonFieldType.STRING)
                                    .description("별칭 (예: 우리집, 회사)"),
                                fieldWithPath("data.searchResults[].sequence").type(
                                    JsonFieldType.NUMBER).description("순서"))).build())));
    }

    @DisplayName("나의 장소 상세 조회")
    @Test
    void getBookmark() throws Exception {
        final Long memberId = 1L;
        final Long bookmarkId = 1L;
        final BookmarkResponse response = BookmarkResponse.builder().bookmarkId(1L).lat(37.501)
            .lng(127.001).address("서울특별시 강남구 테헤란로 123").name("강남역").build();

        given(bookmarkService.getBookmark(memberId, bookmarkId)).willReturn(response);

        final ResultActions result = mockMvc.perform(
            get("/api/members/{id}/bookmarks/{bookmarkId}", memberId, bookmarkId));

        result.andExpect(status().isOk()).andDo(
            document("나의 장소 상세 조회", preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()), resource(
                    ResourceSnippetParameters.builder().tag(tag).summary("나의 장소 상세 조회")
                        .pathParameters(
                            parameterWithName("id").type(SimpleType.NUMBER).description("유저 ID"),
                            parameterWithName("bookmarkId").type(SimpleType.NUMBER)
                                .description("북마크 ID")).responseSchema(schema("BookmarkResponse"))
                        .responseFields(ArrayUtils.addAll(commonResponseFormat(),
                            fieldWithPath("data.bookmarkId").type(JsonFieldType.NUMBER)
                                .description("북마크 ID"),
                            fieldWithPath("data.lat").type(JsonFieldType.NUMBER).description("위도"),
                            fieldWithPath("data.lng").type(JsonFieldType.NUMBER).description("경도"),
                            fieldWithPath("data.address").type(JsonFieldType.STRING)
                                .description("주소"),
                            fieldWithPath("data.name").type(JsonFieldType.STRING).description("별칭"),
                            fieldWithPath("data.sequence").type(JsonFieldType.NUMBER)
                                .description("조회 순서"))).build())));
    }

    @DisplayName("나의 장소 저장")
    @Test
    void saveBookmark() throws Exception {
        final Long memberId = 1L;

        final BookmarkRequest request = BookmarkRequest.builder().lat(37.501).lng(127.001)
            .address("서울특별시 강남구 테헤란로 123").name("강남역").build();

        final BookmarkResponse response = BookmarkResponse.builder().bookmarkId(1L).lat(37.501)
            .lng(127.001).address("서울특별시 강남구 테헤란로 123").name("강남역").sequence(1).build();

        given(bookmarkService.createBookmark(any(BookmarkRequest.class), eq(memberId))).willReturn(
            response);

        final ResultActions result = mockMvc.perform(
            post("/api/members/{id}/bookmarks", memberId).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        result.andExpect(status().isCreated()).andDo(
            document("나의 장소 저장", preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()), resource(
                    ResourceSnippetParameters.builder().tag(tag).summary("나의 장소 저장").pathParameters(
                            parameterWithName("id").type(SimpleType.NUMBER).description("유저 ID"))
                        .requestSchema(schema("BookmarkCreateRequest")).requestFields(
                            fieldWithPath("lat").type(JsonFieldType.NUMBER).description("위도"),
                            fieldWithPath("lng").type(JsonFieldType.NUMBER).description("경도"),
                            fieldWithPath("address").type(JsonFieldType.STRING).description("주소"),
                            fieldWithPath("name").type(JsonFieldType.STRING).description("이름"))
                        .responseSchema(schema("BookmarkResponse")).responseFields(
                            ArrayUtils.addAll(commonResponseFormat(),
                                fieldWithPath("data.bookmarkId").type(JsonFieldType.NUMBER)
                                    .description("북마크 ID"),
                                fieldWithPath("data.lat").type(JsonFieldType.NUMBER).description("위도"),
                                fieldWithPath("data.lng").type(JsonFieldType.NUMBER).description("경도"),
                                fieldWithPath("data.address").type(JsonFieldType.STRING)
                                    .description("주소"),
                                fieldWithPath("data.name").type(JsonFieldType.STRING).description("별칭"),
                                fieldWithPath("data.sequence").type(JsonFieldType.NUMBER)
                                    .description("조회 순서"))).build())));
    }

    @DisplayName("나의 장소 수정")
    @Test
    void updateBookmark() throws Exception {
        final Long memberId = 1L;
        final Long bookmarkId = 1L;

        final BookmarkRequest request = BookmarkRequest.builder().lat(37.501).lng(127.001)
            .address("수정된 주소").name("수정된 별칭").build();

        final BookmarkResponse response = BookmarkResponse.builder().bookmarkId(1L).lat(37.501)
            .lng(127.001).address("수정된 주소").name("수정된 별칭").sequence(1).build();

        given(bookmarkService.updateBookmark(any(BookmarkRequest.class), eq(bookmarkId),
            eq(memberId))).willReturn(response);

        final ResultActions result = mockMvc.perform(
            patch("/api/members/{id}/bookmarks/{bookmarkId}", memberId, bookmarkId).contentType(
                MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)));

        result.andExpect(status().isOk()).andDo(
            document("나의 장소 수정", preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()), resource(
                    ResourceSnippetParameters.builder().tag(tag).summary("나의 장소 수정").pathParameters(
                            parameterWithName("id").type(SimpleType.NUMBER).description("유저 ID"),
                            parameterWithName("bookmarkId").type(SimpleType.NUMBER)
                                .description("북마크 ID")).requestSchema(schema("BookmarkUpdateRequest"))
                        .requestFields(
                            fieldWithPath("lat").type(JsonFieldType.NUMBER).description("위도"),
                            fieldWithPath("lng").type(JsonFieldType.NUMBER).description("경도"),
                            fieldWithPath("address").type(JsonFieldType.STRING).description("주소"),
                            fieldWithPath("name").type(JsonFieldType.STRING).description("이름"))
                        .responseSchema(schema("BookmarkResponse")).responseFields(
                            ArrayUtils.addAll(commonResponseFormat(),
                                fieldWithPath("data.bookmarkId").type(JsonFieldType.NUMBER)
                                    .description("북마크 ID"),
                                fieldWithPath("data.lat").type(JsonFieldType.NUMBER).description("위도"),
                                fieldWithPath("data.lng").type(JsonFieldType.NUMBER).description("경도"),
                                fieldWithPath("data.address").type(JsonFieldType.STRING)
                                    .description("주소"),
                                fieldWithPath("data.name").type(JsonFieldType.STRING).description("별칭"),
                                fieldWithPath("data.sequence").type(JsonFieldType.NUMBER)
                                    .description("조회 순서"))).build())));
    }

    @DisplayName("나의 장소 삭제")
    @Test
    void deleteBookmark() throws Exception {
        final Long memberId = 1L;
        final List<Long> bookmarkIds = List.of(1L, 2L, 3L);

        doNothing().when(bookmarkService).deleteBookmark(bookmarkIds, memberId);

        final ResultActions result = mockMvc.perform(
            delete("/api/members/{id}/bookmarks", memberId).param("bookmarkIds",
                bookmarkIds.stream().map(String::valueOf).toArray(String[]::new)));
        // DELETE /bookmarks?bookmarkIds=1,2,3

        result.andExpect(status().isOk()).andDo(
            document("나의 장소 삭제", preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()), resource(
                    ResourceSnippetParameters.builder().tag(tag).summary("나의 장소 삭제").pathParameters(
                            parameterWithName("id").type(SimpleType.NUMBER).description("유저 ID"))
                        .formParameters(parameterWithName("bookmarkIds").type(SimpleType.NUMBER)
                            .description(
                                "삭제할 북마크 ID 배열 (예: /api/members/1/bookmarks?bookmarkIds=1,2,3)"))
                        .build())));
    }

    @DisplayName("나의 장소 순서 변경")
    @Test
    void updateBookmarkSequence() throws Exception {
        final Long memberId = 1L;
        final List<BookmarkSequenceUpdateRequest> requests = List.of(
            new BookmarkSequenceUpdateRequest(1L, 3), new BookmarkSequenceUpdateRequest(2L, 5),
            new BookmarkSequenceUpdateRequest(3L, 9));

        final List<BookmarkResponse> responses = List.of(
            BookmarkResponse.builder().bookmarkId(1L).lat(37.501).lng(127.001).address("수정된 주소1")
                .name("수정된 별칭1").sequence(3).build(),
            BookmarkResponse.builder().bookmarkId(2L).lat(37.501).lng(127.001).address("수정된 주소2")
                .name("수정된 별칭2").sequence(5).build(),
            BookmarkResponse.builder().bookmarkId(3L).lat(37.501).lng(127.001).address("수정된 주소3")
                .name("수정된 별칭3").sequence(9).build());

        given(bookmarkService.updateBookmarkSequences(memberId, requests)).willReturn(responses);
        final ResultActions result = mockMvc.perform(
            patch("/api/members/{id}/bookmarks/sequence/reorder", memberId).contentType(
                MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(requests)));

        result.andExpect(status().isOk()).andDo(
            document("나의 장소 순서 변경", preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()), resource(
                    ResourceSnippetParameters.builder().tag(tag).summary("나의 장소 순서 변경")
                        .pathParameters(
                            parameterWithName("id").type(SimpleType.NUMBER).description("유저 ID"))
                        .responseSchema(schema("BookmarkResponseList")).responseFields(
                            ArrayUtils.addAll(commonResponseFormat(),
                                fieldWithPath("data[].bookmarkId").type(JsonFieldType.NUMBER)
                                    .description("북마크 ID"),
                                fieldWithPath("data[].lat").type(JsonFieldType.NUMBER)
                                    .description("위도"),
                                fieldWithPath("data[].lng").type(JsonFieldType.NUMBER)
                                    .description("경도"),
                                fieldWithPath("data[].address").type(JsonFieldType.STRING)
                                    .description("주소"),
                                fieldWithPath("data[].name").type(JsonFieldType.STRING)
                                    .description("별칭"),
                                fieldWithPath("data[].sequence").type(JsonFieldType.NUMBER)
                                    .description("조회 순서"))).build())));
    }

    @DisplayName("최근 경로 저장")
    @Test
    void saveRecentPath() throws Exception {
        final Long memberId = 1L;
        final RecentPathRequest request = RecentPathRequest.builder().lat(37.501).lng(127.001)
            .name("Recent Path").build();
        final RecentPathResponse response = RecentPathResponse.builder().recentPathId(1L)
            .lat(37.501).lng(127.001).name("Recent Path").lastAccessedAt(LocalDateTime.now())
            .isBookmarked(false).build();

        given(recentPathService.saveRecentPath(eq(memberId),
            any(RecentPathRequest.class))).willReturn(response);

        final ResultActions result = mockMvc.perform(
            post("/api/members/{id}/recent-path", memberId).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        result.andExpect(status().isCreated()).andDo(
            document("최근 경로 저장", preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()), resource(
                    ResourceSnippetParameters.builder().tag(tag).summary("최근 경로 저장").pathParameters(
                            parameterWithName("id").type(SimpleType.NUMBER).description("유저 ID"))
                        .requestSchema(schema("RecentPathRequest"))
                        .requestFields(fieldWithPath("name").description("최근 경로 이름"),
                            fieldWithPath("lat").description("위도"),
                            fieldWithPath("lng").description("경도"))
                        .responseSchema(schema("RecentPathResponse")).responseFields(
                            ArrayUtils.addAll(commonResponseFormat(),
                                fieldWithPath("data.recentPathId").type(JsonFieldType.NUMBER)
                                    .description("최근 경로 ID"),
                                fieldWithPath("data.lat").type(JsonFieldType.NUMBER).description("위도"),
                                fieldWithPath("data.lng").type(JsonFieldType.NUMBER).description("경도"),
                                fieldWithPath("data.name").type(JsonFieldType.STRING)
                                    .description("최근 경로 이름"),
                                fieldWithPath("data.lastAccessedAt").type(JsonFieldType.STRING)
                                    .description("최근 방문 시각"),
                                fieldWithPath("data.bookmarked").type(JsonFieldType.BOOLEAN)
                                    .description("나의 목적지와의 연관관계 여부"))).build())));
    }

    @DisplayName("최근 경로 목록 조회")
    @Test
    void getRecentPathList() throws Exception {
        final Long memberId = 1L;

        final List<RecentPathResponse> list = List.of(
            RecentPathResponse.builder().recentPathId(1L).lat(37.501).lng(127.001)
                .name("Recent Path").lastAccessedAt(LocalDateTime.now()).isBookmarked(false)
                .build(),
            RecentPathResponse.builder().recentPathId(2L).lat(37.501).lng(127.001)
                .name("Recent Path").lastAccessedAt(LocalDateTime.now()).isBookmarked(false)
                .build(),
            RecentPathResponse.builder().recentPathId(3L).lat(37.501).lng(127.001)
                .name("Recent Path").lastAccessedAt(LocalDateTime.now()).isBookmarked(false)
                .build());

        given(recentPathService.getRecentPathList(memberId)).willReturn(list);

        final ResultActions result = mockMvc.perform(
            get("/api/members/{id}/recent-path", memberId));

        result.andExpect(status().isOk()).andDo(
            document("최근 경로 목록 조회 (최대 10개)", preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()), resource(
                    ResourceSnippetParameters.builder().tag(tag).summary("최근 경로 목록 조회 (최대 10개)")
                        .pathParameters(
                            parameterWithName("id").type(SimpleType.NUMBER).description("유저 ID"))
                        .responseSchema(schema("RecentPathResponse")).responseFields(
                            ArrayUtils.addAll(commonResponseFormat(),
                                fieldWithPath("data[].recentPathId").type(JsonFieldType.NUMBER)
                                    .description("최근 경로 ID"),
                                fieldWithPath("data[].lat").type(JsonFieldType.NUMBER)
                                    .description("위도"),
                                fieldWithPath("data[].lng").type(JsonFieldType.NUMBER)
                                    .description("경도"),
                                fieldWithPath("data[].name").type(JsonFieldType.STRING)
                                    .description("최근 경로 이름"),
                                fieldWithPath("data[].lastAccessedAt").type(JsonFieldType.STRING)
                                    .description("최근 방문 시각"),
                                fieldWithPath("data[].bookmarked").type(JsonFieldType.BOOLEAN)
                                    .description("나의 목적지와의 연관관계 여부"))).build())));
    }

    @DisplayName("기본 회원가입 성공")
    @Test
    void successJoin() throws Exception {
        // given
        MemberJoinRequest memberJoinRequest = MemberJoinRequest.builder()
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
            "memberJoinRequest", "",
            MediaType.APPLICATION_JSON_VALUE,
            objectMapper.writeValueAsBytes(memberJoinRequest)
        );

        // when, then
        mockMvc.perform(multipart("/api/members/join")
                .file(requestPart)
                .file(file))
            .andExpect(status().isOk())
            .andDo(document("기본 계정 회원가입",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                resource(
                    ResourceSnippetParameters.builder()
                        .tag(tag)
                        .summary("회원가입")
                        .build()
                ),
                requestParts(
                    partWithName("profileImageUrl").description("프로필 이미지 파일"),
                    partWithName("memberJoinRequest").description("회원 가입 정보")
                ),
                requestPartFields("memberJoinRequest",
                    fieldWithPath("email").description("이메일"),
                    fieldWithPath("password").description("비밀번호"),
                    fieldWithPath("nickname").description("닉네임")
                    )
                ));
    }

    @DisplayName("비밀번호 재설정")
    @Test
    void successResetPassword() throws Exception {
        // given
        ResetPasswordRequest resetPasswordRequest = new ResetPasswordRequest("test@test.com",
            "new1234");
        ApiResponse<Object> apiResponse = ApiResponse.createSuccessWithNoData();
        ResponseEntity<ApiResponse<Object>> response = ResponseEntity.ok(apiResponse);
        when(memberService.resetPassword(any(ResetPasswordRequest.class))).thenReturn(response);

        // when, then
        mockMvc.perform(post("/api/members/password-reset")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(resetPasswordRequest)))
            .andExpect(status().isOk())
            .andDo(MockMvcRestDocumentation.document("비밀번호 재설정",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    resource(
                        ResourceSnippetParameters.builder()
                            .tag(tag)
                            .summary("비밀번호 재설정")
                            .requestFields(
                                fieldWithPath("email").type(JsonFieldType.STRING)
                                    .description("비밀번호를 변경하고자 하는 이메일"),
                                fieldWithPath("newPassword").type(JsonFieldType.STRING)
                                    .description("새로운 비밀번호")
                            )
                            .build()
                    )
                )
            );
    }

    @DisplayName("계정 복구")
    @Test
    void successRestore() throws Exception {
        // given
        MemberRestoreRequest memberRestoreRequest = new MemberRestoreRequest("test@test.com");

        final MemberResponse memberResponse = MemberResponse.builder().memberId(1l)
            .memberStatus(MemberStatus.ACTIVITY)
            .role(MemberRole.USER)
            .email(memberRestoreRequest.getEmail())
            .nickname("temp_nickname")
            .profileImageUrl("profile_image.jpeg")
            .build();

        ApiResponse<MemberResponse> apiResponse = ApiResponse.createSuccess(memberResponse);
        when(memberService.restore(any(MemberRestoreRequest.class))).thenReturn(ResponseEntity.ok(apiResponse));

        // when, then
        mockMvc.perform(post("/api/members/restore")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(memberRestoreRequest)))
            .andExpect(status().isOk())
            .andDo(print())
            .andDo(MockMvcRestDocumentation.document("계정 복구",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    resource(
                        ResourceSnippetParameters.builder()
                            .tag(tag)
                            .summary("계정 복구")
                            .requestFields(
                                fieldWithPath("email").type(JsonFieldType.STRING)
                                    .description("계정을 복구하고자 하는 이메일")
                            )
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
}