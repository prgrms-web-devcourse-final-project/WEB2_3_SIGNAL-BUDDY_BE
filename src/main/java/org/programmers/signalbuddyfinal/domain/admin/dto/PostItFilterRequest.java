package org.programmers.signalbuddyfinal.domain.admin.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.programmers.signalbuddyfinal.domain.admin.dto.enums.Deleted;
import org.programmers.signalbuddyfinal.domain.postit.entity.Danger;

@AllArgsConstructor
@Getter
@NoArgsConstructor
@Builder
public class PostItFilterRequest {

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private Danger danger;

    private String search;

    private Deleted deleted;
}
