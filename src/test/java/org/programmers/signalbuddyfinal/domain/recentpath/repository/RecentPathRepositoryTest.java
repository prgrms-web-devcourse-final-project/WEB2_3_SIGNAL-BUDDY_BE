package org.programmers.signalbuddyfinal.domain.recentpath.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.programmers.signalbuddyfinal.domain.member.entity.Member;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberRole;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberStatus;
import org.programmers.signalbuddyfinal.domain.member.repository.MemberRepository;
import org.programmers.signalbuddyfinal.domain.recentpath.entity.RecentPath;
import org.programmers.signalbuddyfinal.global.support.RepositoryTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Limit;

class RecentPathRepositoryTest extends RepositoryTest {

    private final GeometryFactory geometryFactory = new GeometryFactory();

    private Member member;

    @Autowired
    private RecentPathRepository recentPathRepository;

    @Autowired
    private MemberRepository memberRepository;

    @BeforeEach
    void setUp() {
        member = Member.builder().email("test@example.com").password("password123")
            .nickname("TestUser").profileImageUrl("http://example.com/profile.jpg")
            .role(MemberRole.USER).memberStatus(MemberStatus.ACTIVITY).build();
        member = memberRepository.save(member);

        Point point = geometryFactory.createPoint(new Coordinate(127.12345, 37.12345));
        for (int i = 1; i <= 15; i++) {
            final RecentPath recentPath = RecentPath.builder().member(member).name("Recent Path")
                .endPoint(point).build();
            recentPathRepository.save(recentPath);
        }
    }

    @DisplayName("최근 경로 목록 최대 10개 조회")
    @Test
    void getRecentPathsLimit10() {
        final List<RecentPath> list = recentPathRepository.findAllByMemberOrderByLastAccessedAtDesc(
            member, Limit.of(10));

        assertThat(list).isNotEmpty().hasSize(10).allSatisfy(recentPath -> {
            assertThat(recentPath.getMember()).isEqualTo(member);
        });
    }
}