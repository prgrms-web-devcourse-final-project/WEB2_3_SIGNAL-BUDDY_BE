package org.programmers.signalbuddyfinal.domain.postit.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Point;
import org.programmers.signalbuddyfinal.domain.basetime.BaseTimeEntity;
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
    private Postit(Danger danger, Point coordinate, String subject, String content, String imageUrl, LocalDateTime expiryDate, Member member) {
        this.danger = Objects.requireNonNull(danger);
        this.coordinate = Objects.requireNonNull(coordinate);
        this.subject = Objects.requireNonNull(subject);
        this.content = Objects.requireNonNull(content);
        this.imageUrl = Objects.requireNonNull(imageUrl);
        this.expiryDate = Objects.requireNonNull(expiryDate);
        this.deletedAt = null;
        this.member = Objects.requireNonNull(member);
    }

}
