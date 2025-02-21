package org.programmers.signalbuddyfinal.domain.recentpath.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.programmers.signalbuddyfinal.domain.member.entity.Member;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberRole;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberStatus;
import org.programmers.signalbuddyfinal.domain.member.repository.MemberRepository;
import org.programmers.signalbuddyfinal.domain.recentpath.dto.RecentPathRequest;
import org.programmers.signalbuddyfinal.domain.recentpath.dto.RecentPathResponse;
import org.programmers.signalbuddyfinal.global.support.ServiceTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class RecentPathServiceTest extends ServiceTest {

    @Autowired
    private RecentPathService recentPathService;

    @Autowired
    private MemberRepository memberRepository;

    private Member member;

    @BeforeEach
    void setup() {
        member = Member.builder().email("bookmark@bookmark.com").password("123456")
            .role(MemberRole.USER).nickname("bookmarkTest").memberStatus(MemberStatus.ACTIVITY)
            .profileImageUrl("https://book-test-image.com/test-123131").build();
        member = memberRepository.save(member);
    }

    @DisplayName("최근 경로 저장")
    @Test
    void saveRecentPath() {
        final RecentPathRequest request = RecentPathRequest.builder()
            .lat(37.12345).lng(127.12345).name("오징어집").build();

        final RecentPathResponse response = recentPathService.saveRecentPath(member.getMemberId(),
            request);

        assertThat(response).isNotNull();
        assertThat(response.getLastAccessedAt()).isNotNull();
    }
}