package org.programmers.signalbuddyfinal.domain.admin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.programmers.signalbuddyfinal.domain.feedback.dto.FeedbackResponse;
import org.programmers.signalbuddyfinal.domain.feedback.dto.FeedbackSearchCondition;
import org.programmers.signalbuddyfinal.domain.feedback.dto.FeedbackSearchRequest;
import org.programmers.signalbuddyfinal.domain.feedback.entity.enums.AnswerStatus;
import org.programmers.signalbuddyfinal.domain.feedback.entity.enums.FeedbackCategory;
import org.programmers.signalbuddyfinal.domain.feedback.repository.FeedbackRepository;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberRole;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberStatus;
import org.programmers.signalbuddyfinal.global.constant.SearchTarget;
import org.programmers.signalbuddyfinal.global.dto.CustomUser2Member;
import org.programmers.signalbuddyfinal.global.dto.PageResponse;
import org.programmers.signalbuddyfinal.global.exception.BusinessException;
import org.programmers.signalbuddyfinal.global.exception.GlobalErrorCode;
import org.programmers.signalbuddyfinal.global.security.basic.CustomUserDetails;
import org.programmers.signalbuddyfinal.global.support.IntegrationTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

class AdminFeedbackServiceTest extends IntegrationTest {

    @Autowired
    private AdminFeedbackService adminFeedbackService;

    @MockitoBean
    private FeedbackRepository feedbackRepository;

    @DisplayName("관리자용 피드백 조회를 한다.")
    @Test
    void searchFeedbackList_Success() {
        // Given
        CustomUser2Member admin = getCurrentMember(1L, MemberRole.ADMIN);
        Sort sort = Sort.by(Sort.Order.desc("createdAt"));
        Pageable pageable = PageRequest.of(1, 10, sort);
        SearchTarget target = SearchTarget.SUBJECT_OR_CONTENT;
        String keyword = "test";
        Set<FeedbackCategory> categories = Set.of(FeedbackCategory.DELAY, FeedbackCategory.ETC);
        FeedbackSearchRequest request = new FeedbackSearchRequest(keyword, AnswerStatus.BEFORE, categories);
        Boolean deleted = Boolean.FALSE;
        LocalDate startDate = LocalDate.of(2024, 11, 12);
        LocalDate endDate = LocalDate.of(2025, 2, 12);

        List<FeedbackResponse> contents = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            contents.add(
                FeedbackResponse.builder()
                    .feedbackId((long) i + 1)
                    .category(FeedbackCategory.DELAY)
                    .likeCount((long) 0)
                    .subject("test subject " + i)
                    .content("test content " + i)
                    .answerStatus(AnswerStatus.BEFORE)
                    .createdAt(LocalDateTime.of(2024, 11, 1 + i, 10, 10, 10))
                    .updatedAt(LocalDateTime.of(2024, 11, 1 + i, 10, 10, 10))
                    .build()
            );
        }

        when(
            feedbackRepository.findAllByFilter(
                any(Pageable.class), any(FeedbackSearchCondition.class)
            )
        ).thenReturn(new PageImpl<>(contents, pageable, 123));

        // When
        PageResponse<FeedbackResponse> actual = adminFeedbackService.searchFeedbackList(
            pageable, target, request, deleted, startDate, endDate, admin
        );

        // Then
        SoftAssertions.assertSoftly(softAssertions -> {
            softAssertions.assertThat(actual.getCurrentPageNumber()).isOne();
            softAssertions.assertThat(actual.getPageSize()).isEqualTo(10);
            softAssertions.assertThat(actual.getTotalElements()).isEqualTo(123);
            softAssertions.assertThat(actual.isHasNext()).isTrue();
            softAssertions.assertThat(actual.getSearchResults()).isNotEmpty();
            softAssertions.assertThat(actual.getSearchResults().get(5).getFeedbackId())
                .isEqualTo(6);
            softAssertions.assertThat(actual.getSearchResults().get(5).getContent())
                .isEqualTo("test content 5");
        });
    }

    @DisplayName("관리자용 피드백 조회 메서드를 일반 사용자가 이용하면 실패한다.")
    @Test
    void searchFeedbackList_Failure() {
        // Given
        CustomUser2Member user = getCurrentMember(1L, MemberRole.USER);

        // When & Then
        try {
            adminFeedbackService.searchFeedbackList(
                null, SearchTarget.SUBJECT_OR_CONTENT,
                null, null, null, null, user
            );
        } catch (BusinessException e) {
            assertThat(e.getErrorCode()).isEqualTo(GlobalErrorCode.ADMIN_ONLY);
        }
    }

    private CustomUser2Member getCurrentMember(Long id, MemberRole role) {
        return new CustomUser2Member(
            new CustomUserDetails(id, "", "",
                "", "", role, MemberStatus.ACTIVITY));
    }
}