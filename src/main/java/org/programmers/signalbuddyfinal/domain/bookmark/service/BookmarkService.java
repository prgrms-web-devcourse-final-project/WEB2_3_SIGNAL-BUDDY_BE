package org.programmers.signalbuddyfinal.domain.bookmark.service;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final GeometryFactory geometryFactory;
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

        final Point point = toPoint(request.getLng(), request.getLat());
        final int nextSequence =
            bookmarkRepository.findTopByMemberOrderBySequenceDesc(member).map(Bookmark::getSequence)
                .orElse(0) + 1;

        final Bookmark bookmark = BookmarkMapper.INSTANCE.toEntity(request, point, member);
        bookmark.updateSequence(nextSequence);
        final Bookmark save = bookmarkRepository.save(bookmark);
        return BookmarkMapper.INSTANCE.toDto(save);
    }

    @Transactional
    public BookmarkResponse updateBookmark(BookmarkRequest request, Long id, Long memberId) {
        final Member member = getMember(memberId);

        // TODO : 성능 개선
//        final Bookmark bookmark = bookmarkRepository.findByBookmarkIdAndMemberMemberId(id, memberId)
//            .orElseThrow(() -> new BusinessException(BookmarkErrorCode.NOT_FOUND_BOOKMARK));
        final Bookmark bookmark = bookmarkRepository.findById(id)
            .orElseThrow(() -> new BusinessException(BookmarkErrorCode.NOT_FOUND_BOOKMARK));

        if (bookmark.isNotOwnedBy(member)) {
            throw new BusinessException(BookmarkErrorCode.UNAUTHORIZED_MEMBER_ACCESS);
        }

        final Point point = toPoint(request.getLng(), request.getLat());

        bookmark.update(point, request.getAddress(), request.getName());
        return BookmarkMapper.INSTANCE.toDto(bookmark);
    }

    @Transactional
    public void deleteBookmark(List<Long> bookmarkIds, Long memberId) {
        final List<Bookmark> bookmarkList = bookmarkRepository.findAllByBookmarkIdInAndMemberMemberId(
            bookmarkIds, memberId);

        final List<RecentPath> recentPaths = recentPathRepository.findAllByBookmarkIn(bookmarkList);

        bookmarkList.forEach(Bookmark::delete);

        // 북마크 삭제 시 최근경로에서 북마크 연관관계 해제
        recentPaths.forEach(RecentPath::unlinkBookmark);

        log.info("Bookmark deleted: {}", bookmarkIds);
    }

    private Member getMember(Long id) {
        return memberRepository.findById(id)
            .orElseThrow(() -> new BusinessException(MemberErrorCode.NOT_FOUND_MEMBER));
    }

    private Point toPoint(double lng, double lat) {
        if (lng < -180 || lng > 180 || lat < -90 || lat > 90) {
            throw new BusinessException(BookmarkErrorCode.INVALID_COORDINATES);
        }
        return geometryFactory.createPoint(new Coordinate(lng, lat));
    }

    @Transactional(readOnly = true)
    public BookmarkResponse getBookmark(Long id, Long bookmarkId) {
        final Member member = getMember(id);
        final Bookmark bookmark = bookmarkRepository.findById(bookmarkId)
            .orElseThrow(() -> new BusinessException(BookmarkErrorCode.NOT_FOUND_BOOKMARK));
        if (bookmark.isNotOwnedBy(member)) {
            throw new BusinessException(BookmarkErrorCode.UNAUTHORIZED_MEMBER_ACCESS);
        }
        return BookmarkMapper.INSTANCE.toDto(bookmark);
    }

    @Transactional
    public List<BookmarkResponse> updateBookmarkSequences(Long id,
        @Valid List<BookmarkSequenceUpdateRequest> requests) {
        final List<Long> bookmarkIds = requests.stream().map(BookmarkSequenceUpdateRequest::id)
            .toList();
        final List<Integer> targetSequences = requests.stream()
            .map(BookmarkSequenceUpdateRequest::targetSequence).toList();

        // 북마크 ID로 목록 조회
        final List<Bookmark> bookmarks = bookmarkRepository.findAllByBookmarkIdInAndMemberMemberId(
            bookmarkIds, id);

        // Target Sequence 로 목록 조회
        final List<Bookmark> targetBookmarks = bookmarkRepository.findAllBySequenceInAndMemberMemberId(
            targetSequences, id);

        // id -> Bookmark 매핑
        final Map<Long, Bookmark> bookmarkMap = bookmarks.stream()
            .collect(Collectors.toMap(Bookmark::getBookmarkId, Function.identity()));
        // sequence -> bookmark 매핑
        final Map<Integer, Bookmark> sequenceMap = targetBookmarks.stream()
            .collect(Collectors.toMap(Bookmark::getSequence, Function.identity()));

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

        return bookmarks.stream().map(BookmarkMapper.INSTANCE::toDto).toList();
    }
}
