package org.programmers.signalbuddyfinal.domain.feedback.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.programmers.signalbuddyfinal.global.support.RestDocsFormatGenerators.getMockImageFile;

import java.net.URL;
import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.programmers.signalbuddyfinal.domain.crossroad.entity.Crossroad;
import org.programmers.signalbuddyfinal.domain.crossroad.repository.CrossroadRepository;
import org.programmers.signalbuddyfinal.domain.feedback.dto.FeedbackRequest;
import org.programmers.signalbuddyfinal.domain.feedback.dto.FeedbackResponse;
import org.programmers.signalbuddyfinal.domain.feedback.entity.Feedback;
import org.programmers.signalbuddyfinal.domain.feedback.entity.enums.FeedbackCategory;
import org.programmers.signalbuddyfinal.domain.feedback.exception.FeedbackErrorCode;
import org.programmers.signalbuddyfinal.domain.feedback.repository.FeedbackRepository;
import org.programmers.signalbuddyfinal.domain.like.entity.Like;
import org.programmers.signalbuddyfinal.domain.like.repository.LikeRepository;
import org.programmers.signalbuddyfinal.domain.member.entity.Member;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberRole;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberStatus;
import org.programmers.signalbuddyfinal.domain.member.fixture.TestMemberFactory;
import org.programmers.signalbuddyfinal.domain.member.repository.MemberRepository;
import org.programmers.signalbuddyfinal.global.dto.CustomUser2Member;
import org.programmers.signalbuddyfinal.global.dto.PageResponse;
import org.programmers.signalbuddyfinal.global.exception.BusinessException;
import org.programmers.signalbuddyfinal.global.security.basic.CustomUserDetails;
import org.programmers.signalbuddyfinal.global.service.AwsFileService;
import org.programmers.signalbuddyfinal.global.support.IntegrationTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

class FeedbackServiceTest extends IntegrationTest {

    @Autowired
    private FeedbackService feedbackService;

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private CrossroadRepository crossroadRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private LikeRepository likeRepository;

    @MockitoBean
    private AwsFileService awsFileService;

    private Member feedbackWriter;
    private Crossroad crossroad;
    private final String imageFormName = "imageFile";

    @BeforeEach
    void setup() {
        feedbackWriter = memberRepository.save(
            TestMemberFactory.createActiveUser("test@test.com", "tester")
        );
        crossroad = saveCrossroad("13214", "00사거리", 37.12222, 127.12132);

        saveFeedback("test subject", "test content", feedbackWriter, crossroad);
        saveFeedback("test subject2", "test content2", feedbackWriter, crossroad);
        saveFeedback("test subject3", "test content3", feedbackWriter, crossroad);
        saveSoftDeleteFeedback("test subject4", "test content4", feedbackWriter, crossroad);
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
        CustomUser2Member user = getCurrentMember(feedbackWriter.getMemberId(), MemberRole.USER);

        // When
        URL mockURL = mock(URL.class);
        when(awsFileService.uploadFileToS3(any(MockMultipartFile.class), anyString()))
            .thenReturn(imageFile.getName());
        when(awsFileService.getFileFromS3(anyString(), anyString())).thenReturn(mockURL);
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
            feedbackWriter.getMemberId(), Pageable.ofSize(10));

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
                .isEqualTo(feedbackWriter.getMemberId());
            assertThat(response.getCrossroad().getCrossroadId())
                .isEqualTo(crossroad.getCrossroadId());
        });
    }

    @DisplayName("작성자 본인이 비밀글을 상세 조회를 한다.")
    @Test
    void searchSecretFeedbackDetail_Success() {
        // Given
        Feedback secret = saveSecretFeedback(
            "test subject5", "test content5", feedbackWriter, crossroad
        );
        CustomUser2Member user = getCurrentMember(feedbackWriter.getMemberId(), MemberRole.USER);

        // When
        FeedbackResponse response = feedbackService.searchFeedbackDetail(
            secret.getFeedbackId(), user
        );

        // Then
        SoftAssertions.assertSoftly(softAssertions -> {
            assertThat(response.getFeedbackId()).isEqualTo(secret.getFeedbackId());
            assertThat(response.getSecret()).isTrue();
            assertThat(response.getMember().getMemberId())
                .isEqualTo(feedbackWriter.getMemberId());
            assertThat(response.getCrossroad().getCrossroadId())
                .isEqualTo(crossroad.getCrossroadId());
        });
    }

    @DisplayName("작성자가 아닌 일반 사용자가 비밀글을 상세 조회하면 실패한다.")
    @Test
    void searchSecretFeedbackDetail_Failure() {
        // Given
        Feedback secret = saveSecretFeedback(
            "test subject5", "test content5", feedbackWriter, crossroad
        );
        Member otherMember = memberRepository.save(
            TestMemberFactory.createActiveUser("test2@test.com", "tester2")
        );
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
            "test subject5", "test content5", feedbackWriter, crossroad
        );
        Member admin = memberRepository.save(
            TestMemberFactory.createAdmin("test@test.com", "tester2")
        );
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
                .isEqualTo(feedbackWriter.getMemberId());
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
            "test subject", "test content", feedbackWriter, crossroad
        );
        Long feedbackId = feedback.getFeedbackId();
        FeedbackCategory updatedCategory = FeedbackCategory.DELAY;
        String updatedContent = "update test content";
        FeedbackRequest request = FeedbackRequest.builder()
            .subject(feedback.getSubject()).content(updatedContent).secret(Boolean.FALSE)
            .category(updatedCategory).crossroadId(crossroad.getCrossroadId())
            .build();
        MockMultipartFile updatedImageFile = getMockImageFile(imageFormName);
        CustomUser2Member user = getCurrentMember(feedbackWriter.getMemberId(), MemberRole.USER);

        // When
        URL mockURL = mock(URL.class);
        when(awsFileService.uploadFileToS3(any(MockMultipartFile.class), anyString()))
            .thenReturn(updatedImageFile.getName());
        when(awsFileService.getFileFromS3(anyString(), anyString())).thenReturn(mockURL);
        FeedbackResponse actual = feedbackService.updateFeedback(
            feedbackId, request, updatedImageFile, user
        );

        // Then
        SoftAssertions.assertSoftly(softAssertions -> {
            softAssertions.assertThat(actual.getFeedbackId()).isEqualTo(feedbackId);
            softAssertions.assertThat(actual.getContent()).isEqualTo(updatedContent);
            softAssertions.assertThat(actual.getCategory()).isEqualTo(updatedCategory);
            softAssertions.assertThat(actual.getImageUrl()).isEqualTo(mockURL.toString());
        });
    }

    @DisplayName("사진이 첨부된 피드백에서 사진도 삭제하여 수정한다.")
    @Test
    void updateFeedback_DeleteImage() {
        // Given
        Feedback feedback = saveFeedback(
            "test subject", "test content", feedbackWriter, crossroad
        );
        Long feedbackId = feedback.getFeedbackId();
        FeedbackCategory updatedCategory = FeedbackCategory.DELAY;
        String updatedContent = "update test content";
        FeedbackRequest request = FeedbackRequest.builder()
            .subject(feedback.getSubject()).content(updatedContent).secret(Boolean.FALSE)
            .category(updatedCategory).crossroadId(crossroad.getCrossroadId())
            .build();
        MockMultipartFile updatedImageFile = null;
        CustomUser2Member user = getCurrentMember(feedbackWriter.getMemberId(), MemberRole.USER);

        // When
        FeedbackResponse actual = feedbackService.updateFeedback(
            feedbackId, request, updatedImageFile, user
        );

        // Then
        assertThat(actual.getImageUrl()).isNull();
        verify(awsFileService, times(0))
            .uploadFileToS3(any(MockMultipartFile.class), anyString());
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
        Member otherMember = memberRepository.save(
            TestMemberFactory.createActiveUser("test@test.com", "tester2")
        );
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
        CustomUser2Member user = getCurrentMember(feedbackWriter.getMemberId(), MemberRole.USER);

        // When
        feedbackService.deleteFeedback(feedbackId, user);

        // Then
        assertThat(feedbackRepository.findById(feedbackId).get().isDeleted()).isTrue();
    }

    @DisplayName("작성자가 아닌 일반 사용자가 피드백을 삭제하면 실패한다.")
    @Test
    void deleteFeedback_Failure() {
        // Given
        Long feedbackId = 2L;
        Member otherMember = memberRepository.save(
            TestMemberFactory.createActiveUser("test@test.com", "tester2")
        );
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
        Member admin = memberRepository.save(
            TestMemberFactory.createAdmin("test@test.com", "tester2")
        );
        CustomUser2Member user = getCurrentMember(admin.getMemberId(), MemberRole.ADMIN);

        // When
        feedbackService.deleteFeedback(feedbackId, user);

        // Then
        assertThat(feedbackRepository.findById(feedbackId).get().isDeleted()).isTrue();
    }

    @DisplayName("사용자가 좋아요한 피드백 목록 조회")
    @Test
    void findPagedLikedFeedbacks() {
        final List<Feedback> feedbacks = feedbackRepository.findAll().stream()
            .filter(feedback -> !feedback.isDeleted())
            .toList();
        final Member likedUser = memberRepository.save(
            TestMemberFactory.createActiveUser("test2@test.com", "tester2")
        );

        final List<Like> likes = List.of(
            Like.create(likedUser, feedbacks.get(0)),
            Like.create(likedUser, feedbacks.get(1)));
        likeRepository.saveAll(likes);

        final PageResponse<FeedbackResponse> likedFeedbacks = feedbackService.findPagedLikedFeedbacks(
            likedUser.getMemberId(), Pageable.ofSize(10));

        assertThat(feedbacks).hasSize(3);
        assertThat(likedFeedbacks.getTotalElements()).isEqualTo(2);
        assertThat(likedFeedbacks.getSearchResults()).allSatisfy(feedback -> {
            assertThat(feedback.getMember().getMemberId()).isEqualTo(feedbackWriter.getMemberId());
        });
    }

    private Crossroad saveCrossroad(String apiId, String name, double lat, double lng) {
        return crossroadRepository.save(
            Crossroad.create()
                .crossroadApiId(apiId).name(name)
                .lat(lat).lng(lng)
                .build()
        );
    }

    private Feedback saveFeedback(String subject, String content, Member member, Crossroad crossroad) {
        return feedbackRepository.save(
            Feedback.create().subject(subject).content(content).secret(Boolean.FALSE)
                .imageUrl("image url").category(FeedbackCategory.ETC).member(member)
                .crossroad(crossroad).build());
    }

    private Feedback saveSecretFeedback(String subject, String content, Member member, Crossroad crossroad) {
        return feedbackRepository.save(
            Feedback.create().subject(subject).content(content).secret(Boolean.TRUE)
                .imageUrl("image url").category(FeedbackCategory.ETC).member(member)
                .crossroad(crossroad).build());
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