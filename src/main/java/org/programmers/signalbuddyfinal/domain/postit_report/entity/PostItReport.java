package org.programmers.signalbuddyfinal.domain.postit_report.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.programmers.signalbuddyfinal.domain.member.entity.Member;
import org.programmers.signalbuddyfinal.domain.postit.entity.Postit;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity(name ="postit_report")
public class PostItReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long postItReportId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "postIt_id")
    private Postit postit;

}
