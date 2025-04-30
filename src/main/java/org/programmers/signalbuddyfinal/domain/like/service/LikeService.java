package org.programmers.signalbuddyfinal.domain.like.service;

import lombok.RequiredArgsConstructor;
import org.programmers.signalbuddyfinal.domain.like.dto.LikeExistResponse;
import org.programmers.signalbuddyfinal.domain.like.dto.LikeRequestType;
import org.programmers.signalbuddyfinal.domain.like.exception.LikeErrorCode;
import org.programmers.signalbuddyfinal.domain.like.repository.LikeRepository;
import org.programmers.signalbuddyfinal.global.dto.CustomUser2Member;
import org.programmers.signalbuddyfinal.global.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LikeService {

    private final LikeRepository likeRepository;
    private final LikeCacheService likeCacheService;

    private static final String LIKE_KEY_PREFIX = "like:";

    @Transactional
    public void addLike(Long feedbackId, CustomUser2Member user) {
        String key = LikeCacheService.generateKey(feedbackId, user.getMemberId());

        // 삭제 요청 데이터가 Redis에 있을 때
        if (likeCacheService.exists(key)) {
            likeCacheService.delete(key);
            return;
        }

        boolean isExisted = likeRepository.existsByMemberAndFeedback(user.getMemberId(), feedbackId);
        if (isExisted) {
            throw new BusinessException(LikeErrorCode.ALREADY_ADDED_LIKE);
        }

        likeCacheService.addLike(key);
    }

    public LikeExistResponse existsLike(Long feedbackId, CustomUser2Member user) {
        String key = LikeCacheService.generateKey(feedbackId, user.getMemberId());

        // Redis에 임시 저장되어 있는 경우
        String cacheLike = likeCacheService.getLikeType(key);
        if (cacheLike != null) {
            // 좋아요 추가 요청일 때
            if (LikeRequestType.ADD.name().equals(cacheLike)) {
                return LikeExistResponse.createTrue();
            }
            // 좋아요 삭제 요청일 때
            return LikeExistResponse.createFalse();
        }

        boolean isExisted = likeRepository.existsByMemberAndFeedback(user.getMemberId(), feedbackId);
        return new LikeExistResponse(isExisted);
    }

    @Transactional
    public void deleteLike(Long feedbackId, CustomUser2Member user) {
        String key = LikeCacheService.generateKey(feedbackId, user.getMemberId());

        // 좋아요 데이터가 아직 DB에 저장되지 않은 경우 (Redis에만 있을 때)
        if (likeCacheService.exists(key)) {
            likeCacheService.delete(key);
            return;
        }

        boolean isExisted = likeRepository.existsByMemberAndFeedback(user.getMemberId(), feedbackId);
        if (!isExisted) {
            throw new BusinessException(LikeErrorCode.NOT_FOUND_LIKE);
        }

        likeCacheService.cancelLike(key);
    }

    public static String getLikeKeyPrefix() {
        return LIKE_KEY_PREFIX;
    }
}
