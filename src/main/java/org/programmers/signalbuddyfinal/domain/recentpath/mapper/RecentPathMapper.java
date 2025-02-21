package org.programmers.signalbuddyfinal.domain.recentpath.mapper;

import org.locationtech.jts.geom.Point;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.programmers.signalbuddyfinal.domain.member.entity.Member;
import org.programmers.signalbuddyfinal.domain.recentpath.dto.RecentPathRequest;
import org.programmers.signalbuddyfinal.domain.recentpath.dto.RecentPathResponse;
import org.programmers.signalbuddyfinal.domain.recentpath.entity.RecentPath;

@Mapper
public interface RecentPathMapper {

    RecentPathMapper INSTANCE = Mappers.getMapper(RecentPathMapper.class);

    @Mapping(source = "point", target = "endPoint")
    RecentPath toEntity(RecentPathRequest request, Point point, Member member);

    @Mapping(target = "lng", expression = "java(getLng(recentPath.getEndPoint()))")
    @Mapping(target = "lat", expression = "java(getLat(recentPath.getEndPoint()))")
    @Mapping(target = "isBookmarked", expression = "java(isBookmarked(recentPath))")
    RecentPathResponse toDto(RecentPath recentPath);

    default double getLng(Point point) {
        return point.getX();
    }

    default double getLat(Point point) {
        return point.getY();
    }

    default boolean isBookmarked(RecentPath recentPath) {
        return recentPath.getBookmark() != null; // Bookmark가 존재하면 true
    }
}
