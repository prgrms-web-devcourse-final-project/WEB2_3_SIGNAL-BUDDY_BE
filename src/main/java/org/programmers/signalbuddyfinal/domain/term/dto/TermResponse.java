package org.programmers.signalbuddyfinal.domain.term.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TermResponse {

    private long termId;
    private long termVersionId;
    private String content;
}
