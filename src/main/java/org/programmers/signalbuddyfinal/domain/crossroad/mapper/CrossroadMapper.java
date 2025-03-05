package org.programmers.signalbuddyfinal.domain.crossroad.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.programmers.signalbuddyfinal.domain.crossroad.dto.CrossroadStateApiResponse;
import org.programmers.signalbuddyfinal.domain.crossroad.dto.CrossroadStateResponse;

@Mapper
public interface CrossroadMapper {

    CrossroadMapper INSTANCE = Mappers.getMapper(CrossroadMapper.class);

    @Mapping(source = "crossroadId", target = "crossroadId")
    CrossroadStateResponse toResponse(
        CrossroadStateApiResponse apiResponse, Long crossroadId
    );
}
