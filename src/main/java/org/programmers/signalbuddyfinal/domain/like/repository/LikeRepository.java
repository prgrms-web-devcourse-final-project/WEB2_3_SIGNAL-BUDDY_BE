package org.programmers.signalbuddyfinal.domain.like.repository;

import org.programmers.signalbuddyfinal.domain.like.entity.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {

    @Query("SELECT CASE  WHEN count(*) > 0 THEN true ELSE false END "
        + "FROM likes l "
        + "WHERE l.member.memberId = :memberId AND l.feedback.feedbackId = :feedbackId")
    boolean existsByMemberAndFeedback(@Param("memberId") Long memberId,
        @Param("feedbackId") Long feedbackId);
}
