package org.programmers.signalbuddyfinal.domain.term_version.repository;

import static org.programmers.signalbuddyfinal.domain.term.entity.QTerm.term;
import static org.programmers.signalbuddyfinal.domain.term_version.entity.QTermVersion.termVersion;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.QBean;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.programmers.signalbuddyfinal.domain.admin.dto.AdminTermResponse;
import org.programmers.signalbuddyfinal.domain.term.entity.enums.TermCategory;
import org.programmers.signalbuddyfinal.domain.term_version.entity.QTermVersion;
import org.programmers.signalbuddyfinal.domain.term_version.entity.TermVersion;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class CustomTermVersionRepositoryImpl implements CustomTermVersionRepository {

    private final JPAQueryFactory jpaQueryFactory;

    private static final QBean<AdminTermResponse> adminTermResponseDTO = Projections.fields(
        AdminTermResponse.class, term.termId, termVersion.termVersionId, term.termTitle,
        term.agreementType,
        term.termCategory, termVersion.version, termVersion.termContent,
        termVersion.effectiveStartDate, termVersion.effectiveEndDate
    );

    @Override
    public Optional<TermVersion> findByTermCategory(LocalDate now, TermCategory termCategory) {
        return Optional.ofNullable(jpaQueryFactory.select(termVersion)
            .from(termVersion)
            .innerJoin(term)
            .on(term.termId.eq(termVersion.term.termId))
            .where(eqTermCategory(termCategory),
                betweenExpirationDate(now)).fetchOne());
    }

    @Override
    public AdminTermResponse findByTermId(Long termId, Long termVersionId) {
        return jpaQueryFactory.select(adminTermResponseDTO)
            .from(termVersion)
            .innerJoin(term)
            .on(term.termId.eq(termVersion.term.termId))
            .where(termVersion.termVersionId.eq(termVersionId)).fetchOne();
    }

    private BooleanExpression eqTermCategory(TermCategory termCategory) {
        return (termCategory != null) ? term.termCategory.eq(termCategory) : null;
    }

    private BooleanExpression betweenExpirationDate(LocalDate now) {
        return (termVersion.effectiveStartDate.goe(now).and(termVersion.effectiveEndDate.loe(now)));
    }
}
