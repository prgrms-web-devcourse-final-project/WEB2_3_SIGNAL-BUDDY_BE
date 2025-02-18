package org.programmers.signalbuddyfinal.domain.postitSolve.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.programmers.signalbuddyfinal.domain.basetime.BaseTimeEntity;
import org.programmers.signalbuddyfinal.domain.member.entity.Member;
import org.programmers.signalbuddyfinal.domain.postit.entity.Postit;

import java.time.LocalDateTime;

@Entity(name = "postits_solves")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostitSolve extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long PostitSolvesId;

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
}
