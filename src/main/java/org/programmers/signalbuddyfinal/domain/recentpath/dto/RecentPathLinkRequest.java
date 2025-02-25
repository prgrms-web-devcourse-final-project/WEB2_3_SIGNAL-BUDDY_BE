package org.programmers.signalbuddyfinal.domain.recentpath.dto;

import jakarta.validation.constraints.NotNull;

public record RecentPathLinkRequest(@NotNull Long bookmarkId) {
}
