package org.programmers.signalbuddyfinal.domain.admin.dto;

import java.time.LocalDate;
import lombok.Getter;
import org.programmers.signalbuddyfinal.domain.term.entity.enums.AgreementType;
import org.programmers.signalbuddyfinal.domain.term.entity.enums.TermCategory;

@Getter
public class AdminTermResponse {

    private long termId;
    private long termVersionId;
    private String termTitle;
    private AgreementType agreementType;
    private TermCategory termCategory;
    private String version;
    private String termContent;
    private LocalDate effectiveStartDate;
    private LocalDate effectiveEndDate;
}
