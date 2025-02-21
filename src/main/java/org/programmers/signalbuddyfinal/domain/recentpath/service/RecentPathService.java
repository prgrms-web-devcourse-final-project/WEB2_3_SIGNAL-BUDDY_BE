package org.programmers.signalbuddyfinal.domain.recentpath.service;

import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.programmers.signalbuddyfinal.domain.member.entity.Member;
import org.programmers.signalbuddyfinal.domain.member.exception.MemberErrorCode;
import org.programmers.signalbuddyfinal.domain.member.repository.MemberRepository;
import org.programmers.signalbuddyfinal.domain.recentpath.dto.RecentPathRequest;
import org.programmers.signalbuddyfinal.domain.recentpath.dto.RecentPathResponse;
import org.programmers.signalbuddyfinal.domain.recentpath.entity.RecentPath;
import org.programmers.signalbuddyfinal.domain.recentpath.exception.RecentPathErrorCode;
import org.programmers.signalbuddyfinal.domain.recentpath.mapper.RecentPathMapper;
import org.programmers.signalbuddyfinal.domain.recentpath.repository.RecentPathRepository;
import org.programmers.signalbuddyfinal.global.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RecentPathService {

    private final RecentPathRepository recentPathRepository;
    private final MemberRepository memberRepository;
    private final GeometryFactory geometryFactory;

    @Transactional
    public RecentPathResponse saveRecentPath(Long memberId, RecentPathRequest request) {
        final Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new BusinessException(MemberErrorCode.NOT_FOUND_MEMBER));

        final Point point = toPoint(request.getLng(), request.getLat());
        final RecentPath recentPath = RecentPathMapper.INSTANCE.toEntity(request, point, member);
        final RecentPath save = recentPathRepository.save(recentPath);
        return RecentPathMapper.INSTANCE.toDto(save);
    }

    private Point toPoint(double lng, double lat) {
        if (lng < -180 || lng > 180 || lat < -90 || lat > 90) {
            throw new BusinessException(RecentPathErrorCode.INVALID_COORDINATES);
        }
        return geometryFactory.createPoint(new Coordinate(lng, lat));
    }
}
