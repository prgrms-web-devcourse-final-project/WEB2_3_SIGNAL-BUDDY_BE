package org.programmers.signalbuddyfinal.domain.bookmark.entity;

import jakarta.persistence.*;
import lombok.*;
import org.locationtech.jts.geom.Point;
import org.programmers.signalbuddyfinal.domain.basetime.BaseTimeEntity;
import org.programmers.signalbuddyfinal.domain.member.entity.Member;

import java.time.LocalDateTime;

@Entity(name = "bookmarks")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
public class Bookmark extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookmarkId;

    @Column(nullable = false)
    private Point coordinate;

    @Column(nullable = false)
    private String address;

    @Column
    private String name;

    @Column
    private int sequence;

    @Column
    private LocalDateTime deletedAt; // 삭제일

    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;


    @Builder
    public Bookmark(Point coordinate, String address, Member member, String name) {
        this.coordinate = coordinate;
        this.address = address;
        this.member = member;
        this.name = name;
    }

    public void update(Point coordinate, String address, String name) {
        if (coordinate != null) {
            this.coordinate = coordinate;
        }
        if (address != null) {
            this.address = address;
        }
        if (name != null) {
            this.name = name;
        }
    }
}