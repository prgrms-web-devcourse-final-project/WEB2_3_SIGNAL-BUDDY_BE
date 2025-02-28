package org.programmers.signalbuddyfinal.domain.admin.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Point;
import org.programmers.signalbuddyfinal.domain.admin.dto.MemberFilterRequest;
import org.programmers.signalbuddyfinal.domain.admin.dto.PostItFilterRequest;
import org.programmers.signalbuddyfinal.domain.admin.dto.enums.Deleted;
import org.programmers.signalbuddyfinal.domain.admin.dto.enums.Periods;
import org.programmers.signalbuddyfinal.global.util.PointUtil;
import org.programmers.signalbuddyfinal.domain.member.entity.Member;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberRole;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberStatus;
import org.programmers.signalbuddyfinal.domain.member.repository.MemberRepository;
import org.programmers.signalbuddyfinal.domain.postit.entity.Danger;
import org.programmers.signalbuddyfinal.domain.postit.entity.Postit;
import org.programmers.signalbuddyfinal.domain.postit.repository.PostItRepository;
import org.programmers.signalbuddyfinal.global.support.RepositoryTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public class AdminPostItRepositoryTest extends RepositoryTest {

    Pageable pageable;
    Member member;

    @Autowired
    PostItRepository postItRepository;
    @Autowired
    MemberRepository memberRepository;

    @BeforeEach
    void setUp() {
        member = memberRepository.save(Member.builder()
            .email("user1@gmail.com")
            .role(MemberRole.USER)
            .profileImageUrl("https://memberImage1.com/imageUrl")
            .password("password")
            .nickname("user1")
            .memberStatus(MemberStatus.ACTIVITY)
            .build());

        createPostIt(Danger.NOTICE, PointUtil.toPoint(1.0203, 1.3048), "제목1",
            "내용1",
            "https://image1.com/imageUrl",
            LocalDateTime.of(2025, 1, 2, 1, 30), null,
            member);
        createPostIt(Danger.WARNING, PointUtil.toPoint(1.0203, 1.3048), "검색 테스트",
            "내용1",
            "https://image1.com/imageUrl",
            LocalDateTime.of(2025, 2, 26, 1, 30), LocalDateTime.of(2025, 1, 10, 3, 30),
            member);
        createPostIt(Danger.NOTICE, PointUtil.toPoint(1.0203, 1.3048), "제목1",
            "검색 테스트",
            "https://image1.com/imageUrl",
            LocalDateTime.of(2025, 1, 2, 1, 30),null ,
            member);

        pageable = PageRequest.of(0, 10);
    }



    @DisplayName("한개의 조건이 설정된 포스트잇 필더링 조회")
    @Test
    public void 한개의_조건이_설정된_포스트잇_필터링_조회() {

        PostItFilterRequest dateFilter = createFilter(LocalDateTime.of(2025, 1, 1, 0, 0),
            LocalDateTime.of(2025, 4, 1, 0, 0), null, null, null, null);
        PostItFilterRequest dangerFilter = createFilter(null, null, null, Danger.NOTICE, null,
            null);
        PostItFilterRequest periodsFilter = createFilter(null, null, Periods.THREE_DAYS, null, null,
            null);
        PostItFilterRequest searchFilter = createFilter(null, null, null, null, "검색",
            null);
        PostItFilterRequest deletedFilter = createFilter(null, null, null, null, null,
            Deleted.DELETED);

        assertThat(postItRepository.findAllPostItWithFilter(pageable, dateFilter)
            .getTotalElements()).isEqualTo(3);
        assertThat(postItRepository.findAllPostItWithFilter(pageable, dangerFilter)
            .getTotalElements()).isEqualTo(2);
        assertThat(postItRepository.findAllPostItWithFilter(pageable, periodsFilter)
            .getTotalElements()).isEqualTo(1);
        assertThat(postItRepository.findAllPostItWithFilter(pageable, searchFilter)
            .getTotalElements()).isEqualTo(2);
        assertThat(postItRepository.findAllPostItWithFilter(pageable, deletedFilter)
            .getTotalElements()).isEqualTo(1);

    }

    @DisplayName("중복 조건 포스트잇 필더링 조회")
    @Test
    public void 중복_조건_포스트잇_필터링_조회() {

        PostItFilterRequest dateAndDangerFilter = createFilter(LocalDateTime.of(2025, 1, 1, 0, 0),
            LocalDateTime.of(2025, 4, 1, 0, 0), null, Danger.WARNING, null, null);
        PostItFilterRequest dangerAndSearchFilter = createFilter(null, null, null, Danger.NOTICE, "검색",
            null);

        assertThat(postItRepository.findAllPostItWithFilter(pageable, dateAndDangerFilter)
            .getTotalElements()).isEqualTo(1);
        assertThat(postItRepository.findAllPostItWithFilter(pageable, dangerAndSearchFilter)
            .getTotalElements()).isEqualTo(1);

    }

    @DisplayName("필터가 전부 null 인 경우 테스트")
    @Test
    public void 전체_null_조회() {

        PostItFilterRequest nullFilter = createFilter(null, null, null, null,
            null, null);

        assertThat(postItRepository.findAllPostItWithFilter(pageable, nullFilter)
            .getTotalElements()).isEqualTo(3);
    }

    private Postit createPostIt(Danger danger, Point coordinate, String subject,
        String content, String imageURl, LocalDateTime expiryDate, LocalDateTime deleteAt,
        Member member) {

        Postit postit = Postit.builder()
            .danger(danger)
            .coordinate(coordinate)
            .subject(subject)
            .content(content)
            .imageUrl(imageURl)
            .expiryDate(expiryDate)
            .deletedAt(deleteAt)
            .member(member)
            .build();
        return postItRepository.save(postit);
    }

    private PostItFilterRequest createFilter(LocalDateTime startDate, LocalDateTime endDate,
        Periods period,
        Danger danger, String search, Deleted deleted) {
        return PostItFilterRequest.builder()
            .startDate(startDate)
            .endDate(endDate)
            .periods(period)
            .danger(danger)
            .search(search)
            .deleted(deleted)
            .build();
    }
}
