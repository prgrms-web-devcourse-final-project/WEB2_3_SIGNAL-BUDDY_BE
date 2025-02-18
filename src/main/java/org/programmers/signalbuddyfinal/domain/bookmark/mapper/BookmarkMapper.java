package org.programmers.signalbuddyfinal.domain.bookmark.mapper;

import org.locationtech.jts.geom.Point;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.programmers.signalbuddyfinal.domain.bookmark.dto.AdminBookmarkResponse;
import org.programmers.signalbuddyfinal.domain.bookmark.dto.BookmarkRequest;
import org.programmers.signalbuddyfinal.domain.bookmark.dto.BookmarkResponse;
import org.programmers.signalbuddyfinal.domain.bookmark.entity.Bookmark;
import org.programmers.signalbuddyfinal.domain.member.entity.Member;

import java.util.List;

@Mapper
public interface BookmarkMapper {

    BookmarkMapper INSTANCE = Mappers.getMapper(BookmarkMapper.class);

    @Mapping(source = "point", target = "coordinate")
    Bookmark toEntity(BookmarkRequest bookmarkRequest, Point point, Member member);


    @Mapping(target = "lng", expression = "java(getLng(bookmark.getCoordinate()))")
    @Mapping(target = "lat", expression = "java(getLat(bookmark.getCoordinate()))")
    BookmarkResponse toDto(Bookmark bookmark);

    default double getLng(Point point) {
        return point.getX();
    }

    default double getLat(Point point) {
        return point.getY();
    }

    // 관리자
    List<AdminBookmarkResponse> toAdminDto(List<Bookmark> bookmarks);
}
