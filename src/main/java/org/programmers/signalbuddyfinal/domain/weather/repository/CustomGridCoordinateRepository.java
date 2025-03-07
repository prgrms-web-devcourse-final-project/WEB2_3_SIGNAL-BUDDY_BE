package org.programmers.signalbuddyfinal.domain.weather.repository;

import org.programmers.signalbuddyfinal.domain.weather.dto.GridResponse;

public interface CustomGridCoordinateRepository {

    GridResponse findByLatAndLngWithRadius(double lat, double lng, double radius);
}
