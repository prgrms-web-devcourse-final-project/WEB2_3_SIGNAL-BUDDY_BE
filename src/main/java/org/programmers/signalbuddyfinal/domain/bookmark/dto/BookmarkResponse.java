package org.programmers.signalbuddyfinal.domain.bookmark.dto;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
@EqualsAndHashCode
public class BookmarkResponse {

    private Long bookmarkId;

    private double lat;

    private double lng;

    private String address;

    private String name;

    private int sequence;
}
