package org.programmers.signalbuddyfinal.domain.crossroad.repository;

import org.programmers.signalbuddyfinal.domain.crossroad.dto.CrossroadApiResponse;
import org.programmers.signalbuddyfinal.domain.crossroad.dto.CrossroadResponse;

import java.util.List;
import org.locationtech.jts.geom.Point;

public interface CustomCrossroadRepository {

    List<CrossroadApiResponse> findNearByCrossroads(double latitude, double longitude);

    List<CrossroadResponse> findNearestCrossroads(double lat, double lng, int radius);

    List<Long> findByCoordinateInWithRadius(List<Point> points, int radius);

}
