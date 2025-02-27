package org.programmers.signalbuddyfinal.domain.admin.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.programmers.signalbuddyfinal.domain.postit.entity.Danger;
import org.springframework.format.annotation.DateTimeFormat;

@AllArgsConstructor
@Getter
@NoArgsConstructor
@Builder
public class AdminPostItResponse {

    private Danger danger;

    private String subject;

    private String content;

    private String email;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime expireDate;

}
