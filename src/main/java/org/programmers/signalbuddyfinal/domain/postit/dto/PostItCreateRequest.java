package org.programmers.signalbuddyfinal.domain.postit.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.programmers.signalbuddyfinal.domain.postit.entity.Danger;
import org.springframework.format.annotation.DateTimeFormat;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class PostItCreateRequest {

    private Danger danger;

    private double lat;

    private double lng;

    private String subject;

    private String content;

    @DateTimeFormat(pattern = "YYYY-MM-DD HH:mm")
    private LocalDateTime createDate;

}
