package org.programmers.signalbuddyfinal.domain.admin.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import org.programmers.signalbuddyfinal.domain.term.entity.enums.AgreementType;
import org.programmers.signalbuddyfinal.domain.term.entity.enums.TermCategory;

@Builder
@Getter
public class CreateTermRequest {

    private TermCategory category;
    private String title;
    private String content;
    private String version;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate effectiveStartDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate effectiveEndDate;
    private AgreementType agreementType;
}
