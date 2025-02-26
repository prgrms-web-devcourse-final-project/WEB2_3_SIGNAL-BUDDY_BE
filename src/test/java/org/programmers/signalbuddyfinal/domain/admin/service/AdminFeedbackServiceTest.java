package org.programmers.signalbuddyfinal.domain.admin.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberRole;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberStatus;
import org.programmers.signalbuddyfinal.global.dto.CustomUser2Member;
import org.programmers.signalbuddyfinal.global.exception.BusinessException;
import org.programmers.signalbuddyfinal.global.exception.GlobalErrorCode;
import org.programmers.signalbuddyfinal.global.security.basic.CustomUserDetails;
import org.programmers.signalbuddyfinal.global.support.ServiceTest;
import org.springframework.beans.factory.annotation.Autowired;

class AdminFeedbackServiceTest extends ServiceTest {

    @Autowired
    private AdminFeedbackService adminFeedbackService;

    @DisplayName("관리자용 피드백 조회 메서드를 일반 사용자가 이용하면 실패한다.")
    @Test
    void searchFeedbackList_Failure() {
        // Given
        CustomUser2Member user = getCurrentMember(1L, MemberRole.USER);

        // When & Then
        try {
            adminFeedbackService.searchFeedbackList(
                null, null, null, null, null, user
            );
        } catch (BusinessException e) {
            assertThat(e.getErrorCode()).isEqualTo(GlobalErrorCode.ADMIN_ONLY);
        }
    }

    private CustomUser2Member getCurrentMember(Long id, MemberRole role) {
        return new CustomUser2Member(
            new CustomUserDetails(id, "", "",
                "", "", role, MemberStatus.ACTIVITY));
    }
}