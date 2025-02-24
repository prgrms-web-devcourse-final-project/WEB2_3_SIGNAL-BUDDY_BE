package org.programmers.signalbuddyfinal.domain.postit.service;

import lombok.RequiredArgsConstructor;
import org.programmers.signalbuddyfinal.domain.member.entity.Member;
import org.programmers.signalbuddyfinal.domain.member.exception.MemberErrorCode;
import org.programmers.signalbuddyfinal.domain.member.repository.MemberRepository;
import org.programmers.signalbuddyfinal.domain.postit.dto.PostItRequest;
import org.programmers.signalbuddyfinal.domain.postit.dto.PostItResponse;
import org.programmers.signalbuddyfinal.domain.postit.entity.Postit;
import org.programmers.signalbuddyfinal.domain.postit.mapper.PostItMapper;
import org.programmers.signalbuddyfinal.domain.postit.repository.PostItRepository;
import org.programmers.signalbuddyfinal.global.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class PostItService {

    private final PostItRepository postItRepository;
    private final MemberRepository memberRepository;

    public PostItResponse createPostIt(PostItRequest postItRequest) {

        Member member = memberRepository.findById(postItRequest.getMemberId())
            .orElseThrow(() -> new BusinessException(
                MemberErrorCode.NOT_FOUND_MEMBER));

        Postit postit = Postit.creator()
            .danger(postItRequest.getDanger())
            .subject(postItRequest.getSubject())
            .coordinate(postItRequest.getCoordinate())
            .content(postItRequest.getContent())
            .imageUrl(postItRequest.getImageUrl())
            .expiryDate(postItRequest.getCreateDate().plusDays(7))
            .member(member)
            .build();
        postItRepository.save(postit);

        return PostItMapper.INSTANCE.toResponse(postit);
    }
}
