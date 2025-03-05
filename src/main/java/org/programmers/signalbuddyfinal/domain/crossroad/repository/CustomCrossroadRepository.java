package org.programmers.signalbuddyfinal.domain.crossroad.repository;

import org.programmers.signalbuddyfinal.domain.crossroad.dto.CrossroadApiResponse;

import java.util.List;
import org.programmers.signalbuddyfinal.domain.crossroad.dto.CrossroadResponse;

public interface CustomCrossroadRepository {
    List<CrossroadApiResponse> findNearByCrossroads(double latitude, double longitude);

    List<CrossroadResponse> findNearestCrossroads(double lat, double lng, int radius);
}
