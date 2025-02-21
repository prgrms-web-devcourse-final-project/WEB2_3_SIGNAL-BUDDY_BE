package org.programmers.signalbuddyfinal.domain.recentpath.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Point;
import org.programmers.signalbuddyfinal.domain.member.entity.Member;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberRole;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberStatus;
import org.programmers.signalbuddyfinal.domain.member.repository.MemberRepository;
import org.programmers.signalbuddyfinal.domain.recentpath.dto.RecentPathRequest;
import org.programmers.signalbuddyfinal.domain.recentpath.dto.RecentPathResponse;
import org.programmers.signalbuddyfinal.domain.recentpath.entity.RecentPath;
import org.programmers.signalbuddyfinal.domain.recentpath.repository.RecentPathRepository;
import org.programmers.signalbuddyfinal.global.support.ServiceTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class RecentPathServiceTest extends ServiceTest {

    @Autowired
    private RecentPathService recentPathService;

    @Autowired
    private MemberRepository memberRepository;

    @MockitoBean
    private RecentPathRepository recentPathRepository;

    private Member member;

    @BeforeEach
    void setup() {
        member = Member.builder().email("bookmark@bookmark.com").password("123456")
            .role(MemberRole.USER).nickname("bookmarkTest").memberStatus(MemberStatus.ACTIVITY)
            .profileImageUrl("https://book-test-image.com/test-123131").build();
        member = memberRepository.save(member);

        for (int i = 1; i <= 10; i++) {
            RecentPathRequest request = RecentPathRequest.builder()
                .lat(37.12345).lng(127.12345).name("Name " + i).build();
            recentPathService.saveRecentPath(member.getMemberId(), request);
        }
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

    @DisplayName("최근 경로 목록 조회")
    @Test
    void getRecentPathList() {
        final List<RecentPathResponse> recentPathList = recentPathService.getRecentPathList(
            member.getMemberId());

        assertThat(recentPathList).isNotEmpty().allSatisfy(recentPathResponse -> {
            assertThat(recentPathResponse.getLastAccessedAt()).isNotNull();
        });
    }

    @DisplayName("최근 경로 방문 시간 갱신")
    @Test
    void updateRecentPathTime() {
        LocalDateTime now = LocalDateTime.now().minusSeconds(1);;
        final RecentPathResponse response = recentPathService.updateRecentPathTime(1L);
        assertThat(response).isNotNull();
        assertThat(response.getLastAccessedAt()).isNotNull();
        assertThat(response.getLastAccessedAt()).isAfter(now);
    }

    @DisplayName("최근 경로와 북마크 연관관계 해제")
    @Test
    void unlinkBookmark() {
        RecentPath realRecentPath = new RecentPath("테스트 경로", mock(Point.class), member);
        RecentPath recentPath = spy(realRecentPath);
        when(recentPathRepository.findById(1L)).thenReturn(Optional.of(recentPath));

        // When
        recentPathService.unlinkBookmark(1L);

        // Then
        verify(recentPath).unlinkBookmark();
        assertThat(recentPath.getBookmark()).isNull();
    }
}