package org.programmers.signalbuddyfinal.domain.recentpath.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.programmers.signalbuddyfinal.domain.bookmark.dto.BookmarkRequest;
import org.programmers.signalbuddyfinal.domain.bookmark.entity.Bookmark;
import org.programmers.signalbuddyfinal.domain.bookmark.mapper.BookmarkMapper;
import org.programmers.signalbuddyfinal.domain.bookmark.repository.BookmarkRepository;
import org.programmers.signalbuddyfinal.domain.member.entity.Member;
import org.programmers.signalbuddyfinal.domain.member.exception.MemberErrorCode;
import org.programmers.signalbuddyfinal.domain.member.repository.MemberRepository;
import org.programmers.signalbuddyfinal.domain.recentpath.dto.RecentPathLinkRequest;
import org.programmers.signalbuddyfinal.domain.recentpath.dto.RecentPathRequest;
import org.programmers.signalbuddyfinal.domain.recentpath.dto.RecentPathResponse;
import org.programmers.signalbuddyfinal.domain.recentpath.entity.RecentPath;
import org.programmers.signalbuddyfinal.domain.recentpath.exception.RecentPathErrorCode;
import org.programmers.signalbuddyfinal.domain.recentpath.mapper.RecentPathMapper;
import org.programmers.signalbuddyfinal.domain.recentpath.repository.RecentPathRepository;
import org.programmers.signalbuddyfinal.global.exception.BusinessException;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RecentPathService {

    private final RecentPathRepository recentPathRepository;
    private final MemberRepository memberRepository;
    private final BookmarkRepository bookmarkRepository;
    private final GeometryFactory geometryFactory;

    @Transactional
    public RecentPathResponse saveRecentPath(Long memberId, RecentPathRequest request) {
        final Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new BusinessException(MemberErrorCode.NOT_FOUND_MEMBER));
        final Point point = toPoint(request.getLng(), request.getLat());

        final RecentPath recentPath = findOrCreateRecentPath(point, request, member);

        // 최근경로에 추가되는 좌표가 북마크에 저장되어 있으면 연관관계 생성.
        bookmarkRepository.findByCoordinateAndMemberIdNotDeleted(point, memberId)
            .ifPresent(recentPath::linkBookmark);

        final RecentPath save = recentPathRepository.save(recentPath);
        return RecentPathMapper.INSTANCE.toDto(save);
    }

    @Transactional(readOnly = true)
    public List<RecentPathResponse> getRecentPathList(Long memberId) {
        final Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new BusinessException(MemberErrorCode.NOT_FOUND_MEMBER));

        final List<RecentPath> list = recentPathRepository.findAllByMemberOrderByLastAccessedAtDesc(
            member, Limit.of(10));
        return list.stream().map(RecentPathMapper.INSTANCE::toDto).toList();
    }

    private Point toPoint(double lng, double lat) {
        if (lng < -180 || lng > 180 || lat < -90 || lat > 90) {
            throw new BusinessException(RecentPathErrorCode.INVALID_COORDINATES);
        }
        return geometryFactory.createPoint(new Coordinate(lng, lat));
    }

    @Transactional
    public RecentPathResponse updateRecentPathTime(Long id) {
        final RecentPath recentPath = recentPathRepository.findById(id)
            .orElseThrow(() -> new BusinessException(RecentPathErrorCode.NOT_FOUND_RECENT_PATH));

        recentPath.updateLastAccessedTime();
        return RecentPathMapper.INSTANCE.toDto(recentPath);
    }

    @Transactional
    public void unlinkBookmark(Long id) {
        final RecentPath recentPath = recentPathRepository.findById(id)
            .orElseThrow(() -> new BusinessException(RecentPathErrorCode.NOT_FOUND_RECENT_PATH));

        recentPath.getBookmark().delete();
        recentPath.unlinkBookmark();
    }

    @Transactional
    public RecentPathResponse linkBookmark(Long id, RecentPathLinkRequest recentPathLinkRequest) {
        final RecentPath recentPath = recentPathRepository.findById(id)
            .orElseThrow(() -> new BusinessException(RecentPathErrorCode.NOT_FOUND_RECENT_PATH));

        final Member member = memberRepository.findById(recentPathLinkRequest.memberId())
            .orElseThrow(() -> new BusinessException(MemberErrorCode.NOT_FOUND_MEMBER));

        final Bookmark bookmark = findOrCreateBookmark(recentPath, member);
        recentPath.linkBookmark(bookmark);

        return RecentPathMapper.INSTANCE.toDto(recentPath);
    }

    private Bookmark findOrCreateBookmark(RecentPath recentPath, Member member) {
        return bookmarkRepository.findByCoordinateAndMemberIdNotDeleted(recentPath.getEndPoint(),
            member.getMemberId()).orElseGet(() -> createNewBookmark(recentPath, member));
    }

    private Bookmark createNewBookmark(RecentPath recentPath, Member member) {
        final int nextSequence =
            bookmarkRepository.findTopByMemberOrderBySequenceDesc(member).map(Bookmark::getSequence)
                .orElse(0) + 1;

        final BookmarkRequest bookmarkRequest = BookmarkRequest.builder().address(recentPath.getAddress())
            .name(recentPath.getName()).build();

        final Bookmark newBookmark = BookmarkMapper.INSTANCE.toEntity(bookmarkRequest,
            recentPath.getEndPoint(), member);
        newBookmark.updateSequence(nextSequence);

        return bookmarkRepository.save(newBookmark);
    }


    private RecentPath findOrCreateRecentPath(Point point, RecentPathRequest request,
        Member member) {
        return recentPathRepository.findByEndPointAndMemberMemberId(point, member.getMemberId()).map(existingPath -> {
            existingPath.updateLastAccessedTime();
            return existingPath;
        }).orElseGet(() -> RecentPathMapper.INSTANCE.toEntity(request, point, member));
    }
}
