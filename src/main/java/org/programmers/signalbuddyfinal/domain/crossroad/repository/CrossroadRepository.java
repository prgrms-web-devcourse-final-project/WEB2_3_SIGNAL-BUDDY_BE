package org.programmers.signalbuddyfinal.domain.crossroad.repository;

import org.programmers.signalbuddyfinal.domain.crossroad.entity.Crossroad;
import org.programmers.signalbuddyfinal.domain.crossroad.exception.CrossroadErrorCode;
import org.programmers.signalbuddyfinal.global.exception.BusinessException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface CrossroadRepository extends JpaRepository<Crossroad, Long>,
    CustomCrossroadRepository {

    default Crossroad findByIdOrThrow(Long id) {
        return findById(id).orElseThrow(
            () -> new BusinessException(CrossroadErrorCode.NOT_FOUND_CROSSROAD));
    }
}
