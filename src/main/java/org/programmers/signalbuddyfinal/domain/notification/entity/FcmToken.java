package org.programmers.signalbuddyfinal.domain.notification.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.programmers.signalbuddyfinal.domain.basetime.BaseTimeEntity;
import org.programmers.signalbuddyfinal.domain.member.entity.Member;

@Getter
@Entity(name = "fcm_tokens")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FcmToken extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long fcmTokenId;

    @Column(nullable = false)
    private String deviceToken;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Builder(builderMethodName = "create")
    private FcmToken(String deviceToken, Member member) {
        this.deviceToken = deviceToken;
        this.member = member;
    }

    public void updateDeviceToken(String deviceToken) {
        if (!this.deviceToken.equals(deviceToken)) {
            this.deviceToken = deviceToken;
        }
    }
}
