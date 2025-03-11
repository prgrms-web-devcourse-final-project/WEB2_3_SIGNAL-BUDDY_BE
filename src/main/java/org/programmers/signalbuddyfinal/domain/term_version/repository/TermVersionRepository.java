package org.programmers.signalbuddyfinal.domain.term_version.repository;

import org.programmers.signalbuddyfinal.domain.term.entity.Term;
import org.programmers.signalbuddyfinal.domain.term_version.entity.TermVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TermVersionRepository extends JpaRepository<TermVersion, Long> {

    boolean existsByTermAndVersion(Term term, String termVersion);
}
