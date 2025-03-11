package org.programmers.signalbuddyfinal.domain.weather.repository;

import org.programmers.signalbuddyfinal.domain.weather.entity.GridCoordinate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GridCoordinateRepository extends JpaRepository<GridCoordinate, Long>,
    CustomGridCoordinateRepository {

}
