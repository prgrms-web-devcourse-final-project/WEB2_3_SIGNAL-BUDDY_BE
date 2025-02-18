package org.programmers.signalbuddyfinal.domain.bookmark.repository;

import org.programmers.signalbuddyfinal.domain.bookmark.entity.Bookmark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, Long>,
    BookmarkRepositoryCustom {

}
