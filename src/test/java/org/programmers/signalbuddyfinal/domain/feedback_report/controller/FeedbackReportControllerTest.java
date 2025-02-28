package org.programmers.signalbuddyfinal.domain.feedback_report.controller;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.programmers.signalbuddyfinal.global.support.RestDocsFormatGenerators.commonResponseWithMemberFormat;
import static org.programmers.signalbuddyfinal.global.support.RestDocsFormatGenerators.getTokenExample;
import static org.programmers.signalbuddyfinal.global.support.RestDocsFormatGenerators.jwtFormat;
import static org.programmers.signalbuddyfinal.global.support.RestDocsFormatGenerators.pageResponseWithMemberFormat;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.SimpleType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.programmers.signalbuddyfinal.domain.feedback_report.dto.FeedbackReportRequest;
import org.programmers.signalbuddyfinal.domain.feedback_report.dto.FeedbackReportResponse;
import org.programmers.signalbuddyfinal.domain.feedback_report.dto.FeedbackReportSearchRequest;
import org.programmers.signalbuddyfinal.domain.feedback_report.dto.FeedbackReportUpdateRequest;
import org.programmers.signalbuddyfinal.domain.feedback_report.entity.enums.FeedbackReportCategory;
import org.programmers.signalbuddyfinal.domain.feedback_report.entity.enums.FeedbackReportStatus;
import org.programmers.signalbuddyfinal.domain.feedback_report.service.FeedbackReportService;
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
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.shaded.org.apache.commons.lang3.ArrayUtils;

@WebMvcTest(FeedbackReportController.class)
class FeedbackReportControllerTest extends ControllerTest {

    @MockitoBean
    private FeedbackReportService reportService;

    private final String tag = "Feedback Report API";

    @DisplayName("일반 사용자가 피드백을 신고한다.")
    @Test
    @WithMockCustomUser
    void writeFeedbackReport() throws Exception {
        // Given
        Long feedbackId = 1L;
        String content = "feedback report test";
        FeedbackReportCategory category = FeedbackReportCategory.ETC;
        FeedbackReportRequest request = FeedbackReportRequest.builder()
            .content(content).category(category)
            .build();
        FeedbackReportResponse response = FeedbackReportResponse.builder()
            .feedbackReportId(1L).content(content).category(category)
            .member(makeMemberResponse()).feedbackId(feedbackId)
            .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
            .status(FeedbackReportStatus.PENDING)
            .build();

        given(
            reportService.writeFeedbackReport(
                anyLong(), any(FeedbackReportRequest.class), any(CustomUser2Member.class)
            )
        ).willReturn(response);

        // When
        ResultActions result = mockMvc.perform(
            post("/api/feedbacks/{feedbackId}/reports", feedbackId)
                .header(HttpHeaders.AUTHORIZATION, getTokenExample())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        );

        // Then
        result.andExpect(status().isCreated())
            .andExpect(
                jsonPath("$.data.feedbackReportId")
                    .value(response.getFeedbackReportId())
            )
            .andDo(
                document(
                    "피드백 신고 작성",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    resource(
                        ResourceSnippetParameters.builder()
                            .tag(tag)
                            .summary("피드백 신고 작성")
                            .requestHeaders(
                                jwtFormat()
                            )
                            .pathParameters(
                                parameterWithName("feedbackId").type(SimpleType.NUMBER)
                                    .description("신고할 피드백 ID(PK)")
                            )
                            .requestFields(
                                fieldWithPath("category").type(JsonFieldType.STRING)
                                        .description("""
                                            피드백 신고 유형 (택 1)
                                            - `INAPPROPRIATE` : 부적절함
                                            - `OFFENSIVE` : 욕설/비방
                                            - `SPAM` : 스팸
                                            - `FALSE` : 허위
                                            - `ETC` : 기타
                                            """),
                                fieldWithPath("content").type(JsonFieldType.STRING)
                                    .description("신고 내용")
                            )
                            .responseFields(
                                ArrayUtils.addAll(
                                    commonResponseWithMemberFormat(),
                                    fieldWithPath("data.feedbackReportId")
                                        .type(JsonFieldType.NUMBER)
                                        .description("피드백 신고 ID(PK)"),
                                    fieldWithPath("data.category")
                                        .type(JsonFieldType.STRING)
                                        .description("""
                                            피드백 신고 유형
                                            - `INAPPROPRIATE` : 부적절함
                                            - `OFFENSIVE` : 욕설/비방
                                            - `SPAM` : 스팸
                                            - `FALSE` : 허위
                                            - `ETC` : 기타
                                            """),
                                    fieldWithPath("data.content")
                                        .type(JsonFieldType.STRING)
                                        .description("피드백 신고 내용"),
                                    fieldWithPath("data.status")
                                        .type(JsonFieldType.STRING)
                                        .description("""
                                            피드백 신고 처리 상태
                                            - `PENDING` : 미처리
                                            - `REJECTED` : 신고 반려
                                            - `PROCESSED` : 처리 완료
                                            """),
                                    fieldWithPath("data.createdAt")
                                        .type(JsonFieldType.STRING)
                                        .description("신고 생성일"),
                                    fieldWithPath("data.updatedAt")
                                        .type(JsonFieldType.STRING)
                                        .description("신고 수정일"),
                                    fieldWithPath("data.feedbackId")
                                        .type(JsonFieldType.NUMBER)
                                        .description("신고 대상 (피드백 ID(PK))")
                                )
                            )
                            .build()
                    )
                )
            );
    }

    @DisplayName("관리자가 피드백 신고 목록을 확인한다.")
    @Test
    @WithMockCustomUser(roleType = "ROLE_ADMIN")
    void searchFeedbackReportList() throws Exception {
        // Given
        Pageable pageable = PageRequest.of(
            0, 10, Direction.DESC, "createdAt"
        );

        List<FeedbackReportResponse> reports = new ArrayList<>();
        for (int i = 1; i < 11; i++) {
            MemberResponse member = makeMemberResponse();
            FeedbackReportResponse response = makeReportResponse(member, i);
            reports.add(response);
        }
        PageResponse<FeedbackReportResponse> response = new PageResponse<>(
            new PageImpl<>(reports, pageable, 10)
        );

        given(
            reportService.searchFeedbackReportList(
                any(Pageable.class), any(SearchTarget.class),
                any(FeedbackReportSearchRequest.class),
                any(LocalDate.class), any(LocalDate.class),
                any(CustomUser2Member.class)
            )
        ).willReturn(response);

        // When
        ResultActions result = mockMvc.perform(
            get("/api/feedbacks/reports")
                .header(HttpHeaders.AUTHORIZATION, getTokenExample())
                .queryParam("page", String.valueOf(pageable.getPageNumber()))
                .queryParam("size", String.valueOf(pageable.getPageSize()))
                .queryParam("target", "content")
                .queryParam("keyword", "test")
                .queryParam(
                    "category",
                    FeedbackReportCategory.ETC.getValue(),
                    FeedbackReportCategory.FALSE.getValue()
                )
                .queryParam(
                    "status",
                    FeedbackReportStatus.PENDING.getValue(),
                    FeedbackReportStatus.PROCESSED.getValue()
                )
                .queryParam("start-date", "2024-11-15")
                .queryParam("end-date", "2025-02-15")
        );

        // Then
        result.andExpect(status().isOk())
            .andExpect(
                jsonPath("$.data.searchResults[0].feedbackReportId")
                    .value(response.getSearchResults().get(0).getFeedbackReportId())
            )
            .andDo(
                document(
                    "피드백 신고 목록",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    resource(
                        ResourceSnippetParameters.builder()
                            .tag(tag)
                            .summary("피드백 신고 목록")
                            .requestHeaders(
                                jwtFormat()
                            )
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
                                        - `feedback.feedbackId` : 피드백 ID(PK)
                                        - `feedbackReportId` : 피드백 신고 ID(PK)
                                        - `category` : 신고 유형
                                        - `status` : 신고 처리 상태
                                        - `processedAt` : 처리일
                                        - `createdAt` : 작성일
                                        - `updatedAt` : 수정일
                                        
                                        정렬 순서 (기본값 : `desc`)
                                        - `asc` : 오름차순
                                        - `desc` : 내림차순
                                        """).optional(),
                                parameterWithName("target").type(SimpleType.STRING)
                                    .description("""
                                        피드백 검색 범위 (기본값 : `content`)
                                        - `content` : 신고 내용
                                        - `writer` : 작성자
                                        """).optional(),
                                parameterWithName("keyword").type(SimpleType.STRING)
                                    .description("검색어").optional(),
                                parameterWithName("category").type(SimpleType.STRING)
                                    .description("""
                                            피드백 신고 유형 (여러 개 선택 가능)
                                            - `inappropriate` : 부적절함
                                            - `offensive` : 욕설/비방
                                            - `spam` : 스팸
                                            - `false` : 허위
                                            - `etc` : 기타
                                            """).optional(),
                                parameterWithName("status").type(SimpleType.STRING)
                                    .description("""
                                            피드백 신고 처리 상태 (여러 개 선택 가능)
                                            - `pending` : 미처리
                                            - `rejected` : 신고 반려
                                            - `processed` : 처리 완료
                                            """).optional(),
                                parameterWithName("start-date").type(SimpleType.STRING)
                                    .description("""
                                        시작 날짜 범위 (신고 작성일 기준)
                                        형식 : `yyyy-MM-dd`
                                        """).optional(),
                                parameterWithName("end-date").type(SimpleType.STRING)
                                    .description("""
                                        종료 날짜 범위 (신고 작성일 기준)
                                        형식 : `yyyy-MM-dd`
                                        """).optional()
                            )
                            .responseFields(
                                ArrayUtils.addAll(
                                    pageResponseWithMemberFormat(),
                                    fieldWithPath("data.searchResults[].feedbackReportId")
                                        .type(JsonFieldType.NUMBER)
                                        .description("피드백 신고 ID(PK)"),
                                    fieldWithPath("data.searchResults[].category")
                                        .type(JsonFieldType.STRING)
                                        .description("""
                                            피드백 신고 유형
                                            - `INAPPROPRIATE` : 부적절함
                                            - `OFFENSIVE` : 욕설/비방
                                            - `SPAM` : 스팸
                                            - `FALSE` : 허위
                                            - `ETC` : 기타
                                            """),
                                    fieldWithPath("data.searchResults[].content")
                                        .type(JsonFieldType.STRING)
                                        .description("피드백 신고 내용"),
                                    fieldWithPath("data.searchResults[].status")
                                        .type(JsonFieldType.STRING)
                                        .description("""
                                            피드백 신고 처리 상태
                                            - `PENDING` : 미처리
                                            - `REJECTED` : 신고 반려
                                            - `PROCESSED` : 처리 완료
                                            """),
                                    fieldWithPath("data.searchResults[].processedAt")
                                        .type(JsonFieldType.STRING)
                                        .description("신고 처리일"),
                                    fieldWithPath("data.searchResults[].createdAt")
                                        .type(JsonFieldType.STRING)
                                        .description("신고 생성일"),
                                    fieldWithPath("data.searchResults[].updatedAt")
                                        .type(JsonFieldType.STRING)
                                        .description("신고 수정일"),
                                    fieldWithPath("data.searchResults[].feedbackId")
                                        .type(JsonFieldType.NUMBER)
                                        .description("신고 대상 (피드백 ID(PK))")
                                )
                            )
                            .build()
                    )
                )
            );
    }

    @DisplayName("관리자가 피드백 신고 처리 상태를 변경한다.")
    @Test
    @WithMockCustomUser(roleType = "ROLE_ADMIN")
    void updateFeedbackReports() throws Exception {
        // Given
        Long feedbackId = 1L;
        Long reportId = 1L;
        FeedbackReportUpdateRequest request = FeedbackReportUpdateRequest.builder()
            .status(FeedbackReportStatus.PROCESSED).build();

        doNothing().when(reportService)
            .updateFeedbackReport(anyLong(), anyLong(), eq(request), any(CustomUser2Member.class));

        // When
        ResultActions result = mockMvc.perform(
            patch("/api/feedbacks/{feedbackId}/reports/{reportId}", feedbackId, reportId)
                .header(HttpHeaders.AUTHORIZATION, getTokenExample())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        );

        // Then
        result.andExpect(status().isOk())
            .andDo(
                document(
                    "피드백 신고 처리 상태 수정",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    resource(
                        ResourceSnippetParameters.builder()
                            .tag(tag)
                            .summary("피드백 신고 처리 상태 수정")
                            .requestHeaders(
                                jwtFormat()
                            )
                            .pathParameters(
                                parameterWithName("feedbackId").type(SimpleType.NUMBER)
                                    .description("수정할 신고의 피드백 ID(PK)"),
                                parameterWithName("reportId").type(SimpleType.NUMBER)
                                    .description("수정할 피드백 신고 ID(PK)")
                            )
                            .requestFields(
                                fieldWithPath("status").type(JsonFieldType.STRING)
                                    .description("""
                                        피드백 신고의 수정할 처리 상태
                                        - `PENDING` : 미처리
                                        - `REJECTED` : 신고 반려
                                        - `PROCESSED` : 처리 완료
                                        """)
                            )
                            .build()
                    )
                )
            );
    }

    @DisplayName("관리자가 피드백 신고를 삭제한다.")
    @Test
    @WithMockCustomUser(roleType = "ROLE_ADMIN")
    void deleteFeedbackReport() throws Exception {
        // Given
        Long feedbackId = 1L;
        Long reportId = 1L;

        doNothing().when(reportService)
            .deleteFeedbackReport(anyLong(), anyLong(), any(CustomUser2Member.class));

        // When
        ResultActions result = mockMvc.perform(
            delete("/api/feedbacks/{feedbackId}/reports/{reportId}", feedbackId, reportId)
                .header(HttpHeaders.AUTHORIZATION, getTokenExample())
                .contentType(MediaType.APPLICATION_JSON)
        );

        // Then
        result.andExpect(status().isOk())
            .andDo(
                document(
                    "피드백 신고 삭제",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    resource(
                        ResourceSnippetParameters.builder()
                            .tag(tag)
                            .summary("피드백 신고 삭제")
                            .requestHeaders(
                                jwtFormat()
                            )
                            .pathParameters(
                                parameterWithName("feedbackId").type(SimpleType.NUMBER)
                                    .description("삭제할 신고의 피드백 ID(PK)"),
                                parameterWithName("reportId").type(SimpleType.NUMBER)
                                    .description("삭제할 피드백 신고 ID(PK)")
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

    private FeedbackReportResponse makeReportResponse(MemberResponse member, int num) {
        return FeedbackReportResponse.builder()
            .feedbackReportId((long) num).content("feedback report test " + num)
            .category(
                num % 5 == 0 ?
                FeedbackReportCategory.FALSE : FeedbackReportCategory.ETC
            )
            .member(member).feedbackId((long) num % 5)
            .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
            .status(FeedbackReportStatus.PROCESSED)
            .processedAt(LocalDateTime.now())
            .build();
    }
}