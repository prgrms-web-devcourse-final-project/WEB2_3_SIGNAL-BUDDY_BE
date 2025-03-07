package org.programmers.signalbuddyfinal.domain.postit.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.programmers.signalbuddyfinal.domain.postit.dto.PostItResponse;
import org.programmers.signalbuddyfinal.domain.postit.entity.Postit;

@Mapper
public interface PostItMapper {

    PostItMapper INSTANCE = Mappers.getMapper(PostItMapper.class);

    @Mapping(target = "createDate", source = "createdAt")
    @Mapping(target = "memberId", source = "member.memberId")
    @Mapping(target = "content", source = "content")
    @Mapping(target = "lat", expression = "java(postit.getCoordinate() != null ? postit.getCoordinate().getY() : null)")
    @Mapping(target = "lng", expression = "java(postit.getCoordinate() != null ? postit.getCoordinate().getX() : null)")
    PostItResponse toResponse(Postit postit);

}

