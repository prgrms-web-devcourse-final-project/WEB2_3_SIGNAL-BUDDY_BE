package org.programmers.signalbuddyfinal.domain.feedback.dto;

import javax.annotation.processing.Generated;
import org.programmers.signalbuddyfinal.domain.feedback.entity.Feedback;
import org.programmers.signalbuddyfinal.domain.member.dto.MemberResponse;
import org.programmers.signalbuddyfinal.domain.member.entity.Member;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-02-18T12:47:10+0900",
    comments = "version: 1.6.3, compiler: IncrementalProcessingEnvironment from gradle-language-java-8.12.1.jar, environment: Java 17.0.12 (Azul Systems, Inc.)"
)
public class FeedbackMapperImpl implements FeedbackMapper {

    @Override
    public MemberResponse toMemberResponse(Member member) {
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

    @Override
    public FeedbackResponse toResponse(Feedback feedback) {
        if ( feedback == null ) {
            return null;
        }

        FeedbackResponse.FeedbackResponseBuilder feedbackResponse = FeedbackResponse.builder();

        feedbackResponse.feedbackId( feedback.getFeedbackId() );
        feedbackResponse.subject( feedback.getSubject() );
        feedbackResponse.content( feedback.getContent() );
        feedbackResponse.likeCount( feedback.getLikeCount() );
        feedbackResponse.answerStatus( feedback.getAnswerStatus() );
        feedbackResponse.createdAt( feedback.getCreatedAt() );
        feedbackResponse.updatedAt( feedback.getUpdatedAt() );
        feedbackResponse.member( toMemberResponse( feedback.getMember() ) );

        return feedbackResponse.build();
    }
}
