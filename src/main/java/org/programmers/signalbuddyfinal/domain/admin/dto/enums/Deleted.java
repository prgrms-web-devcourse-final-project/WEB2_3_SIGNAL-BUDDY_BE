package org.programmers.signalbuddyfinal.domain.admin.dto.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Deleted {
    DELETED("해결"),NOTDELETED("미해결");

    private final String label;
}
