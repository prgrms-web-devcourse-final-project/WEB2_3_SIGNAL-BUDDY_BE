package org.programmers.signalbuddyfinal.domain.postit.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.programmers.signalbuddyfinal.domain.admin.mapper.AdminMapper;
import org.programmers.signalbuddyfinal.domain.postit.dto.PostItResponse;
import org.programmers.signalbuddyfinal.domain.postit.entity.Postit;

@Mapper
public interface PostItMapper
{
    PostItMapper INSTANCE = Mappers.getMapper(PostItMapper.class);

    PostItResponse toResponse(Postit postit);

}
