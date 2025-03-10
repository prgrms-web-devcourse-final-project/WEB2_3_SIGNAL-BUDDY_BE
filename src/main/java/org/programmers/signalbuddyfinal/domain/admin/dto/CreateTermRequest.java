package org.programmers.signalbuddyfinal.domain.admin.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import org.programmers.signalbuddyfinal.domain.term.entity.enums.AgreementType;
import org.programmers.signalbuddyfinal.domain.term.entity.enums.TermCategory;

@Getter
public class CreateTermRequest {

    private TermCategory category;
    private String title;
    private String content;
    private String version;
    private LocalDate effectiveStartDate;
    private LocalDate effectiveEndDate;
    private AgreementType agreementType;
}
