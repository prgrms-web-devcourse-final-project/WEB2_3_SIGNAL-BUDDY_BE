package org.programmers.signalbuddyfinal.domain.admin.service;

import lombok.RequiredArgsConstructor;
import org.programmers.signalbuddyfinal.domain.admin.dto.AdminTermResponse;
import org.programmers.signalbuddyfinal.domain.admin.dto.CreateTermRequest;
import org.programmers.signalbuddyfinal.domain.term.entity.Term;
import org.programmers.signalbuddyfinal.domain.term.entity.enums.TermCategory;
import org.programmers.signalbuddyfinal.domain.term.exception.TermErrorCode;
import org.programmers.signalbuddyfinal.domain.term.repository.TermRepository;
import org.programmers.signalbuddyfinal.domain.term_version.entity.TermVersion;
import org.programmers.signalbuddyfinal.domain.term_version.repository.CustomTermVersionRepositoryImpl;
import org.programmers.signalbuddyfinal.domain.term_version.repository.TermVersionRepository;
import org.programmers.signalbuddyfinal.global.exception.BusinessException;
import org.programmers.signalbuddyfinal.global.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class AdminTermService {

    private final TermRepository termRepository;
    private final TermVersionRepository termVersionRepository;
    private final CustomTermVersionRepositoryImpl customTermVersionRepository;

    @Transactional
    public ResponseEntity<ApiResponse<Object>> registerTerm(CreateTermRequest createTermRequest) {

        TermCategory category = createTermRequest.getCategory();
        String version = createTermRequest.getVersion();
        String title = category.getName() + "_v" + version;

        if (termRepository.existsByTermTitle(title)) {
            throw new BusinessException(TermErrorCode.ALREADY_EXIST_TERM);
        }

        if(createTermRequest.getEffectiveStartDate().isAfter(createTermRequest.getEffectiveEndDate())){
            throw new BusinessException(TermErrorCode.START_DATE_AFTER_END_DATE);
        }

        if (termRepository.existsByEffectiveDate(category,
            createTermRequest.getEffectiveStartDate())>0) {
            throw new BusinessException(TermErrorCode.EFFECTIVE_DATE_CHANGE_REQUIRED);
        }

        Term savedTerm = termRepository.save(Term.builder()
            .termCategory(category)
            .termTitle(title)
            .agreementType(createTermRequest.getAgreementType())
            .build());

        termVersionRepository.save(TermVersion.builder()
            .version(version)
            .termContent(createTermRequest.getContent())
            .effectiveStartDate(createTermRequest.getEffectiveStartDate())
            .effectiveEndDate(createTermRequest.getEffectiveEndDate())
            .term(savedTerm)
            .build());

        return ResponseEntity.ok().body(ApiResponse.createSuccessWithNoData());
    }

    @Transactional
    public ResponseEntity<ApiResponse<AdminTermResponse>> getDetailTerm(Long termId, Long termVersionId) {

        AdminTermResponse adminTermResponse = customTermVersionRepository.findByTermId(termId, termVersionId);
        if (adminTermResponse == null) { throw new BusinessException(TermErrorCode.NO_EXIST_TERM);}

        return ResponseEntity.ok(ApiResponse.createSuccess(adminTermResponse));
    }
}
