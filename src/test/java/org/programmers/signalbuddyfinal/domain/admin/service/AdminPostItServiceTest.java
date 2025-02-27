package org.programmers.signalbuddyfinal.domain.admin.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Point;
import org.programmers.signalbuddyfinal.domain.crossroad.service.PointUtil;
import org.programmers.signalbuddyfinal.domain.member.entity.Member;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberRole;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberStatus;
import org.programmers.signalbuddyfinal.domain.member.repository.MemberRepository;
import org.programmers.signalbuddyfinal.domain.postit.entity.Danger;
import org.programmers.signalbuddyfinal.domain.postit.entity.Postit;
import org.programmers.signalbuddyfinal.domain.postit.repository.PostItRepository;
import org.programmers.signalbuddyfinal.global.support.ServiceTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public class AdminPostItServiceTest extends ServiceTest {

    List<Member> member;
    Pageable pageable;

    @Autowired
    PostItRepository postItRepository;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    AdminPostItService adminPostItService;

    @BeforeEach
    public void setUp() {
        member = new ArrayList<>();

        for (int i = 0; i < 20; i++) {
            member.add(createMember("user" + i + "@gmail.com", "user" + i));
            createPostIt(Danger.WARNING, PointUtil.toPoint(1.0203, 1.3048), "제목1", "내용1",
                "https://image1.com/imageUrl",
                LocalDateTime.of(2025, 1, 2, 1, 30), LocalDateTime.of(2025, 1, 8, 1, 30),
                member.get(i));
        }
        pageable = PageRequest.of(0, 10);
    }

    @DisplayName("포스트잇 전체 조회 성공 테스트")
    @Test
    public void getAllPostItSuccessTest() {

        assertThat(adminPostItService.getAllPostIt(pageable).getTotalElements()).isEqualTo(20);
        assertThat(adminPostItService.getAllPostIt(pageable).getPageSize()).isEqualTo(10);
    }

    private Postit createPostIt(Danger danger, Point coordinate, String subject,
        String content, String imageURl, LocalDateTime expiryDate, LocalDateTime deleteAt,
        Member member) {

        Postit postit = Postit.builder()
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

    private Member createMember(String email, String nickName) {
        return memberRepository.save(Member.builder()
            .email(email)
            .password("password1")
            .nickname(nickName)
            .profileImageUrl("url")
            .role(MemberRole.USER)
            .memberStatus(MemberStatus.ACTIVITY)
            .build());
    }

}
