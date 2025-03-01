package org.programmers.signalbuddyfinal.domain.postit_report.repository;

import org.programmers.signalbuddyfinal.domain.postit_report.entity.PostItReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostItReportRepository extends JpaRepository<PostItReport, Long> {

}
