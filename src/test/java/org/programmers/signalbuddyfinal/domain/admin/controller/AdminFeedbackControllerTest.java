package org.programmers.signalbuddyfinal.domain.admin.controller;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.BDDMockito.given;
import static org.programmers.signalbuddyfinal.global.support.RestDocsFormatGenerators.getTokenExample;
import static org.programmers.signalbuddyfinal.global.support.RestDocsFormatGenerators.pageResponseWithMemberFormat;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.SimpleType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.programmers.signalbuddyfinal.domain.admin.service.AdminFeedbackService;
import org.programmers.signalbuddyfinal.domain.feedback.dto.FeedbackResponse;
import org.programmers.signalbuddyfinal.domain.feedback.dto.FeedbackSearchRequest;
import org.programmers.signalbuddyfinal.domain.feedback.entity.enums.AnswerStatus;
import org.programmers.signalbuddyfinal.domain.feedback.entity.enums.FeedbackCategory;
import org.programmers.signalbuddyfinal.domain.member.dto.MemberResponse;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberRole;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberStatus;
import org.programmers.signalbuddyfinal.global.anotation.WithMockCustomUser;
import org.programmers.signalbuddyfinal.global.constant.SearchTarget;
import org.programmers.signalbuddyfinal.global.dto.CustomUser2Member;
import org.programmers.signalbuddyfinal.global.dto.PageResponse;
import org.programmers.signalbuddyfinal.global.support.ControllerTest;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpHeaders;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.shaded.org.apache.commons.lang3.ArrayUtils;

@WebMvcTest(AdminFeedbackController.class)
class AdminFeedbackControllerTest extends ControllerTest {

    @MockitoBean
    private AdminFeedbackService adminFeedbackService;

    private final String tag = "Feedback Admin API";

    @DisplayName("관리자가 피드백을 검색한다.")
    @Test
    @WithMockCustomUser(roleType = "ROLE_ADMIN")
    void searchFeedbackListByAdmin() throws Exception {
        // Given
        Pageable pageable = PageRequest.of(
            1, 10, Direction.DESC, "createdAt"
        );
        List<FeedbackResponse> feedbacks = new ArrayList<>();
        MemberResponse member = makeMemberResponse();
        for (int i = 1; i <= 10; i++) {
            feedbacks.add(makeDeletedFeedbackResponse(member, i));
        }
        PageResponse<FeedbackResponse> response = new PageResponse<>(
            new PageImpl<>(feedbacks, pageable, 23)
        );

        given(
            adminFeedbackService.searchFeedbackList(
                any(Pageable.class), any(SearchTarget.class), any(FeedbackSearchRequest.class), anyBoolean(),
                any(LocalDate.class), any(LocalDate.class), any(CustomUser2Member.class)
            )
        ).willReturn(response);

        // When
        ResultActions result = mockMvc.perform(
            get("/api/admin/feedbacks")
                .header(HttpHeaders.AUTHORIZATION, getTokenExample())
                .queryParam("page", String.valueOf(pageable.getPageNumber()))
                .queryParam("size", String.valueOf(pageable.getPageSize()))
                .queryParam("sort", "createdAt", "desc")
                .queryParam("target", "content")
                .queryParam("keyword", "test")
                .queryParam("deleted", "true")
                .queryParam("status", "before")
                .queryParam("category", "etc", "delay")
                .queryParam("start-date", "2024-10-10")
                .queryParam("end-date", "2025-01-01")
        );

        // Then
        result.andExpect(status().isOk())
            .andExpect(
                jsonPath("$.data.searchResults[2].feedbackId")
                    .value(response.getSearchResults().get(2).getFeedbackId())
            )
            .andDo(
                document(
                    "관리자용 피드백 목록 조회",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    resource(
                        ResourceSnippetParameters.builder()
                            .tag(tag)
                            .summary("관리자용 피드백 목록 조회")
                            .queryParameters(
                                parameterWithName("page").type(SimpleType.NUMBER)
                                    .description("페이지 번호 (기본값 : `0`, 0부터 시작)").optional(),
                                parameterWithName("size").type(SimpleType.NUMBER)
                                    .description("페이지 크기 (기본값 : `10`)").optional(),
                                parameterWithName("sort").type(SimpleType.STRING)
                                    .description("""
                                        정렬 설정
                                        - 형식 : `정렬할_컬럼,정렬_순서`
                                        - ex) createdAt,desc
                                        
                                        정렬할 컬럼 (기본값 : `createdAt`)
                                        - `feedbackId` : 피드백 ID(PK)
                                        - `subject` : 피드백 제목
                                        - `content` : 피드백 내용
                                        - `category` : 피드백 유형
                                        - `status` : 피드백 답변 상태
                                        - `createdAt` : 작성일
                                        - `updatedAt` : 수정일
                                        - `deletedAt` : 삭제일
                                        
                                        정렬 순서 (기본값 : `desc`)
                                        - `asc` : 오름차순
                                        - `desc` : 내림차순
                                        """).optional(),
                                parameterWithName("target").type(SimpleType.STRING)
                                    .description("""
                                        피드백 검색 범위 (기본값 : `content`)
                                        - `content` : 제목 + 내용
                                        - `writer` : 작성자
                                        """).optional(),
                                parameterWithName("keyword").type(SimpleType.STRING)
                                    .description("검색어").optional(),
                                parameterWithName("deleted").type(SimpleType.BOOLEAN)
                                    .description("""
                                        삭제된 데이터 표시 여부 (택 1)
                                        - `true` : 삭제된 데이터만 가져오기
                                        - `false` : 삭제된 데이터 제외
                                        """).optional(),
                                parameterWithName("status").type(SimpleType.STRING)
                                    .description("""
                                        피드백 답변 상태 (택 1)
                                        - `before` : 답변 전
                                        - `completion` : 답변 완료
                                        """).optional(),
                                parameterWithName("category").type(SimpleType.STRING)
                                    .description("""
                                        피드백 유형 (여러 개 선택 가능)
                                        - `delay` : 신호 지연
                                        - `malfunction` : 오작동
                                        - `add-signal` : 신호등 추가
                                        - `etc` : 기타
                                        """).optional(),
                                parameterWithName("start-date").type(SimpleType.STRING)
                                    .description("""
                                        시작 날짜 범위 (피드백 작성일 기준)
                                        형식 : `yyyy-MM-dd`
                                        """).optional(),
                                parameterWithName("end-date").type(SimpleType.STRING)
                                    .description("""
                                        종료 날짜 범위 (피드백 작성일 기준)
                                        형식 : `yyyy-MM-dd`
                                        """).optional()
                            )
                            .responseFields(
                                ArrayUtils.addAll(
                                    feedbackPageResponseDocs(),
                                    fieldWithPath("data.searchResults[].deletedAt")
                                        .description("피드백 삭제일")
                                )
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

    private FeedbackResponse makeDeletedFeedbackResponse(MemberResponse member, int num) {
        return FeedbackResponse.builder()
            .feedbackId((long) num).secret(Boolean.FALSE)
            .subject("test subject" + num).content("test content" + num)
            .answerStatus(AnswerStatus.BEFORE).category(FeedbackCategory.ETC)
            .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
            .deletedAt(LocalDateTime.now().plusDays(21 % num))
            .imageUrl("https://image.com/dfsdfsdf").member(member).likeCount(21L % num)
            .build();
    }

    private FieldDescriptor[] feedbackPageResponseDocs() {
        FieldDescriptor[] pageDocs = pageResponseWithMemberFormat();

        FieldDescriptor[] feedbackListDocs = new FieldDescriptor[10];
        feedbackListDocs[0] = fieldWithPath("data.searchResults[].feedbackId")
            .type(JsonFieldType.NUMBER)
            .description("피드백 ID");
        feedbackListDocs[1] = fieldWithPath("data.searchResults[].subject")
            .type(JsonFieldType.STRING)
            .description("피드백 제목");
        feedbackListDocs[2] = fieldWithPath("data.searchResults[].content")
            .type(JsonFieldType.STRING)
            .description("피드백 내용");
        feedbackListDocs[3] = fieldWithPath("data.searchResults[].category")
            .type(JsonFieldType.STRING)
            .description("""
                피드백 유형
                - `DELAY` : 신호 지연
                - `MALFUNCTION` : 오작동
                - `ADD_SIGNAL` : 신호등 추가
                - `ETC` : 기타
                """);
        feedbackListDocs[4] = fieldWithPath("data.searchResults[].likeCount")
            .type(JsonFieldType.NUMBER)
            .description("피드백의 좋아요 개수");
        feedbackListDocs[5] = fieldWithPath("data.searchResults[].secret")
            .type(JsonFieldType.BOOLEAN)
            .description("비밀글 여부");
        feedbackListDocs[6] = fieldWithPath("data.searchResults[].answerStatus")
            .type(JsonFieldType.STRING)
            .description("""
                피드백의 답변 여부
                - `BEFORE` : 답변 전
                - `COMPLETION` : 답변 완료
                """);
        feedbackListDocs[7] = fieldWithPath("data.searchResults[].imageUrl")
            .type(JsonFieldType.STRING)
            .description("이미지 URL");
        feedbackListDocs[8] = fieldWithPath("data.searchResults[].createdAt")
            .type(JsonFieldType.STRING)
            .description("피드백 작성일");
        feedbackListDocs[9] = fieldWithPath("data.searchResults[].updatedAt")
            .type(JsonFieldType.STRING)
            .description("피드백 수정일");

        return Stream.concat(Arrays.stream(pageDocs), Arrays.stream(feedbackListDocs))
            .toArray(FieldDescriptor[]::new);
    }
}