package org.programmers.signalbuddyfinal.domain.postit.service;


import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.programmers.signalbuddyfinal.domain.member.entity.Member;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberRole;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberStatus;
import org.programmers.signalbuddyfinal.domain.member.repository.MemberRepository;
import org.programmers.signalbuddyfinal.domain.postit.dto.PostItRequest;
import org.programmers.signalbuddyfinal.domain.postit.dto.PostItResponse;
import org.programmers.signalbuddyfinal.domain.postit.entity.Danger;
import org.programmers.signalbuddyfinal.domain.postit.entity.Postit;
import org.programmers.signalbuddyfinal.domain.postit.repository.PostItRepository;
import org.programmers.signalbuddyfinal.global.exception.BusinessException;
import org.programmers.signalbuddyfinal.global.support.ServiceTest;
import org.springframework.beans.factory.annotation.Autowired;

public class PostItServiceTest extends ServiceTest {

    @Autowired
    private PostItRepository postItRepository;
    @Autowired
    private PostItService postItService;
    @Autowired
    private MemberRepository memberRepository;

    private static final GeometryFactory geometryFactory = new GeometryFactory();
    private Member member;

    @BeforeEach
    protected void setUp() throws Exception {
        member = createMember();
    }

    @Test
    @DisplayName("포스트잇 등록 테스트")
    public void createPostItTest() {
        PostItRequest request = createPostItRequest("제목", "내용", 1L,
            LocalDateTime.of(25, 1, 1, 0, 0, 0));
        PostItResponse response = postItService.createPostIt(request);

        assertThat(response.getContent()).isEqualTo(request.getContent());
        assertThat(response.getSubject()).isEqualTo(request.getSubject());
        assertThat(response.getMemberId()).isEqualTo(request.getMemberId());

    }

    @Test
    @DisplayName("비회원 포스트잇 등록 테스트")
    public void nonMemberCreatePostItTest() {
        PostItRequest request = createPostItRequest("제목", "내용", 3L,
            LocalDateTime.of(25, 1, 1, 0, 0, 0));
        assertThrows(BusinessException.class, () -> postItService.createPostIt(request));
    }

    private PostItRequest createPostItRequest(String subject, String content, Long memberId,
        LocalDateTime createDate) {

        return PostItRequest.builder()
            .danger(Danger.NOTICE)
            .coordinate(createPoint(1, 1))
            .subject(subject)
            .content(content)
            .imageUrl("imageUrl")
            .createDate(createDate)
            .memberId(memberId)
            .build();
    }

    private Postit createPostIt(Long postItId, Danger danger, Point coordinate, String subject,
        String content, String imageURl, LocalDateTime expiryDate, LocalDateTime deleteAt,
        Member member) {
        Postit postit = Postit.builder()
            .postitId(postItId)
            .danger(danger)
            .coordinate(coordinate)
            .subject(subject)
            .content(content)
            .imageUrl(imageURl)
            .expiryDate(expiryDate)
            .deletedAt(deleteAt)
            .member(member)
            .build();
        return postItRepository.save(postit);
    }

    private Point createPoint(double latitude, double longitude) {
        return geometryFactory.createPoint(new Coordinate(longitude, latitude));
    }

    private Member createMember() {
        return memberRepository.save(Member.builder()
            .email("user1@test.com")
            .password("password1")
            .nickname("user1")
            .profileImageUrl("url")
            .role(MemberRole.USER)
            .memberStatus(MemberStatus.ACTIVITY)
            .build());
    }
}
