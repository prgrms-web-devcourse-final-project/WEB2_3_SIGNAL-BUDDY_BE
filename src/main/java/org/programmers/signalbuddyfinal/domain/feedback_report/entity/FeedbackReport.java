package org.programmers.signalbuddyfinal.domain.feedback_report.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
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
import org.programmers.signalbuddyfinal.domain.feedback.entity.Feedback;
import org.programmers.signalbuddyfinal.domain.feedback_report.entity.enums.FeedbackReportCategory;
import org.programmers.signalbuddyfinal.domain.feedback_report.entity.enums.FeedbackReportStatus;
import org.programmers.signalbuddyfinal.domain.member.entity.Member;

@Getter
@Entity(name = "feedback_reports")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLRestriction("deleted_at IS NULL")
@SQLDelete(sql = "UPDATE feedback_reports SET deleted_at = now() WHERE feedback_report_id = ?")
public class FeedbackReport extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long feedbackReportId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private FeedbackReportCategory category;

    @Column(nullable = false)
    private String content;

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feedback_id", nullable = false)
    private Feedback feedback;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private FeedbackReportStatus status;

    @Column
    private LocalDateTime processedAt;

    @Column
    private LocalDateTime deletedAt;

    @Builder(builderMethodName = "create")
    private FeedbackReport(
        final FeedbackReportCategory category, final String content,
        final Member member, final Feedback feedback
    ) {
        this.category = Objects.requireNonNull(category);
        this.content = Objects.requireNonNull(content);
        this.member = Objects.requireNonNull(member);
        this.feedback = Objects.requireNonNull(feedback);
        this.status = FeedbackReportStatus.PENDING;
    }

    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public void updateStatus(FeedbackReportStatus status) {
        if (!this.status.equals(status)) {
            this.status = status;

            if (FeedbackReportStatus.PROCESSED.equals(status)) {
                this.processedAt = LocalDateTime.now();
            } else {
                this.processedAt = null;
            }
        }
    }
}
