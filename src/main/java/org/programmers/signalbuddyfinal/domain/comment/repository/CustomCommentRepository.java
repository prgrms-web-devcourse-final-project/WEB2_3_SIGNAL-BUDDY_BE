package org.programmers.signalbuddyfinal.domain.comment.repository;

import org.programmers.signalbuddyfinal.domain.comment.dto.CommentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomCommentRepository {

    Page<CommentResponse> findAllByFeedbackId(Long feedbackId, Pageable pageable);
}
