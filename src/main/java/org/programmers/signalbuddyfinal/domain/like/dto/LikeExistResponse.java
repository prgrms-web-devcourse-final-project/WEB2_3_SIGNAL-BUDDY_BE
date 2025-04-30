package org.programmers.signalbuddyfinal.domain.like.dto;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class LikeExistResponse {

    private boolean status;

    public boolean getStatus() {
        return this.status;
    }
}
