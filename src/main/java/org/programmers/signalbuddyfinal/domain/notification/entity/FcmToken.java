package org.programmers.signalbuddyfinal.domain.notification.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;
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
    private String fcmTokenUuid;

    @Column(nullable = false)
    private String deviceToken;

    @Column
    private LocalDateTime logoutTime;

    @Column(nullable = false)
    private LocalDate lastUsedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Builder(builderMethodName = "create")
    private FcmToken(final String deviceToken, final Member member) {
        this.fcmTokenUuid = UUID.randomUUID().toString();
        this.deviceToken = Objects.requireNonNull(deviceToken);
        this.member = Objects.requireNonNull(member);
        this.lastUsedAt = LocalDate.now();
    }

    public void login() {
        this.logoutTime = null;
    }

    public void logout() {
        this.logoutTime = LocalDateTime.now();
    }

    public void updateLastUsedAt() {
        this.lastUsedAt = LocalDate.now();
    }
}
