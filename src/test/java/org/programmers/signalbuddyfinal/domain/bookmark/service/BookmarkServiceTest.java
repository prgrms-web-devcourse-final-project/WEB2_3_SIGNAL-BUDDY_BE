package org.programmers.signalbuddyfinal.domain.bookmark.service;

import static org.assertj.core.api.Assertions.assertThat;

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
import org.programmers.signalbuddyfinal.domain.bookmark.repository.BookmarkRepository;
import org.programmers.signalbuddyfinal.domain.member.entity.Member;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberRole;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberStatus;
import org.programmers.signalbuddyfinal.domain.member.repository.MemberRepository;
import org.programmers.signalbuddyfinal.global.dto.CustomUser2Member;
import org.programmers.signalbuddyfinal.global.security.basic.CustomUserDetails;
import org.programmers.signalbuddyfinal.global.support.ServiceTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class BookmarkServiceTest extends ServiceTest {

    @Autowired
    private GeometryFactory geometryFactory;

    @Autowired
    private BookmarkRepository bookmarkRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private BookmarkService bookmarkService;

    private Member member;

    @BeforeEach
    void setup() {
        member = Member.builder().email("bookmark@bookmark.com").password("123456")
            .role(MemberRole.USER).nickname("bookmarkTest").memberStatus(MemberStatus.ACTIVITY)
            .profileImageUrl("https://book-test-image.com/test-123131").build();
        member = memberRepository.save(member);

        for (int i = 1; i <= 10; i++) {
            final BookmarkRequest request = BookmarkRequest.builder().lat(37.12345).lng(127.12345)
                .address("Address " + i).build();
            bookmarkService.createBookmark(request, member.getMemberId());
        }
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
    @DisplayName("즐겨찾기 수정 테스트")
    void updateBookmark() {
        CustomUser2Member user = new CustomUser2Member(
            new CustomUserDetails(member.getMemberId(), "", "", "", "", MemberRole.USER,
                MemberStatus.ACTIVITY));

        final BookmarkRequest request = BookmarkRequest.builder().lat(37.12345).lng(127.12345)
            .address("test").build();

        final Optional<Bookmark> bookmark = bookmarkRepository.findById(1L);
        assertThat(bookmark).isPresent();

        final BookmarkResponse response = bookmarkService.updateBookmark(request,
            bookmark.get().getBookmarkId(), user.getMemberId());
        final Optional<Bookmark> found = bookmarkRepository.findById(1L);

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

        assertThat(responses).isNotEmpty().allSatisfy(e -> {
            assertThat(map).doesNotContainEntry(e.getBookmarkId(), e.getSequence());
        });
    }
}