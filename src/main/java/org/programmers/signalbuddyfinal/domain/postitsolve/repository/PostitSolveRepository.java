package org.programmers.signalbuddyfinal.domain.postitsolve.repository;

import io.lettuce.core.dynamic.annotation.Param;
import org.programmers.signalbuddyfinal.domain.postit.entity.Postit;
import org.programmers.signalbuddyfinal.domain.postit.exception.PostItErrorCode;
import org.programmers.signalbuddyfinal.domain.postitsolve.entity.PostitSolve;
import org.programmers.signalbuddyfinal.domain.postitsolve.exception.PostItSolveErrorCode;
import org.programmers.signalbuddyfinal.global.exception.BusinessException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PostitSolveRepository extends JpaRepository<PostitSolve, Long> {

    @Query("SELECT ps FROM postits_solves ps WHERE ps.postit.postitId = :postitId")
    PostitSolve findByPostItId(@Param("postitId") Long postitId);

    @Modifying
    @Query("DELETE FROM postits_solves ps WHERE ps.postit.postitId = :postitId")
    void deleteByPostItId(@Param("postitId") Long postitId);

    default PostitSolve findByIdOrThrow(Long id) {
        return findById(id).orElseThrow(
            () -> new BusinessException(PostItSolveErrorCode.NOT_FOUND_POSTIT_SOLVE));
    }

}
