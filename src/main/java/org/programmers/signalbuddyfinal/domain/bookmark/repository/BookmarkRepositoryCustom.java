package org.programmers.signalbuddyfinal.domain.bookmark.repository;

import java.util.Optional;
import org.locationtech.jts.geom.Point;
import org.programmers.signalbuddyfinal.domain.bookmark.dto.AdminBookmarkResponse;
import org.programmers.signalbuddyfinal.domain.bookmark.dto.BookmarkResponse;
import org.programmers.signalbuddyfinal.domain.bookmark.entity.Bookmark;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BookmarkRepositoryCustom {

    Page<BookmarkResponse> findPagedByMember(Pageable pageable, Long memberId);

    List<AdminBookmarkResponse> findBookmarkByMember(Long memberId);

    Optional<Bookmark> findByCoordinateAndMemberIdNotDeleted(Point endPoint, Long memberId);
}
