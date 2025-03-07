package org.programmers.signalbuddyfinal.domain.feedback.mapper;

import org.locationtech.jts.geom.Point;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.programmers.signalbuddyfinal.domain.crossroad.dto.CrossroadResponse;
import org.programmers.signalbuddyfinal.domain.crossroad.entity.Crossroad;
import org.programmers.signalbuddyfinal.domain.feedback.dto.FeedbackResponse;
import org.programmers.signalbuddyfinal.domain.feedback.entity.Feedback;
import org.programmers.signalbuddyfinal.domain.member.dto.MemberResponse;
import org.programmers.signalbuddyfinal.domain.member.entity.Member;

@Mapper
public interface FeedbackMapper {

    FeedbackMapper INSTANCE = Mappers.getMapper(FeedbackMapper.class);

    @Mapping(target = "lng", expression = "java(getLng(crossroad.getCoordinate()))")
    @Mapping(target = "lat", expression = "java(getLat(crossroad.getCoordinate()))")
    CrossroadResponse toCrossroadResponse(Crossroad crossroad);

    MemberResponse toMemberResponse(Member member);

    FeedbackResponse toResponse(Feedback feedback);

    default double getLng(Point point) {
        return point.getX();
    }

    default double getLat(Point point) {
        return point.getY();
    }
}
