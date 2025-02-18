package org.programmers.signalbuddyfinal.domain.comment.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.programmers.signalbuddyfinal.domain.basetime.BaseTimeEntity;
import org.programmers.signalbuddyfinal.domain.comment.dto.CommentRequest;
import org.programmers.signalbuddyfinal.domain.feedback.entity.Feedback;
import org.programmers.signalbuddyfinal.domain.member.entity.Member;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity(name = "comments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long commentId;

    @Column(nullable = false)
    private String content;

    @Column
    private LocalDateTime deletedAt; // 삭제일

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feedback_id", nullable = false)
    private Feedback feedback;

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Builder(builderMethodName = "creator")
    private Comment(CommentRequest request, Feedback feedback, Member member) {
        this.content = Objects.requireNonNull(request.getContent());
        this.feedback = Objects.requireNonNull(feedback);
        this.member = Objects.requireNonNull(member);
    }

    public void delete() {
        this.deletedAt = LocalDateTime.now();
    } // 삭제 시간

    public boolean isDeleted() {
        return deletedAt != null;
    } // 삭제 확인

    public void updateContent(CommentRequest request) {
        this.content = request.getContent();
    }
}