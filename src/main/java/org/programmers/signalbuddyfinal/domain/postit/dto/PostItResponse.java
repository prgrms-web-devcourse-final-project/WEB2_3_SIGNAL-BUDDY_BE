package org.programmers.signalbuddyfinal.domain.postit.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
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
public class PostItResponse {

    private Long postitId;

    private Danger danger;

    private double lat;

    private double lng;

    private String subject;

    private String content;

    private String imageUrl;

    @JsonFormat(pattern = "YYYY-MM-dd HH:mm")
    private LocalDateTime expiryDate;

    @JsonFormat(pattern = "YYYY-MM-dd HH:mm")
    private LocalDateTime createDate;

    private Long memberId;

}
