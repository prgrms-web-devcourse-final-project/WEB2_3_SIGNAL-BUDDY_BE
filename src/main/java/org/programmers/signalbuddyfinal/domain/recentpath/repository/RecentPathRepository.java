package org.programmers.signalbuddyfinal.domain.recentpath.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.locationtech.jts.geom.Point;
import org.programmers.signalbuddyfinal.domain.bookmark.entity.Bookmark;
import org.programmers.signalbuddyfinal.domain.member.entity.Member;
import org.programmers.signalbuddyfinal.domain.recentpath.entity.RecentPath;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecentPathRepository extends JpaRepository<RecentPath, Long> {

    List<RecentPath> findAllByMemberOrderByLastAccessedAtDesc(Member member, Limit limit);

    List<RecentPath> findAllByBookmarkIn(Collection<Bookmark> bookmarks);

    Optional<RecentPath> findByEndPoint(Point endPoint);
}
