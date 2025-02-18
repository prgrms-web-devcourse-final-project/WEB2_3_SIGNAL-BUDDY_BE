package org.programmers.signalbuddyfinal.domain.crossroad.repository;

import org.programmers.signalbuddyfinal.domain.crossroad.dto.CrossroadApiResponse;

import java.util.List;

public interface QueryCrossroadRepository {
    List<CrossroadApiResponse> findNearByCrossroads(double latitude, double longitude);
}
