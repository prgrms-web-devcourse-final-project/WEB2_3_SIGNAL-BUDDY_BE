package org.programmers.signalbuddyfinal.domain.member.mapper;

import javax.annotation.processing.Generated;
import org.programmers.signalbuddyfinal.domain.member.dto.MemberResponse;
import org.programmers.signalbuddyfinal.domain.member.entity.Member;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-02-18T13:18:43+0900",
    comments = "version: 1.6.3, compiler: IncrementalProcessingEnvironment from gradle-language-java-8.12.1.jar, environment: Java 17.0.12 (Azul Systems, Inc.)"
)
public class MemberMapperImpl implements MemberMapper {

    @Override
    public MemberResponse toDto(Member member) {
        if ( member == null ) {
            return null;
        }

        MemberResponse.MemberResponseBuilder memberResponse = MemberResponse.builder();

        memberResponse.memberId( member.getMemberId() );
        memberResponse.email( member.getEmail() );
        memberResponse.nickname( member.getNickname() );
        memberResponse.profileImageUrl( member.getProfileImageUrl() );
        memberResponse.role( member.getRole() );
        memberResponse.memberStatus( member.getMemberStatus() );

        return memberResponse.build();
    }
}
