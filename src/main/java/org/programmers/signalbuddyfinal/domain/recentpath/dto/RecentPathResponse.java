package org.programmers.signalbuddyfinal.domain.recentpath.dto;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
@EqualsAndHashCode
@Builder
public class RecentPathResponse {

    private Long recentPathId;

    private String name;

    private double lng;

    private double lat;

    private LocalDateTime lastAccessedAt;
}
