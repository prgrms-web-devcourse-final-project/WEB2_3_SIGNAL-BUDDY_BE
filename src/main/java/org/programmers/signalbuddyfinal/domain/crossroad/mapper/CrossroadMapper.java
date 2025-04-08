package org.programmers.signalbuddyfinal.domain.crossroad.mapper;

import org.locationtech.jts.geom.Point;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.programmers.signalbuddyfinal.domain.crossroad.dto.CrossroadResponse;
import org.programmers.signalbuddyfinal.domain.crossroad.dto.CrossroadStateApiResponse;
import org.programmers.signalbuddyfinal.domain.crossroad.dto.CrossroadStateResponse;
import org.programmers.signalbuddyfinal.domain.crossroad.entity.Crossroad;

@Mapper(componentModel = "spring")
public interface CrossroadMapper {

    CrossroadMapper INSTANCE = Mappers.getMapper(CrossroadMapper.class);

    @Mapping(target = "crossroadId", expression = "java(crossroad.getCrossroadId())")
    @Mapping(target = "crossroadApiId", expression = "java(apiResponse.getCrossroadApiId())")
    @Mapping(target = "name", expression = "java(crossroad.getName())")
    @Mapping(target = "lat", expression = "java(crossroad.getLat())")
    @Mapping(target = "lng", expression = "java(crossroad.getLng())")
    CrossroadStateResponse toStateResponse(
        CrossroadStateApiResponse apiResponse, CrossroadResponse crossroad
    );

    @Mapping(target = "lng", expression = "java(getLng(crossroad.getCoordinate()))")
    @Mapping(target = "lat", expression = "java(getLat(crossroad.getCoordinate()))")
    CrossroadResponse toResponse(Crossroad crossroad);

    default double getLng(Point point) {
        return point.getX();
    }
    default double getLat(Point point) {
        return point.getY();
    }
}
