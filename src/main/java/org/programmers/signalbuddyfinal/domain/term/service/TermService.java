package org.programmers.signalbuddyfinal.domain.term.service;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.programmers.signalbuddyfinal.domain.term.dto.TermResponse;
import org.programmers.signalbuddyfinal.domain.term.entity.enums.TermCategory;
import org.programmers.signalbuddyfinal.domain.term.exception.TermErrorCode;
import org.programmers.signalbuddyfinal.domain.term_version.entity.TermVersion;
import org.programmers.signalbuddyfinal.domain.term_version.repository.CustomTermVersionRepositoryImpl;
import org.programmers.signalbuddyfinal.domain.term_version.repository.TermVersionRepository;
import org.programmers.signalbuddyfinal.global.exception.BusinessException;
import org.programmers.signalbuddyfinal.global.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class TermService {

    private final CustomTermVersionRepositoryImpl customTermVersionRepository;
    private final TermVersionRepository termVersionRepository;

    public ResponseEntity<ApiResponse<TermResponse>> getTerm(TermCategory termCategory) {

        LocalDate now = LocalDate.now();
        TermVersion termVersion = customTermVersionRepository.findByTermCategory(now, termCategory)
            .orElseThrow(() -> new BusinessException(
                TermErrorCode.NO_EXIST_TERM_NOW));
        TermResponse termResponse = TermResponse.builder()
            .termId(termVersion.getTerm().getTermId())
            .termVersionId(termVersion.getTermVersionId())
            .content(termVersion.getTermContent())
            .build();

        return ResponseEntity.ok(ApiResponse.createSuccess(termResponse));
    }

}
