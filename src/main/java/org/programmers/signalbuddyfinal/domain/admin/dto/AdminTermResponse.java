package org.programmers.signalbuddyfinal.domain.admin.dto;

import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;
import org.programmers.signalbuddyfinal.domain.term.entity.enums.AgreementType;
import org.programmers.signalbuddyfinal.domain.term.entity.enums.TermCategory;

@Builder
@Getter
public class AdminTermResponse {

    private Long termId;
    private Long termVersionId;
    private String title;
    private AgreementType agreementType;
    private TermCategory category;
    private String version;
    private String content;
    private LocalDate effectiveStartDate;
    private LocalDate effectiveEndDate;
}
