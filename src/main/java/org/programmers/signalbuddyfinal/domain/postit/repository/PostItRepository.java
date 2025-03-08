package org.programmers.signalbuddyfinal.domain.postit.repository;

import org.programmers.signalbuddyfinal.domain.postit.entity.Postit;
import org.programmers.signalbuddyfinal.domain.postit.exception.PostItErrorCode;
import org.programmers.signalbuddyfinal.global.exception.BusinessException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PostItRepository extends JpaRepository<Postit, Long>, CustomPostItRepository {

    @Query("SELECT p FROM postits p WHERE p.postitId = :postItId")
    Postit findCountById(@Param("postItId") Long postItId);

    default Postit findByIdOrThrow(Long id) {
        return findById(id).orElseThrow(
            () -> new BusinessException(PostItErrorCode.NOT_FOUND_POSTIT));
    }
}
