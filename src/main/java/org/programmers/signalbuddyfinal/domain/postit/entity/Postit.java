package org.programmers.signalbuddyfinal.domain.postit.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;
import org.programmers.signalbuddyfinal.domain.basetime.BaseTimeEntity;
import org.programmers.signalbuddyfinal.domain.member.entity.Member;

import java.time.LocalDateTime;

@Entity(name = "postits")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Postit extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long postitId;

    @Column(nullable = false)
    private String danger;

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

    @Column(nullable = false)
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


}
