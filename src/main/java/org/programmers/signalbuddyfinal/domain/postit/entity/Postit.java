package org.programmers.signalbuddyfinal.domain.postit.entity;

import jakarta.persistence.*;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;
import org.programmers.signalbuddyfinal.domain.basetime.BaseTimeEntity;
import org.programmers.signalbuddyfinal.global.util.PointUtil;
import org.programmers.signalbuddyfinal.domain.member.entity.Member;
import java.time.LocalDateTime;
import org.programmers.signalbuddyfinal.domain.postit.dto.PostItRequest;

@Entity(name = "postits")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Postit extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long postitId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Danger danger;

    @Column(nullable = false)
    private Point coordinate;

    @Column(nullable = false)
    private String subject;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private String imageUrl;

    @Column(nullable = false)
    private LocalDateTime expiryDate;

    private LocalDateTime deletedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    public void delete() {
        this.deletedAt = LocalDateTime.now();
    } // 삭제 시간

    public boolean isDeleted() {
        return deletedAt != null;
    } // 삭제 확인


    @Builder(builderMethodName = "creator")
    private Postit(Danger danger, Point coordinate, String subject, String content, String imageUrl,
        LocalDateTime expiryDate, Member member) {
        this.danger = Objects.requireNonNull(danger);
        this.coordinate = Objects.requireNonNull(coordinate);
        this.subject = Objects.requireNonNull(subject);
        this.content = Objects.requireNonNull(content);
        this.imageUrl = Objects.requireNonNull(imageUrl);
        this.expiryDate = Objects.requireNonNull(expiryDate);
        this.deletedAt = null;
        this.member = Objects.requireNonNull(member);
    }

    public void updatePostIt(PostItRequest postItRequest, String imageUrl) {
        Point newCoordinate = PointUtil.toPoint(postItRequest.getLat(), postItRequest.getLng());

        if (!this.danger.equals(postItRequest.getDanger())) {
            this.danger = postItRequest.getDanger();
        }
        if (!this.coordinate.equals(newCoordinate)) {
            this.coordinate = newCoordinate;
        }
        if (!this.subject.equals(postItRequest.getSubject())) {
            this.subject = postItRequest.getSubject();
        }
        if (!this.content.equals(postItRequest.getContent())) {
            this.content = postItRequest.getContent();
        }
        if (!this.imageUrl.equals(imageUrl)) {
            this.imageUrl = imageUrl;
        }
    }

    public void completePostIt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    public void returnCompletePostIt(LocalDateTime expiryDate) {
        this.deletedAt = null;
        if (expiryDate != null) {
            this.expiryDate = expiryDate;
        }

    }
}
