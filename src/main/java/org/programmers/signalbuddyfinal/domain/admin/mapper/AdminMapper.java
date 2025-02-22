package org.programmers.signalbuddyfinal.domain.admin.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;
import org.programmers.signalbuddyfinal.domain.admin.dto.AdminMemberDetailResponse;
import org.programmers.signalbuddyfinal.domain.bookmark.dto.AdminBookmarkResponse;
import org.programmers.signalbuddyfinal.domain.member.entity.Member;

import java.util.List;

@Mapper
public interface AdminMapper {

    AdminMapper INSTANCE = Mappers.getMapper(AdminMapper.class);

    @Mapping(target = "userAddress", ignore = true)
    @Mapping(target = "bookmarkCount", expression = "java(adminBookmarkResponses.size())")
    @Mapping(target = "bookmarkResponses", source = "adminBookmarkResponses")
    AdminMemberDetailResponse toAdminMemberResponse(Member member,
        List<AdminBookmarkResponse> adminBookmarkResponses);

//    @AfterMapping
//    default void setUserAddress(
//        @MappingTarget AdminMemberDetailResponse.AdminMemberResponseBuilder builder,
//        List<AdminBookmarkResponse> adminBookmarkResponses) {
//        if (adminBookmarkResponses.isEmpty()) {
//            builder.userAddress("");
//        } else {
//            builder.userAddress(adminBookmarkResponses.get(0).getAddress());
//        }
//    }
}
