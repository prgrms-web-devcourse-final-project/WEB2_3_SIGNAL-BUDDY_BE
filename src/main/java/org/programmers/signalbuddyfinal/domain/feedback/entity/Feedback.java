package org.programmers.signalbuddyfinal.domain.feedback.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.programmers.signalbuddyfinal.domain.basetime.BaseTimeEntity;
import org.programmers.signalbuddyfinal.domain.feedback.dto.FeedbackWriteRequest;
import org.programmers.signalbuddyfinal.domain.feedback.entity.enums.AnswerStatus;
import org.programmers.signalbuddyfinal.domain.member.entity.Member;

import java.time.LocalDateTime;

@Entity(name = "feedbacks")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Feedback extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long feedbackId;

    @Column(nullable = false)
    private String subject;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private Long likeCount;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AnswerStatus answerStatus;

    @Column
    private LocalDateTime deletedAt; // 삭제일

    @Column(nullable = false)
    private String secret;

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    private Feedback(FeedbackWriteRequest request, Member member) {
        this.subject = request.getSubject();
        this.content = request.getContent();
        this.category = "기타";
        this.likeCount = 0L;
        this.answerStatus = AnswerStatus.BEFORE;
        this.member = member;
        this.secret = "PUBLIC";
    }

    public static Feedback create(FeedbackWriteRequest request, Member member) {
        return new Feedback(request, member);
    }

    public void delete() {
        this.deletedAt = LocalDateTime.now();
    } // 삭제 시간

    public boolean isDeleted() {
        return deletedAt != null;
    } // 삭제 확인

    public void updateFeedback(FeedbackWriteRequest request) {
        if (!this.subject.equals(request.getSubject())) {
            this.subject = request.getSubject();
        }

        if (!this.content.equals(request.getContent())) {
            this.content = request.getContent();
        }
    }

    public void updateFeedbackStatus() {
        if (AnswerStatus.BEFORE.equals(this.getAnswerStatus())) {
            this.answerStatus = AnswerStatus.COMPLETION;
        } else if (AnswerStatus.COMPLETION.equals(this.getAnswerStatus())) {
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