package org.programmers.signalbuddyfinal.domain.bookmark.service;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Point;
import org.programmers.signalbuddyfinal.domain.bookmark.dto.BookmarkRequest;
import org.programmers.signalbuddyfinal.domain.bookmark.dto.BookmarkResponse;
import org.programmers.signalbuddyfinal.domain.bookmark.dto.BookmarkSequenceUpdateRequest;
import org.programmers.signalbuddyfinal.domain.bookmark.entity.Bookmark;
import org.programmers.signalbuddyfinal.domain.bookmark.exception.BookmarkErrorCode;
import org.programmers.signalbuddyfinal.domain.bookmark.mapper.BookmarkMapper;
import org.programmers.signalbuddyfinal.domain.bookmark.repository.BookmarkRepository;
import org.programmers.signalbuddyfinal.domain.member.entity.Member;
import org.programmers.signalbuddyfinal.domain.member.exception.MemberErrorCode;
import org.programmers.signalbuddyfinal.domain.member.repository.MemberRepository;
import org.programmers.signalbuddyfinal.domain.recentpath.entity.RecentPath;
import org.programmers.signalbuddyfinal.domain.recentpath.repository.RecentPathRepository;
import org.programmers.signalbuddyfinal.global.dto.PageResponse;
import org.programmers.signalbuddyfinal.global.exception.BusinessException;
import org.programmers.signalbuddyfinal.global.util.PointUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final MemberRepository memberRepository;
    private final RecentPathRepository recentPathRepository;


    public PageResponse<BookmarkResponse> findPagedBookmarks(Pageable pageable, Long memberId) {
        final Page<BookmarkResponse> page = bookmarkRepository.findPagedByMember(pageable,
            memberId);
        return new PageResponse<>(page);
    }

    @Transactional
    public BookmarkResponse createBookmark(BookmarkRequest request, Long memberId) {
        final Member member = getMember(memberId);

        final Point point = PointUtils.toPoint(request.getLat(), request.getLng());

        validateDuplicate(memberId, point);

        final Bookmark bookmark = createBookmarkEntity(request, member, point);

        linkRecentPathIfExists(memberId, point, bookmark);

        return BookmarkMapper.INSTANCE.toDto(bookmarkRepository.save(bookmark));
    }

    @Transactional
    public BookmarkResponse updateBookmark(BookmarkRequest request, Long bookmarkId,
        Long memberId) {
        final Member member = getMember(memberId);
        final Bookmark bookmark = getAuthorizedBookmark(bookmarkId, member);

        final Point updatedPoint = PointUtils.toPoint(request.getLat(), request.getLng());
        bookmark.update(updatedPoint, request.getAddress(), request.getName());

        updateLinkedRecentPath(updatedPoint, memberId, request);

        return BookmarkMapper.INSTANCE.toDto(bookmark);
    }


    @Transactional
    public void deleteBookmark(List<Long> bookmarkIds, Long memberId) {
        final List<Bookmark> bookmarks = findMemberBookmarks(bookmarkIds, memberId);
        bookmarks.forEach(Bookmark::delete);

        unlinkBookmarksFromRecentPaths(bookmarks);
        log.info("Bookmark deleted: {}", bookmarkIds);
    }

    @Transactional(readOnly = true)
    public BookmarkResponse getBookmark(Long memberId, Long bookmarkId) {
        final Member member = getMember(memberId);
        final Bookmark bookmark = getAuthorizedBookmark(bookmarkId, member);
        return BookmarkMapper.INSTANCE.toDto(bookmark);
    }

    @Transactional
    public List<BookmarkResponse> updateBookmarkSequences(Long id,
        @Valid List<BookmarkSequenceUpdateRequest> requests) {
        final List<Long> bookmarkIds = requests.stream().map(BookmarkSequenceUpdateRequest::id)
            .toList();

        final List<Integer> targetSequences = requests.stream()
            .map(BookmarkSequenceUpdateRequest::targetSequence).toList();

        final List<Bookmark> all = bookmarkRepository.findAllByMemberMemberIdAndBookmarkIdInOrSequenceIn(
            id, bookmarkIds, targetSequences);

        final Map<Long, Bookmark> bookmarkMap = all.stream()
            .filter(b -> bookmarkIds.contains(b.getBookmarkId()))
            .collect(Collectors.toMap(Bookmark::getBookmarkId, Function.identity()));

        final Map<Integer, Bookmark> sequenceMap = all.stream()
            .filter(b -> targetSequences.contains(b.getSequence()))
            .collect(Collectors.toMap(Bookmark::getSequence, Function.identity()));

        swapSequence(requests, bookmarkMap, sequenceMap);

        return all.stream().filter(b -> bookmarkIds.contains(b.getBookmarkId()))
            .map(BookmarkMapper.INSTANCE::toDto).toList();
    }


    private void linkRecentPathIfExists(Long memberId, Point point, Bookmark bookmark) {
        // 북마크 저장하는 좌표가 최근경로에 있다면 연관관계 생성
        recentPathRepository.findByEndPointAndMemberMemberId(point, memberId)
            .ifPresent(recentPath -> recentPath.linkBookmark(bookmark));
    }

    private Bookmark createBookmarkEntity(BookmarkRequest request, Member member, Point point) {
        final int nextSequence =
            bookmarkRepository.findTopByMemberOrderBySequenceDesc(member).map(Bookmark::getSequence)
                .orElse(0) + 1;

        final Bookmark bookmark = BookmarkMapper.INSTANCE.toEntity(request, point, member);
        bookmark.updateSequence(nextSequence);
        return bookmark;
    }

    private void validateDuplicate(Long memberId, Point point) {
        bookmarkRepository.findByCoordinateAndMemberIdNotDeleted(point, memberId)
            .ifPresent(bookmark -> {
                throw new BusinessException(BookmarkErrorCode.ALREADY_EXIST_BOOKMARK);
            });
    }

    private Bookmark getAuthorizedBookmark(Long bookmarkId, Member member) {
        Bookmark bookmark = bookmarkRepository.findById(bookmarkId)
            .orElseThrow(() -> new BusinessException(BookmarkErrorCode.NOT_FOUND_BOOKMARK));

        if (bookmark.isNotOwnedBy(member)) {
            throw new BusinessException(BookmarkErrorCode.UNAUTHORIZED_MEMBER_ACCESS);
        }
        return bookmark;
    }

    private void updateLinkedRecentPath(Point point, Long memberId, BookmarkRequest request) {
        recentPathRepository.findByEndPointAndMemberMemberId(point, memberId)
            .ifPresent(path -> path.updateNameAndAddress(request.getName(), request.getAddress()));
    }

    private List<Bookmark> findMemberBookmarks(List<Long> bookmarkIds, Long memberId) {
        return bookmarkRepository.findAllByBookmarkIdInAndMemberMemberId(bookmarkIds, memberId);
    }

    private void unlinkBookmarksFromRecentPaths(List<Bookmark> bookmarks) {
        final List<RecentPath> recentPaths = recentPathRepository.findAllByBookmarkIn(bookmarks);
        recentPaths.forEach(RecentPath::unlinkBookmark);
    }

    private Member getMember(Long id) {
        return memberRepository.findById(id)
            .orElseThrow(() -> new BusinessException(MemberErrorCode.NOT_FOUND_MEMBER));
    }

    private void swapSequence(List<BookmarkSequenceUpdateRequest> requests,
        Map<Long, Bookmark> bookmarkMap, Map<Integer, Bookmark> sequenceMap) {
        for (BookmarkSequenceUpdateRequest request : requests) {
            final Bookmark bookmark = bookmarkMap.get(request.id());
            final Bookmark targetBookmark = sequenceMap.get(request.targetSequence());

            if (bookmark == null) {
                throw new BusinessException(BookmarkErrorCode.NOT_FOUND_BOOKMARK);
            }

            final int originalSequence = bookmark.getSequence();
            final int targetSequence = targetBookmark.getSequence();

            // sequence 값 변경 (스왑)
            bookmark.updateSequence(targetSequence);
            targetBookmark.updateSequence(originalSequence);

            // sequenceMap 업데이트
            sequenceMap.put(targetSequence, bookmark);
            sequenceMap.put(originalSequence, targetBookmark);
        }
    }
}
