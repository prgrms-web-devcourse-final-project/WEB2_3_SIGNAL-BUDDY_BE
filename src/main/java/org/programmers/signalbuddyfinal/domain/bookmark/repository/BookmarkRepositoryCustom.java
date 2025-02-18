package org.programmers.signalbuddyfinal.domain.bookmark.repository;

import org.programmers.signalbuddyfinal.domain.bookmark.dto.AdminBookmarkResponse;
import org.programmers.signalbuddyfinal.domain.bookmark.dto.BookmarkResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BookmarkRepositoryCustom {

    Page<BookmarkResponse> findPagedByMember(Pageable pageable, Long memberId);

    List<AdminBookmarkResponse> findBookmarkByMember(Long memberId);
}
