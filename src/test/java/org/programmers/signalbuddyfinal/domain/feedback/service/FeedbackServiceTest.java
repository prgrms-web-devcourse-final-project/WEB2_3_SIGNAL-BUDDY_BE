package org.programmers.signalbuddyfinal.domain.feedback.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.programmers.signalbuddyfinal.global.support.RestDocsFormatGenerators.getMockImageFile;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.programmers.signalbuddyfinal.domain.crossroad.dto.CrossroadApiResponse;
import org.programmers.signalbuddyfinal.domain.crossroad.entity.Crossroad;
import org.programmers.signalbuddyfinal.domain.crossroad.repository.CrossroadRepository;
import org.programmers.signalbuddyfinal.domain.feedback.dto.FeedbackRequest;
import org.programmers.signalbuddyfinal.domain.feedback.dto.FeedbackResponse;
import org.programmers.signalbuddyfinal.domain.feedback.entity.Feedback;
import org.programmers.signalbuddyfinal.domain.feedback.entity.enums.FeedbackCategory;
import org.programmers.signalbuddyfinal.domain.feedback.exception.FeedbackErrorCode;
import org.programmers.signalbuddyfinal.domain.feedback.repository.FeedbackRepository;
import org.programmers.signalbuddyfinal.domain.member.entity.Member;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberRole;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberStatus;
import org.programmers.signalbuddyfinal.domain.member.repository.MemberRepository;
import org.programmers.signalbuddyfinal.global.service.AwsFileService;
import org.programmers.signalbuddyfinal.global.dto.CustomUser2Member;
import org.programmers.signalbuddyfinal.global.dto.PageResponse;
import org.programmers.signalbuddyfinal.global.exception.BusinessException;
import org.programmers.signalbuddyfinal.global.security.basic.CustomUserDetails;
import org.programmers.signalbuddyfinal.global.support.ServiceTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

class FeedbackServiceTest extends ServiceTest {

    @Autowired
    private FeedbackService feedbackService;

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private CrossroadRepository crossroadRepository;

    @Autowired
    private MemberRepository memberRepository;

    @MockitoBean
    private AwsFileService awsFileService;

    private Member member;
    private Crossroad crossroad;
    private final String imageFormName = "imageFile";

    @BeforeEach
    void setup() {
        member = saveMember("test@test.com", "tester");
        crossroad = saveCrossroad("13214", "00사거리", 37.12222, 127.12132);

        saveFeedback("test subject", "test content", member, crossroad);
        saveFeedback("test subject2", "test content2", member, crossroad);
        saveFeedback("test subject3", "test content3", member, crossroad);
        saveSoftDeleteFeedback("test subject4", "test content4", member, crossroad);
    }

    @DisplayName("피드백을 작성한다.")
    @Test
    void writeFeedback() {
        // Given
        String subject = "test subjcet";
        String content = "test content";
        Long crossroadId = crossroad.getCrossroadId();
        FeedbackRequest request = FeedbackRequest.builder()
            .subject(subject).content(content).secret(Boolean.FALSE)
            .category(FeedbackCategory.ETC).crossroadId(crossroadId)
            .build();
        MockMultipartFile imageFile = getMockImageFile(imageFormName);
        CustomUser2Member user = getCurrentMember(member.getMemberId(), MemberRole.USER);

        // When
        Resource mockResource = mock(UrlResource.class);
        when(awsFileService.saveProfileImage(any(MockMultipartFile.class)))
            .thenReturn(imageFile.getName());
        when(awsFileService.getProfileImage(anyString())).thenReturn(mockResource);
        FeedbackResponse actual = feedbackService.writeFeedback(request, imageFile, user);

        // Then
        SoftAssertions.assertSoftly(softAssertions -> {
            softAssertions.assertThat(actual.getFeedbackId()).isNotNull();
            softAssertions.assertThat(actual.getSubject()).isEqualTo(subject);
            softAssertions.assertThat(actual.getCrossroad().getCrossroadId())
                .isEqualTo(crossroadId);
            softAssertions.assertThat(actual.getMember().getMemberId())
                .isEqualTo(user.getMemberId());
            softAssertions.assertThat(actual.getImageUrl()).isNotNull();
        });
    }

    @DisplayName("사용자가 작성한 피드백 목록 조회한다.")
    @Test
    void getFeedbacksByMember() {
        // feedbackNoMemberDto
        final PageResponse<FeedbackResponse> feedbacks = feedbackService.findPagedExcludingMember(
            member.getMemberId(), Pageable.ofSize(10));

        assertThat(feedbacks).isNotNull();
        assertThat(feedbacks.getTotalElements()).isEqualTo(3);

        // Member 조회 결과가 없어야 함.
        assertThat(feedbacks.getSearchResults()).isNotEmpty().allSatisfy(
            feedback -> assertThat(feedback.getMember()).isNull());
    }

    @DisplayName("피드백 상세 조회를 한다.")
    @Test
    void searchFeedbackDetail() {
        // Given
        Long feedbackId = 3L;

        // When
        FeedbackResponse response = feedbackService.searchFeedbackDetail(feedbackId, null);

        // Then
        SoftAssertions.assertSoftly(softAssertions -> {
            assertThat(response.getFeedbackId()).isEqualTo(feedbackId);
            assertThat(response.getMember().getMemberId())
                .isEqualTo(member.getMemberId());
            assertThat(response.getCrossroad().getCrossroadId())
                .isEqualTo(crossroad.getCrossroadId());
        });
    }

    @DisplayName("작성자 본인이 비밀글을 상세 조회를 한다.")
    @Test
    void searchSecretFeedbackDetail_Success() {
        // Given
        Feedback secret = saveSecretFeedback(
            "test subject5", "test content5", member, crossroad
        );
        CustomUser2Member user = getCurrentMember(member.getMemberId(), MemberRole.USER);

        // When
        FeedbackResponse response = feedbackService.searchFeedbackDetail(
            secret.getFeedbackId(), user
        );

        // Then
        SoftAssertions.assertSoftly(softAssertions -> {
            assertThat(response.getFeedbackId()).isEqualTo(secret.getFeedbackId());
            assertThat(response.getSecret()).isTrue();
            assertThat(response.getMember().getMemberId())
                .isEqualTo(member.getMemberId());
            assertThat(response.getCrossroad().getCrossroadId())
                .isEqualTo(crossroad.getCrossroadId());
        });
    }

    @DisplayName("작성자가 아닌 일반 사용자가 비밀글을 상세 조회하면 실패한다.")
    @Test
    void searchSecretFeedbackDetail_Failure() {
        // Given
        Feedback secret = saveSecretFeedback(
            "test subject5", "test content5", member, crossroad
        );
        Member otherMember = saveMember("test2@test.com", "tester2");
        CustomUser2Member user = getCurrentMember(otherMember.getMemberId(), MemberRole.USER);

        // When & Then
        try {
            feedbackService.searchFeedbackDetail(secret.getFeedbackId(), user);
        } catch (BusinessException e) {
            assertThat(e.getErrorCode())
                .isEqualTo(FeedbackErrorCode.SECRET_FEEDBACK_NOT_AUTHORIZED);
        }
    }

    @DisplayName("관리자가 다른 사용자의 비밀글을 상세 조회를 한다.")
    @Test
    void searchSecretFeedbackDetailByAdmin_Success() {
        // Given
        Feedback secret = saveSecretFeedback(
            "test subject5", "test content5", member, crossroad
        );
        Member admin = saveAdmin("test@test.com", "tester2");
        CustomUser2Member user = getCurrentMember(admin.getMemberId(), MemberRole.ADMIN);

        // When
        FeedbackResponse response = feedbackService.searchFeedbackDetail(
            secret.getFeedbackId(), user
        );

        // Then
        SoftAssertions.assertSoftly(softAssertions -> {
            assertThat(response.getFeedbackId()).isEqualTo(secret.getFeedbackId());
            assertThat(response.getSecret()).isTrue();
            assertThat(response.getMember().getMemberId())
                .isEqualTo(member.getMemberId());
            assertThat(response.getMember().getMemberId())
                .isNotEqualTo(user.getMemberId());
            assertThat(response.getCrossroad().getCrossroadId())
                .isEqualTo(crossroad.getCrossroadId());
        });
    }

    @DisplayName("피드백을 수정한다.")
    @Test
    void updateFeedback() {
        // Given
        Feedback feedback = saveFeedback(
            "test subject", "test content", member, crossroad
        );
        Long feedbackId = feedback.getFeedbackId();
        FeedbackCategory updatedCategory = FeedbackCategory.DELAY;
        String updatedContent = "update test content";
        FeedbackRequest request = FeedbackRequest.builder()
            .subject(feedback.getSubject()).content(updatedContent).secret(Boolean.FALSE)
            .category(updatedCategory).crossroadId(crossroad.getCrossroadId())
            .build();
        MockMultipartFile updatedImageFile = getMockImageFile(imageFormName);
        CustomUser2Member user = getCurrentMember(member.getMemberId(), MemberRole.USER);

        // When
        Resource mockResource = mock(UrlResource.class);
        when(awsFileService.saveProfileImage(any(MockMultipartFile.class)))
            .thenReturn(updatedImageFile.getName());
        when(awsFileService.getProfileImage(anyString())).thenReturn(mockResource);
        FeedbackResponse actual = feedbackService.updateFeedback(
            feedbackId, request, updatedImageFile, user
        );

        // Then
        SoftAssertions.assertSoftly(softAssertions -> {
            softAssertions.assertThat(actual.getFeedbackId()).isEqualTo(feedbackId);
            softAssertions.assertThat(actual.getContent()).isEqualTo(updatedContent);
            softAssertions.assertThat(actual.getCategory()).isEqualTo(updatedCategory);
            softAssertions.assertThat(actual.getImageUrl()).isEqualTo(mockResource.toString());
        });
    }

    @DisplayName("작성자가 아닌 일반 사용자가 피드백을 수정하면 실패한다.")
    @Test
    void updateFeedback_Failure() {
        // Given
        Long feedbackId = 2L;
        FeedbackRequest request = FeedbackRequest.builder()
            .subject("updated").content("aaaa").crossroadId(1L)
            .category(FeedbackCategory.ETC).secret(Boolean.FALSE)
            .build();
        Member otherMember = saveMember("test@test.com", "tester2");
        CustomUser2Member user = getCurrentMember(otherMember.getMemberId(), MemberRole.USER);

        // When & Then
        try {
            feedbackService.updateFeedback(feedbackId, request, null, user);
        } catch (BusinessException e) {
            assertThat(e.getErrorCode())
                .isEqualTo(FeedbackErrorCode.FEEDBACK_MODIFIER_NOT_AUTHORIZED);
        }
    }

    @DisplayName("피드백을 삭제한다.")
    @Test
    void deleteFeedback_Success() {
        // Given
        Long feedbackId = 2L;
        CustomUser2Member user = getCurrentMember(member.getMemberId(), MemberRole.USER);

        // When
        feedbackService.deleteFeedback(feedbackId, user);

        // Then
        assertThat(feedbackRepository.findById(feedbackId)).isEmpty();
    }

    @DisplayName("작성자가 아닌 일반 사용자가 피드백을 삭제하면 실패한다.")
    @Test
    void deleteFeedback_Failure() {
        // Given
        Long feedbackId = 2L;
        Member otherMember = saveMember("test@test.com", "tester2");
        CustomUser2Member user = getCurrentMember(otherMember.getMemberId(), MemberRole.USER);

        // When & Then
        try {
            feedbackService.deleteFeedback(feedbackId, user);
        } catch (BusinessException e) {
            assertThat(e.getErrorCode())
                .isEqualTo(FeedbackErrorCode.FEEDBACK_ELIMINATOR_NOT_AUTHORIZED);
        }
    }

    @DisplayName("관리자가 다른 사용자의 피드백을 삭제한다.")
    @Test
    void deleteFeedbackByAdmin() {
        // Given
        Long feedbackId = 2L;
        Member admin = saveAdmin("test@test.com", "tester2");
        CustomUser2Member user = getCurrentMember(admin.getMemberId(), MemberRole.ADMIN);

        // When
        feedbackService.deleteFeedback(feedbackId, user);

        // Then
        assertThat(feedbackRepository.findById(feedbackId)).isEmpty();
    }

    private Member saveMember(String email, String nickname) {
        return memberRepository.save(
            Member.builder().email(email).password("123456").role(MemberRole.USER)
                .nickname(nickname).memberStatus(MemberStatus.ACTIVITY)
                .profileImageUrl("https://test-image.com/test-123131").build());
    }

    private Member saveAdmin(String email, String nickname) {
        return memberRepository.save(
            Member.builder().email(email).password("123456").role(MemberRole.ADMIN)
                .nickname(nickname).memberStatus(MemberStatus.ACTIVITY)
                .profileImageUrl("https://test-image.com/test-123131").build());
    }

    private Crossroad saveCrossroad(String apiId, String name, double lat, double lng) {
        return crossroadRepository.save(new Crossroad(
            CrossroadApiResponse.builder().crossroadApiId(apiId).name(name).lat(lat).lng(lng)
                .build()));
    }

    private Feedback saveFeedback(String subject, String content, Member member, Crossroad crossroad) {
        return feedbackRepository.save(
            Feedback.create().subject(subject).content(content).secret(Boolean.FALSE)
                .category(FeedbackCategory.ETC).member(member).crossroad(crossroad).build());
    }

    private Feedback saveSecretFeedback(String subject, String content, Member member, Crossroad crossroad) {
        return feedbackRepository.save(
            Feedback.create().subject(subject).content(content).secret(Boolean.TRUE)
                .category(FeedbackCategory.ETC).member(member).crossroad(crossroad).build());
    }

    private void saveSoftDeleteFeedback(String subject, String content, Member member,
        Crossroad crossroad) {
        final Feedback feedback = Feedback.create().subject(subject).content(content)
            .secret(Boolean.FALSE).category(FeedbackCategory.ETC).member(member)
            .crossroad(crossroad).build();
        feedback.delete();
        feedbackRepository.save(feedback);
    }

    private CustomUser2Member getCurrentMember(Long id, MemberRole role) {
        return new CustomUser2Member(
            new CustomUserDetails(id, "", "",
                "", "", role, MemberStatus.ACTIVITY));
    }
}