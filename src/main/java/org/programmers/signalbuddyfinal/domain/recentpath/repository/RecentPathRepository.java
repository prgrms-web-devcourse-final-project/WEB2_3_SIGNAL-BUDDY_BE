package org.programmers.signalbuddyfinal.domain.recentpath.repository;

import org.programmers.signalbuddyfinal.domain.recentpath.entity.RecentPath;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecentPathRepository extends JpaRepository<RecentPath, Long> {

}
