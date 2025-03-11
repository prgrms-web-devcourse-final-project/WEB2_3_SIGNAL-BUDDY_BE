package org.programmers.signalbuddyfinal.domain.term.repository;

import org.programmers.signalbuddyfinal.domain.term.entity.Term;
import org.programmers.signalbuddyfinal.domain.term.entity.enums.TermCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TermRepository extends JpaRepository<Term, Long> {

    Term findByTermTitleAndTermCategory(String termTitle, TermCategory termCategory);
}
