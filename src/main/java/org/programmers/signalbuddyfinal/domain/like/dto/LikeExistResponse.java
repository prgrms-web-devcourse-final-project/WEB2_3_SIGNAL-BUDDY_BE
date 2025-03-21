package org.programmers.signalbuddyfinal.domain.like.dto;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class LikeExistResponse {

    private boolean status;

    public static LikeExistResponse createTrue() {
        return new LikeExistResponse(true);
    }

    public static LikeExistResponse createFalse() {
        return new LikeExistResponse(false);
    }

    public boolean getStatus() {
        return this.status;
    }
}
