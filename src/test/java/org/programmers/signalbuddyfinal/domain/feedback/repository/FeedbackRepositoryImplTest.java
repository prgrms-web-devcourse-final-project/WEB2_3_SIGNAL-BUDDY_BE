package org.programmers.signalbuddyfinal.domain.feedback.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.util.Set;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.programmers.signalbuddyfinal.domain.admin.dto.AdminSearchCondition;
import org.programmers.signalbuddyfinal.domain.crossroad.entity.Crossroad;
import org.programmers.signalbuddyfinal.domain.crossroad.repository.CrossroadRepository;
import org.programmers.signalbuddyfinal.domain.feedback.dto.FeedbackResponse;
import org.programmers.signalbuddyfinal.domain.feedback.dto.FeedbackSearchCondition;
import org.programmers.signalbuddyfinal.domain.feedback.entity.Feedback;
import org.programmers.signalbuddyfinal.domain.feedback.entity.enums.AnswerStatus;
import org.programmers.signalbuddyfinal.domain.feedback.entity.enums.FeedbackCategory;
import org.programmers.signalbuddyfinal.domain.feedback.exception.FeedbackErrorCode;
import org.programmers.signalbuddyfinal.domain.member.entity.Member;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberStatus;
import org.programmers.signalbuddyfinal.domain.member.fixture.TestMemberFactory;
import org.programmers.signalbuddyfinal.domain.member.repository.MemberRepository;
import org.programmers.signalbuddyfinal.global.constant.SearchTarget;
import org.programmers.signalbuddyfinal.global.exception.BusinessException;
import org.programmers.signalbuddyfinal.global.support.RepositoryTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

class FeedbackRepositoryImplTest extends RepositoryTest {

    @MockitoSpyBean
    private FeedbackRepository feedbackRepository;

    @Autowired
    private CrossroadRepository crossroadRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Member feedbackWriter;
    private Member withdrawalFeedbackWriter;

    @BeforeEach
    void setup() {
        feedbackWriter = memberRepository.save(
            TestMemberFactory.createActiveUser("test@test.com", "tester")
        );
        withdrawalFeedbackWriter = memberRepository.save(
            TestMemberFactory.createWithdrawalUser("with@test.com", "withdrawal")
        );
        Crossroad crossroad = saveCrossroad("13214", "00사거리", 37.12222, 127.12132);

        saveFeedback("test subject", "test content", feedbackWriter, crossroad);
        saveFeedback("test subject2", "test content2", withdrawalFeedbackWriter, crossroad);
        saveFeedback("test subject3", "test content3", feedbackWriter, crossroad);
        saveFeedback("test subject4", "test content4", feedbackWriter, crossroad);

        createFulltextIndexOnMember();
        createFulltextIndexOnFeedback();
    }

    @DisplayName("활동 중인 회원들의 피드백 목록을 조회한다.")
    @Test
    void getFeedbacks() {
        // Given
        FeedbackSearchCondition searchCondition = FeedbackSearchCondition.builder()
            .target(SearchTarget.SUBJECT_OR_CONTENT).answerStatus(AnswerStatus.BEFORE)
            .build();

        // When
        Page<FeedbackResponse> allByActiveMembers = feedbackRepository.findAllByActiveMembers(
            Pageable.ofSize(10), null, searchCondition);

        // Then
        assertThat(allByActiveMembers).isNotNull();
        assertThat(allByActiveMembers.getTotalElements()).isEqualTo(3);
        assertThat(allByActiveMembers.getTotalPages()).isEqualTo(1);

        assertThat(allByActiveMembers.getContent()).isNotEmpty().allSatisfy(
            feedback -> assertThat(feedback.getMember().getMemberStatus()).isEqualTo(
                MemberStatus.ACTIVITY));
    }

    @DisplayName("특정 교차로의 피드백 목록을 조회한다.")
    @Test
    void findAllByActiveMembersByCrossroadId() {
        // Given
        Crossroad crossroad = saveCrossroad("114111", "00삼거리", 37.42222, 127.42132);
        String subject = "test subject";
        String content = "test content";
        for (int i = 10; i < 13; i++) {
            saveFeedback(subject + i, content + i, feedbackWriter, crossroad);
        }
        saveFeedback(subject + 13, content + 13, withdrawalFeedbackWriter, crossroad);

        createFulltextIndexOnFeedback();

        FeedbackSearchCondition searchCondition = FeedbackSearchCondition.builder()
            .target(SearchTarget.SUBJECT_OR_CONTENT).answerStatus(AnswerStatus.BEFORE)
            .build();

        // When
        Page<FeedbackResponse> actual = feedbackRepository.findAllByActiveMembers(
            Pageable.ofSize(10), crossroad.getCrossroadId(), searchCondition
        );

        // Then
        SoftAssertions.assertSoftly(softAssertions -> {
            softAssertions.assertThat(actual).isNotNull();
            softAssertions.assertThat(actual.getTotalElements()).isEqualTo(3);
            softAssertions.assertThat(actual.getTotalPages()).isEqualTo(1);

            softAssertions.assertThat(actual.getContent()).isNotEmpty().allSatisfy(
                feedback -> assertThat(feedback.getMember().getMemberStatus())
                    .isEqualTo(MemberStatus.ACTIVITY)
            );
            softAssertions.assertThat(actual.getContent().get(1).getSubject()).isEqualTo(subject + 11);
        });
    }

    @DisplayName("특정 피드백 유형의 목록을 조회한다.")
    @Test
    void findAllByActiveMembersByFeedbackCategories() {
        // Given
        Set<FeedbackCategory> categories = Set.of(FeedbackCategory.DELAY, FeedbackCategory.ADD_SIGNAL);
        Crossroad crossroad = saveCrossroad("114111", "00삼거리", 37.42222, 127.42132);
        saveFeedback("test subject11", "test content11", FeedbackCategory.DELAY, feedbackWriter, crossroad);
        saveFeedback("test subject12", "test content12", FeedbackCategory.DELAY, feedbackWriter, crossroad);
        saveFeedback("test subject13", "test content13", FeedbackCategory.ADD_SIGNAL,
            feedbackWriter, crossroad);
        saveFeedback("test subject14", "test content14", feedbackWriter, crossroad);

        createFulltextIndexOnFeedback();

        FeedbackSearchCondition searchCondition = FeedbackSearchCondition.builder()
            .target(SearchTarget.SUBJECT_OR_CONTENT).categories(categories)
            .build();

        // When
        Page<FeedbackResponse> actual = feedbackRepository.findAllByActiveMembers(
            Pageable.ofSize(10), null, searchCondition
        );

        // Then
        SoftAssertions.assertSoftly(softAssertions -> {
            softAssertions.assertThat(actual).isNotNull();
            softAssertions.assertThat(actual.getTotalElements()).isEqualTo(3);
            softAssertions.assertThat(actual.getTotalPages()).isEqualTo(1);

            softAssertions.assertThat(actual.getContent()).isNotEmpty().allSatisfy(
                feedback -> assertThat(feedback.getMember().getMemberStatus())
                    .isEqualTo(MemberStatus.ACTIVITY)
            );
            for (FeedbackResponse feedback : actual.getContent()) {
                softAssertions.assertThat(feedback.getCategory())
                    .isNotEqualTo(FeedbackCategory.ETC);
            }
        });
    }

    @DisplayName("피드백의 제목과 내용을 대상으로 검색어를 조회한다.")
    @Test
    void findAllByActiveMembersByKeywordFromContent() {
        // Given
        String keyword = "홍길동";
        Set<FeedbackCategory> categories = Set.of(FeedbackCategory.DELAY, FeedbackCategory.ADD_SIGNAL);
        Crossroad crossroad = saveCrossroad("114111", "00삼거리", 37.42222, 127.42132);
        saveFeedback("ㅁㅁㅁ " + keyword, "test content11", FeedbackCategory.DELAY, feedbackWriter, crossroad);
        saveFeedback("test subject12", "test content12", FeedbackCategory.DELAY, feedbackWriter, crossroad);
        saveFeedback("test subject13", keyword + " ㅁㅁㅁ", FeedbackCategory.ADD_SIGNAL,
            feedbackWriter, crossroad);
        saveFeedback("test subject14", "test content14", feedbackWriter, crossroad);

        createFulltextIndexOnFeedback();

        FeedbackSearchCondition searchCondition = FeedbackSearchCondition.builder()
            .target(SearchTarget.SUBJECT_OR_CONTENT).keyword(keyword).categories(categories)
            .build();

        // When
        Page<FeedbackResponse> actual = feedbackRepository.findAllByActiveMembers(
            Pageable.ofSize(10), null, searchCondition
        );

        // Then
        SoftAssertions.assertSoftly(softAssertions -> {
            softAssertions.assertThat(actual).isNotNull();
            softAssertions.assertThat(actual.getTotalElements()).isEqualTo(2);
            softAssertions.assertThat(actual.getTotalPages()).isEqualTo(1);

            softAssertions.assertThat(actual.getContent()).isNotEmpty().allSatisfy(
                feedback -> assertThat(feedback.getMember().getMemberStatus())
                    .isEqualTo(MemberStatus.ACTIVITY)
            );
            for (FeedbackResponse feedback : actual.getContent()) {
                softAssertions.assertThat(feedback.getSubject() + feedback.getContent())
                    .contains(keyword);
            }
        });
    }

    @DisplayName("피드백의 작성자를 대상으로 검색어를 조회한다.")
    @Test
    void findAllByActiveMembersByKeywordFromWriter() {
        // Given
        String keyword = feedbackWriter.getNickname();

        FeedbackSearchCondition searchCondition = FeedbackSearchCondition.builder()
            .target(SearchTarget.WRITER).keyword(keyword)
            .build();

        // When
        Page<FeedbackResponse> actual = feedbackRepository.findAllByActiveMembers(
            Pageable.ofSize(10), null, searchCondition
        );

        // Then
        SoftAssertions.assertSoftly(softAssertions -> {
            softAssertions.assertThat(actual).isNotNull();
            softAssertions.assertThat(actual.getTotalElements()).isEqualTo(3);
            softAssertions.assertThat(actual.getTotalPages()).isEqualTo(1);

            softAssertions.assertThat(actual.getContent()).isNotEmpty()
                .allSatisfy(feedback -> {
                    assertThat(feedback.getMember().getMemberStatus())
                        .isEqualTo(MemberStatus.ACTIVITY);
                    assertThat(feedback.getMember().getNickname())
                        .contains(keyword);
                }
            );
        });
    }

    @DisplayName("날짜를 조건으로 설정하고 Soft Delete 처리된 피드백도 함께 가져온다. (쿼리만 확인)")
    @Test
    void findAllByDate() {
        // Given
        Sort sort = Sort.by(Sort.Order.desc("createdAt"));
        Pageable pageable = PageRequest.of(0, 10, sort);
        String keyword = "test";
        Set<FeedbackCategory> categories = Set.of(FeedbackCategory.DELAY, FeedbackCategory.ETC);
        LocalDate startDate = LocalDate.of(2024, 11, 12);
        LocalDate endDate = LocalDate.of(2025, 2, 12);

        AdminSearchCondition adminSearchCondition = AdminSearchCondition.builder()
            .startDate(startDate).endDate(endDate).deleted(Boolean.FALSE)
            .build();
        FeedbackSearchCondition searchCondition = FeedbackSearchCondition.builder()
            .target(SearchTarget.SUBJECT_OR_CONTENT).keyword(keyword).categories(categories)
            .answerStatus(AnswerStatus.BEFORE)
            .adminSearchCondition(adminSearchCondition)
            .build();

        // When
        feedbackRepository.findAllByFilter(
            pageable, searchCondition
        );

        // Then
        verify(feedbackRepository, times(1))
            .findAllByFilter(
                pageable, searchCondition
            );
    }

    @DisplayName("Soft Delete 된 피드백만 가져온다.")
    @Test
    void findAllByDeleted() {
        // Given
        Sort sort = Sort.by(Sort.Order.desc("createdAt"));
        Pageable pageable = PageRequest.of(0, 10, sort);
        feedbackRepository.deleteById(1L);
        feedbackRepository.deleteById(2L);

        AdminSearchCondition adminSearchCondition = AdminSearchCondition.builder()
            .deleted(Boolean.TRUE)
            .build();
        FeedbackSearchCondition searchCondition = FeedbackSearchCondition.builder()
            .target(SearchTarget.SUBJECT_OR_CONTENT).adminSearchCondition(adminSearchCondition)
            .build();

        // When
        Page<FeedbackResponse> actual = feedbackRepository.findAllByFilter(
            pageable, searchCondition
        );

        // Then
        assertThat(actual.getTotalElements()).isEqualTo(2);
    }

    @DisplayName("Soft Delete가 되지 않은 피드백 데이터를 가져온다.")
    @Test
    void findByIdOrThrow_Success() {
        // When & Then
        assertThat(feedbackRepository.findByIdOrThrow(1L).getFeedbackId()).isOne();
    }

    @DisplayName("Soft Delete가 된 피드백 데이터를 가져오게 되면 실패한다.")
    @Test
    void findByIdOrThrow_Failure() {
        // When & Then
        try {
            feedbackRepository.deleteById(1L);
            feedbackRepository.findByIdOrThrow(1L);
        } catch (BusinessException e) {
            assertThat(e.getErrorCode()).isEqualTo(FeedbackErrorCode.NOT_FOUND_FEEDBACK);
        }
    }

    private Crossroad saveCrossroad(String apiId, String name, double lat, double lng) {
        return crossroadRepository.save(
            Crossroad.create()
                .crossroadApiId(apiId).name(name)
                .lat(lat).lng(lng)
                .build()
        );
    }

    private void saveFeedback(String subject, String content, Member member, Crossroad crossroad) {
        feedbackRepository.save(
            Feedback.create().subject(subject).content(content).secret(Boolean.FALSE)
                .category(FeedbackCategory.ETC).member(member).crossroad(crossroad).build());
    }

    private void saveFeedback(
        String subject, String content, FeedbackCategory category,
        Member member, Crossroad crossroad
    ) {
        feedbackRepository.save(
            Feedback.create().subject(subject).content(content).secret(Boolean.FALSE)
                .category(category).member(member).crossroad(crossroad).build());
    }

    private void createFulltextIndexOnFeedback() {
        jdbcTemplate.execute(
            "CREATE FULLTEXT INDEX IF NOT EXISTS idx_subject_content " +
                "ON feedbacks (subject, content)"
        );
    }

    private void createFulltextIndexOnMember() {
        jdbcTemplate.execute(
            "CREATE FULLTEXT INDEX IF NOT EXISTS idx_nickname " +
                "ON members (nickname)"
        );
    }
}