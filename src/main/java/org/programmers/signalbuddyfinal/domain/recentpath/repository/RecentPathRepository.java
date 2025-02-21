package org.programmers.signalbuddyfinal.domain.recentpath.repository;

import java.util.List;
import org.programmers.signalbuddyfinal.domain.member.entity.Member;
import org.programmers.signalbuddyfinal.domain.recentpath.entity.RecentPath;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecentPathRepository extends JpaRepository<RecentPath, Long> {

    List<RecentPath> findAllByMemberOrderByLastAccessedAtDesc(Member member, Limit limit);
}
