package org.programmers.signalbuddyfinal.domain.comment.repository;

import org.programmers.signalbuddyfinal.domain.comment.entity.Comment;
import org.programmers.signalbuddyfinal.domain.comment.exception.CommentErrorCode;
import org.programmers.signalbuddyfinal.global.exception.BusinessException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long>, CustomCommentRepository {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE comments c SET c.deletedAt = now() WHERE c.feedback.feedbackId = :feedbackId")
    void softDeleteAllByFeedbackId(@Param("feedbackId") Long feedbackId);

    default Comment findByIdOrThrow(Long id) {
        return findById(id)
            .orElseThrow(() -> new BusinessException(CommentErrorCode.NOT_FOUND_COMMENT));
    }
}
