package org.programmers.signalbuddyfinal.domain.like.repository;

import org.programmers.signalbuddyfinal.domain.feedback.dto.FeedbackResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomLikeRepository {

    Page<FeedbackResponse> findPagedLikedFeedbacks(Long memberId, Pageable pageable);
}
