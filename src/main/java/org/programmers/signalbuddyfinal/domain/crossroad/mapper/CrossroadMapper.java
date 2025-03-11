package org.programmers.signalbuddyfinal.domain.crossroad.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.programmers.signalbuddyfinal.domain.crossroad.dto.CrossroadResponse;
import org.programmers.signalbuddyfinal.domain.crossroad.dto.CrossroadStateApiResponse;
import org.programmers.signalbuddyfinal.domain.crossroad.dto.CrossroadStateResponse;

@Mapper
public interface CrossroadMapper {

    CrossroadMapper INSTANCE = Mappers.getMapper(CrossroadMapper.class);

    @Mapping(target = "crossroadId", expression = "java(crossroad.getCrossroadId())")
    @Mapping(target = "crossroadApiId", expression = "java(apiResponse.getCrossroadApiId())")
    @Mapping(target = "name", expression = "java(crossroad.getName())")
    @Mapping(target = "lat", expression = "java(crossroad.getLat())")
    @Mapping(target = "lng", expression = "java(crossroad.getLng())")
    CrossroadStateResponse toResponse(
        CrossroadStateApiResponse apiResponse, CrossroadResponse crossroad
    );
}
