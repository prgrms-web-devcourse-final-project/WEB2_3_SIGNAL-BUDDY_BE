package org.programmers.signalbuddyfinal.domain.trafficSignal.repository;

import org.programmers.signalbuddyfinal.domain.trafficSignal.entity.TrafficSignal;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrafficRepository extends CrudRepository<TrafficSignal, Long> {
}
