package org.programmers.signalbuddyfinal.domain.feedback.repository;

import org.programmers.signalbuddyfinal.domain.feedback.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FeedbackRepository extends
    JpaRepository<Feedback, Long>, CustomFeedbackRepository {

}
