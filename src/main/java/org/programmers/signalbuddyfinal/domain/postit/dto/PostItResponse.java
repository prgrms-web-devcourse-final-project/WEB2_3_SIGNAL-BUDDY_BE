package org.programmers.signalbuddyfinal.domain.postit.dto;

import java.time.LocalDateTime;
import org.locationtech.jts.geom.Point;
import org.programmers.signalbuddyfinal.domain.postit.entity.Danger;

public class PostItResponse {

    private Long postitId;

    private Danger danger;

    private Point coordinate;

    private String subject;

    private String content;

    private String imageUrl;

    private LocalDateTime expiryDate;

    private LocalDateTime createDate;

    private Long memberId;

}
