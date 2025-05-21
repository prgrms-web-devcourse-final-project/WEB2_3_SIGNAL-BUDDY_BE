package org.programmers.signalbuddyfinal.domain.bookmark.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.programmers.signalbuddyfinal.domain.bookmark.dto.BookmarkRequest;
import org.programmers.signalbuddyfinal.domain.bookmark.dto.BookmarkResponse;
import org.programmers.signalbuddyfinal.domain.bookmark.dto.BookmarkSequenceUpdateRequest;
import org.programmers.signalbuddyfinal.domain.bookmark.entity.Bookmark;
import org.programmers.signalbuddyfinal.domain.bookmark.exception.BookmarkErrorCode;
import org.programmers.signalbuddyfinal.domain.bookmark.repository.BookmarkRepository;
import org.programmers.signalbuddyfinal.domain.member.entity.Member;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberRole;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberStatus;
import org.programmers.signalbuddyfinal.domain.member.repository.MemberRepository;
import org.programmers.signalbuddyfinal.domain.recentpath.dto.RecentPathRequest;
import org.programmers.signalbuddyfinal.domain.recentpath.dto.RecentPathResponse;
import org.programmers.signalbuddyfinal.domain.recentpath.service.RecentPathService;
import org.programmers.signalbuddyfinal.global.dto.CustomUser2Member;
import org.programmers.signalbuddyfinal.global.exception.BusinessException;
import org.programmers.signalbuddyfinal.global.security.basic.CustomUserDetails;
import org.programmers.signalbuddyfinal.global.support.IntegrationTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class BookmarkServiceTest extends IntegrationTest {

    @Autowired
    private GeometryFactory geometryFactory;

    @Autowired
    private BookmarkRepository bookmarkRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private BookmarkService bookmarkService;

    @Autowired
    private RecentPathService recentPathService;

    private Member member;

    @BeforeEach
    void setup() {
        member = Member.builder().email("bookmark@bookmark.com").password("123456")
            .role(MemberRole.USER).nickname("bookmarkTest").memberStatus(MemberStatus.ACTIVITY)
            .profileImageUrl("https://book-test-image.com/test-123131").build();
        member = memberRepository.save(member);

        for (int i = 1; i <= 10; i++) {
            final BookmarkRequest request = BookmarkRequest.builder().lat(37.12345 + (i * 0.001))
                .lng(127.12345).address("Address " + i).name("Bookmark " + i).build();

            bookmarkService.createBookmark(request, member.getMemberId());
        }
        final RecentPathRequest recentPathRequest = RecentPathRequest.builder()
            .lat(37.12345 + 0.001).lng(127.12345).address("Address").name("name").build();
        recentPathService.saveRecentPath(member.getMemberId(), recentPathRequest);
    }


    @Test
    @DisplayName("즐겨찾기 등록 테스트")
    void createBookmark() {
        CustomUser2Member user = new CustomUser2Member(
            new CustomUserDetails(member.getMemberId(), "", "", "", "", MemberRole.USER,
                MemberStatus.ACTIVITY));

        final BookmarkRequest request = BookmarkRequest.builder().lat(37.12345).lng(127.12345)
            .address("test").build();
        final BookmarkResponse response = bookmarkService.createBookmark(request,
            user.getMemberId());
        final Optional<Bookmark> found = bookmarkRepository.findById(response.getBookmarkId());

        assertThat(response).isNotNull();
        assertThat(found).isPresent();
        assertThat(found.get().getAddress()).isEqualTo(response.getAddress());
        assertThat(found.get().getCoordinate().getX()).isEqualTo(response.getLng());
        assertThat(found.get().getCoordinate().getY()).isEqualTo(response.getLat());
    }

    @Test
    @DisplayName("즐겨찾기 중복 등록 테스트")
    void createBookmarkFailure() {
        CustomUser2Member user = new CustomUser2Member(
            new CustomUserDetails(member.getMemberId(), "", "", "", "", MemberRole.USER,
                MemberStatus.ACTIVITY));

        final BookmarkRequest request = BookmarkRequest.builder().lat(37.12345 + 0.001)
            .lng(127.12345).address("test").build();
        final Long memberId = user.getMemberId();

        assertThatThrownBy(() -> bookmarkService.createBookmark(request, memberId)).isInstanceOf(
                BusinessException.class)
            .hasMessageContaining(BookmarkErrorCode.ALREADY_EXIST_BOOKMARK.getMessage());
    }

    @Test
    @DisplayName("즐겨찾기 수정 테스트")
    void updateBookmark() {
        CustomUser2Member user = new CustomUser2Member(
            new CustomUserDetails(member.getMemberId(), "", "", "", "", MemberRole.USER,
                MemberStatus.ACTIVITY));

        final BookmarkRequest request = BookmarkRequest.builder().lat(37.12345 + 0.001)
            .lng(127.12345).address("Update Address").name("Update Name").build();

        final Optional<Bookmark> bookmark = bookmarkRepository.findById(1L);
        assertThat(bookmark).isPresent();

        final BookmarkResponse response = bookmarkService.updateBookmark(request,
            bookmark.get().getBookmarkId(), user.getMemberId());
        final Optional<Bookmark> found = bookmarkRepository.findById(1L);

        final List<RecentPathResponse> recentPathList = recentPathService.getRecentPathList(
            member.getMemberId());

        assertThat(recentPathList).hasSize(1).allSatisfy(recentPath -> {
            assertThat(recentPath.getAddress()).isEqualTo(response.getAddress());
            assertThat(recentPath.getName()).isEqualTo(response.getName());
        });

        assertThat(response).isNotNull();
        assertThat(found).isPresent();
        assertThat(found.get().getBookmarkId()).isEqualTo(1L);
        assertThat(found.get().getAddress()).isEqualTo(response.getAddress());
        assertThat(found.get().getCoordinate().getX()).isEqualTo(response.getLng());
        assertThat(found.get().getCoordinate().getY()).isEqualTo(response.getLat());
        assertThat(found.get().getSequence()).isEqualTo(response.getSequence());
        assertThat(found.get().getMember().getMemberId()).isEqualTo(member.getMemberId());
    }

    @Test
    @DisplayName("즐겨찾기 수정 실패 : 사용자 인증 정보 다름")
    void updateBookmarkFailure() {
        final Member failure = Member.builder().email("bookmark2@bookmark.com").password("123456")
            .role(MemberRole.USER).nickname("updateBookmarkFailure")
            .memberStatus(MemberStatus.ACTIVITY)
            .profileImageUrl("https://book-test-image.com/test-123131").build();
        memberRepository.save(failure);

        CustomUser2Member user = new CustomUser2Member(
            new CustomUserDetails(failure.getMemberId(), "", "", "", "", MemberRole.USER,
                MemberStatus.ACTIVITY));

        final BookmarkRequest request = BookmarkRequest.builder().lat(37.12345 + 0.001)
            .lng(127.12345).address("Update Address").name("Update Name").build();

        final Optional<Bookmark> bookmark = bookmarkRepository.findById(1L);
        assertThat(bookmark).isPresent();

        final Long bookmarkId = bookmark.get().getBookmarkId();
        final Long memberId = user.getMemberId();

        assertThatThrownBy(
            () -> bookmarkService.updateBookmark(request, bookmarkId, memberId)).isInstanceOf(
                BusinessException.class)
            .hasMessageContaining(BookmarkErrorCode.UNAUTHORIZED_MEMBER_ACCESS.getMessage());
    }

    @Test
    @DisplayName("즐겨찾기 삭제 테스트")
    void deleteBookmark() {
        CustomUser2Member user = new CustomUser2Member(
            new CustomUserDetails(member.getMemberId(), "", "", "", "", MemberRole.USER,
                MemberStatus.ACTIVITY));

        Point point = geometryFactory.createPoint(new Coordinate(126.553311, 36.66633));
        Bookmark.builder().coordinate(point).address("Some Place").member(member).build();

        final List<Long> ids = List.of(1L, 2L, 3L);
        bookmarkService.deleteBookmark(ids, user.getMemberId());
        final List<Bookmark> bookmarkList = bookmarkRepository.findAllById(ids);

        assertThat(bookmarkList).isNotEmpty().allSatisfy(e -> {
            assertThat(e.getDeletedAt()).isNotNull();
        });
    }

    @Test
    @DisplayName("나의 목적지 순서 변경")
    void updateBookmarkSequences() {
        final List<Long> ids = List.of(1L, 2L, 3L);
        final List<BookmarkSequenceUpdateRequest> requests = List.of(
            new BookmarkSequenceUpdateRequest(1L, 3), new BookmarkSequenceUpdateRequest(2L, 5),
            new BookmarkSequenceUpdateRequest(3L, 9));

        final List<Bookmark> bookmarkList = bookmarkRepository.findAllById(ids);
        final Map<Long, Integer> map = bookmarkList.stream()
            .collect(Collectors.toMap(Bookmark::getBookmarkId, Bookmark::getSequence));

        final List<BookmarkResponse> responses = bookmarkService.updateBookmarkSequences(
            member.getMemberId(), requests);

        final Map<Long, Integer> expectedMap = requests.stream()
            .collect(Collectors.toMap(BookmarkSequenceUpdateRequest::id,
                BookmarkSequenceUpdateRequest::targetSequence));

        final Map<Long, Integer> actualMap = responses.stream()
            .collect(Collectors.toMap(BookmarkResponse::getBookmarkId, BookmarkResponse::getSequence));

        assertThat(actualMap).isEqualTo(expectedMap);
        assertThat(responses).isNotEmpty().allSatisfy(e -> {
            assertThat(map).doesNotContainEntry(e.getBookmarkId(), e.getSequence());
            // 기대한 값과 완전히 일치하는지 검증
            assertThat(expectedMap).containsEntry(e.getBookmarkId(), e.getSequence());
        });
    }
}