package org.programmers.signalbuddyfinal.domain.like.dto;

import lombok.Builder;
import lombok.Getter;
import org.programmers.signalbuddyfinal.domain.like.exception.LikeErrorCode;
import org.programmers.signalbuddyfinal.global.exception.BusinessException;

import java.util.Objects;

@Getter
public class LikeUpdateRequest {

    private Long feedbackId;
    private Long memberId;
    private LikeRequestType likeRequestType;

    @Builder
    private LikeUpdateRequest(Long feedbackId, Long memberId, String likeRequestType) {
        this.feedbackId = Objects.requireNonNull(feedbackId);
        this.memberId = Objects.requireNonNull(memberId);
        this.likeRequestType = toEnum(Objects.requireNonNull(likeRequestType));
    }

    private LikeRequestType toEnum(String likeRequestType) {
        if (likeRequestType.equals(LikeRequestType.ADD.name())) {
            return LikeRequestType.ADD;
        }
        if (likeRequestType.equals(LikeRequestType.CANCEL.name())) {
            return LikeRequestType.CANCEL;
        }

        throw new BusinessException(LikeErrorCode.Illegal_REQUEST_TYPE);
    }
}
