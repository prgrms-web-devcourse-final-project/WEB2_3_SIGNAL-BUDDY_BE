package org.programmers.signalbuddyfinal.domain.term_version.repository;

import java.time.LocalDate;
import java.util.Optional;
import org.programmers.signalbuddyfinal.domain.admin.dto.AdminTermResponse;
import org.programmers.signalbuddyfinal.domain.term.entity.enums.TermCategory;
import org.programmers.signalbuddyfinal.domain.term_version.entity.TermVersion;

public interface CustomTermVersionRepository {
    Optional<TermVersion> findByTermCategory(LocalDate now, TermCategory termCategory);
    AdminTermResponse findByTermId(Long termId, Long termVersionId);
}
