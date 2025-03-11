package org.programmers.signalbuddyfinal.domain.term.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TermResponse {

    private Long termId;
    private Long termVersionId;
    private String content;
}
