package org.programmers.signalbuddyfinal.domain.crossroad.repository;

import org.programmers.signalbuddyfinal.domain.crossroad.entity.Crossroad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface CrossroadRepository extends JpaRepository<Crossroad, Long>, QueryCrossroadRepository{

}
