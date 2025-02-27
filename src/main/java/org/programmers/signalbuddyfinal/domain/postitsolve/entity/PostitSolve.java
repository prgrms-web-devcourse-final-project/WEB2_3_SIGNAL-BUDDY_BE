package org.programmers.signalbuddyfinal.domain.postitsolve.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
import org.locationtech.jts.geom.Point;
import org.programmers.signalbuddyfinal.domain.basetime.BaseTimeEntity;
import org.programmers.signalbuddyfinal.domain.member.entity.Member;
import org.programmers.signalbuddyfinal.domain.postit.entity.Danger;
import org.programmers.signalbuddyfinal.domain.postit.entity.Postit;

@Entity(name = "postits_solves")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostitSolve extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long postItSolvesId;

    @Column(nullable = false)
    private String imageUrl;

    @Column(nullable = false)
    private String content;

    @Column
    private LocalDateTime deletedAt;

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "postit_id")
    private Postit postit;

    public void delete() {
        this.deletedAt = LocalDateTime.now();
    } // 삭제 시간

    public boolean isDeleted() {
        return deletedAt != null;
    } // 삭제 확인

    @Builder(builderMethodName = "creator")
    private PostitSolve(String content, String imageUrl, LocalDateTime deletedAt, Member member, Postit postit) {
        this.content = Objects.requireNonNull(content);
        this.imageUrl = Objects.requireNonNull(imageUrl);
        this.deletedAt = Objects.requireNonNull(deletedAt);
        this.member = Objects.requireNonNull(member);
        this.postit = Objects.requireNonNull(postit);
    }
}
