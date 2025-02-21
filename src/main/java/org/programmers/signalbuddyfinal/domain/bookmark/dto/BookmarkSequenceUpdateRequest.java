package org.programmers.signalbuddyfinal.domain.bookmark.dto;

import jakarta.validation.constraints.Min;

public record BookmarkSequenceUpdateRequest(long id, @Min(1) int targetSequence) {

}
