package org.programmers.signalbuddyfinal.domain.postitsolve.repository;

import org.programmers.signalbuddyfinal.domain.postitsolve.entity.PostitSolve;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostitSolveRepository extends JpaRepository<PostitSolve, Long> {

}
