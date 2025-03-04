package org.programmers.signalbuddyfinal.domain.social.entity;

import jakarta.persistence.*;
import lombok.*;
import org.programmers.signalbuddyfinal.domain.basetime.BaseTimeEntity;
import org.programmers.signalbuddyfinal.domain.member.entity.Member;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class SocialProvider extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long socialProviderId;

    @Column(nullable = false)
    private String socialId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Provider oauthProvider;

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;
}
