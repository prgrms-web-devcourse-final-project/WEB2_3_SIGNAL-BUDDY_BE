package org.programmers.signalbuddyfinal.domain.postit.repository;

import org.programmers.signalbuddyfinal.domain.member.entity.Member;
import org.programmers.signalbuddyfinal.domain.member.exception.MemberErrorCode;
import org.programmers.signalbuddyfinal.domain.postit.entity.Postit;
import org.programmers.signalbuddyfinal.domain.postit.exception.PostItErrorCode;
import org.programmers.signalbuddyfinal.global.exception.BusinessException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostItRepository extends JpaRepository<Postit, Long>, CustomPostItRepository {

    default Postit findByIdOrThrow(Long id) {
        return findById(id).orElseThrow(
            () -> new BusinessException(PostItErrorCode.NOT_FOUND_POSTIT));
    }
}
