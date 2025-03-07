package org.programmers.signalbuddyfinal.domain.term.repository;

import java.time.LocalDate;
import org.programmers.signalbuddyfinal.domain.term.entity.Term;
import org.programmers.signalbuddyfinal.domain.term.entity.enums.TermCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TermRepository extends JpaRepository<Term, Long> {

    Boolean existsByTermTitle(String termTitle);

    @Query("select count(*) from Term t "
        + "inner join TermVersion tv on t.termId = tv.term.termId "
        + "where tv.effectiveEndDate <= :effectiveStartDate")
    int existsByEffectiveDate(TermCategory category, LocalDate effectiveStartDate);
}
