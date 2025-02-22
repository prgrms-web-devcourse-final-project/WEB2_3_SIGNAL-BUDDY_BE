package org.programmers.signalbuddyfinal.domain.feedback.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.programmers.signalbuddyfinal.domain.basetime.BaseTimeEntity;
import org.programmers.signalbuddyfinal.domain.crossroad.entity.Crossroad;
import org.programmers.signalbuddyfinal.domain.feedback.dto.FeedbackRequest;
import org.programmers.signalbuddyfinal.domain.feedback.entity.enums.AnswerStatus;
import org.programmers.signalbuddyfinal.domain.feedback.entity.enums.FeedbackCategory;
import org.programmers.signalbuddyfinal.domain.member.entity.Member;

@Entity(name = "feedbacks")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLRestriction("deleted_at IS NULL")
@SQLDelete(sql = "UPDATE feedbacks SET deleted_at = now() WHERE feedback_id = ?")
public class Feedback extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long feedbackId;

    @Column(nullable = false)
    private String subject;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private FeedbackCategory category;

    @Column(nullable = false)
    private String content;

    @Column
    private String imageUrl;

    @Column(nullable = false)
    private Long likeCount;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AnswerStatus answerStatus;

    @Column(nullable = false)
    private Boolean secret;

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne
    @JoinColumn(name = "crossroad_id", nullable = false)
    private Crossroad crossroad;

    @Column
    private LocalDateTime deletedAt;

    @Builder(builderMethodName = "create")
    private Feedback(
        final String subject, final String content, final String imageUrl,
        final FeedbackCategory category, final Boolean secret,
        final Member member, final Crossroad crossroad
    ) {
        this.subject = Objects.requireNonNull(subject);
        this.content = Objects.requireNonNull(content);
        this.imageUrl = imageUrl;
        this.likeCount = 0L;
        this.category = Objects.requireNonNull(category);
        this.answerStatus = AnswerStatus.BEFORE;
        this.secret = Objects.requireNonNull(secret);
        this.member = Objects.requireNonNull(member);
        this.crossroad = Objects.requireNonNull(crossroad);
    }

    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public void updateFeedback(FeedbackRequest request) {
        if (!this.subject.equals(request.getSubject())) {
            this.subject = request.getSubject();
        }

        if (!this.content.equals(request.getContent())) {
            this.content = request.getContent();
        }

        if (!this.category.equals(request.getCategory())) {
            this.category = request.getCategory();
        }

        if (!this.secret.equals(request.getSecret())) {
            this.secret = request.getSecret();
        }
    }

    public void updateCrossroad(Crossroad crossroad) {
        this.crossroad = crossroad;
    }

    public void updateImageUrl(String imageUrl) {
        if (!this.imageUrl.equals(imageUrl)) {
            this.imageUrl = imageUrl;
        }
    }

    public void updateFeedbackStatus() {
        if (AnswerStatus.BEFORE.equals(this.answerStatus)) {
            this.answerStatus = AnswerStatus.COMPLETION;
        } else if (AnswerStatus.COMPLETION.equals(this.answerStatus)) {
            this.answerStatus = AnswerStatus.BEFORE;
        }
    }

    public void increaseLike() {
        this.likeCount += 1;
    }

    public void decreaseLike() {
        if (this.likeCount > 0) {
            this.likeCount -= 1;
        }
    }
}