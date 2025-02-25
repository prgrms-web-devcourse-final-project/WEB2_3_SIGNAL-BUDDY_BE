package org.programmers.signalbuddyfinal.domain.postit.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;
import org.programmers.signalbuddyfinal.domain.postit.entity.Danger;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class PostItRequest {

    private Danger danger;

    private double lat;

    private double lng;

    private String subject;

    private String content;

    private String imageUrl;

    private Long memberId;

    private LocalDateTime createDate;

}
