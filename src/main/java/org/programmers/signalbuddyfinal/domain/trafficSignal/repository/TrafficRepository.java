package org.programmers.signalbuddyfinal.domain.trafficSignal.repository;

import org.programmers.signalbuddyfinal.domain.trafficSignal.entity.TrafficSignal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrafficRepository extends JpaRepository<TrafficSignal, Long> {
}
