package org.programmers.signalbuddyfinal.domain.notification.repository;

import java.util.List;
import org.programmers.signalbuddyfinal.domain.notification.entity.FcmToken;
import org.programmers.signalbuddyfinal.domain.notification.exception.FcmErrorCode;
import org.programmers.signalbuddyfinal.global.exception.BusinessException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FcmTokenRepository extends JpaRepository<FcmToken, Long> {

    @Query("SELECT ft FROM fcm_tokens ft WHERE ft.member.memberId = :memberId")
    List<FcmToken> findAllByMemberId(@Param("memberId") Long memberId);

    default FcmToken findByIdOrThrow(Long id) {
        return findById(id)
            .orElseThrow(() -> new BusinessException(FcmErrorCode.FCM_TOKEN_NOT_FOUND));
    }
}
