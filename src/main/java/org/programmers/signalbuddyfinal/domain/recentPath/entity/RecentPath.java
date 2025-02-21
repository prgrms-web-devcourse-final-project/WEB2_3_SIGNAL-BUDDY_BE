package org.programmers.signalbuddyfinal.domain.recentPath.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;
import org.programmers.signalbuddyfinal.domain.basetime.BaseTimeEntity;
import org.programmers.signalbuddyfinal.domain.bookmark.entity.Bookmark;
import org.programmers.signalbuddyfinal.domain.member.entity.Member;


@Entity(name = "recent_paths")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecentPath extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long recentPathId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Point endPoint;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id",nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bookmark_id",nullable = false)
    private Bookmark bookmark;

}
