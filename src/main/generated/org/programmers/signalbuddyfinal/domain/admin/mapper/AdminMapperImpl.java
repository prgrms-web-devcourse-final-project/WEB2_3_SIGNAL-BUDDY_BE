package org.programmers.signalbuddyfinal.domain.admin.mapper;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.programmers.signalbuddyfinal.domain.admin.dto.AdminMemberResponse;
import org.programmers.signalbuddyfinal.domain.bookmark.dto.AdminBookmarkResponse;
import org.programmers.signalbuddyfinal.domain.member.entity.Member;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-02-18T12:47:10+0900",
    comments = "version: 1.6.3, compiler: IncrementalProcessingEnvironment from gradle-language-java-8.12.1.jar, environment: Java 17.0.12 (Azul Systems, Inc.)"
)
public class AdminMapperImpl implements AdminMapper {

    @Override
    public AdminMemberResponse toAdminMemberResponse(Member member, List<AdminBookmarkResponse> adminBookmarkResponses) {
        if ( member == null && adminBookmarkResponses == null ) {
            return null;
        }

        AdminMemberResponse.AdminMemberResponseBuilder adminMemberResponse = AdminMemberResponse.builder();

        if ( member != null ) {
            adminMemberResponse.memberId( member.getMemberId() );
            adminMemberResponse.email( member.getEmail() );
            adminMemberResponse.nickname( member.getNickname() );
            adminMemberResponse.profileImageUrl( member.getProfileImageUrl() );
            adminMemberResponse.role( member.getRole() );
            adminMemberResponse.memberStatus( member.getMemberStatus() );
            adminMemberResponse.createdAt( member.getCreatedAt() );
            adminMemberResponse.updatedAt( member.getUpdatedAt() );
        }
        List<AdminBookmarkResponse> list = adminBookmarkResponses;
        if ( list != null ) {
            adminMemberResponse.bookmarkResponses( new ArrayList<AdminBookmarkResponse>( list ) );
        }
        adminMemberResponse.bookmarkCount( adminBookmarkResponses.size() );

        setUserAddress( adminMemberResponse, adminBookmarkResponses );

        return adminMemberResponse.build();
    }
}
