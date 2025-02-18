package org.programmers.signalbuddyfinal.domain.bookmark.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class AdminBookmarkResponse {

    private Long bookmarkId;

    private String address;

}
