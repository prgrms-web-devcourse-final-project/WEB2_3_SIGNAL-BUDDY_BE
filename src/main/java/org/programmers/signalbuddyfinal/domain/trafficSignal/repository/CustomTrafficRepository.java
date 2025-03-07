package org.programmers.signalbuddyfinal.domain.trafficSignal.repository;

import org.programmers.signalbuddyfinal.domain.trafficSignal.dto.TrafficResponse;

import java.util.List;

public interface CustomTrafficRepository {

    List<TrafficResponse> findNearestTraffics(double lat, double lng, int radius);

}
