package org.programmers.signalbuddyfinal.domain.postit.service;


import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.programmers.signalbuddyfinal.domain.crossroad.service.PointUtil;
import org.programmers.signalbuddyfinal.domain.member.entity.Member;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberRole;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberStatus;
import org.programmers.signalbuddyfinal.domain.member.repository.MemberRepository;
import org.programmers.signalbuddyfinal.domain.postit.dto.PostItCreateRequest;
import org.programmers.signalbuddyfinal.domain.postit.dto.PostItRequest;
import org.programmers.signalbuddyfinal.domain.postit.dto.PostItResponse;
import org.programmers.signalbuddyfinal.domain.postit.entity.Danger;
import org.programmers.signalbuddyfinal.domain.postit.entity.Postit;
import org.programmers.signalbuddyfinal.domain.postit.repository.PostItRepository;
import org.programmers.signalbuddyfinal.global.dto.CustomUser2Member;
import org.programmers.signalbuddyfinal.global.exception.BusinessException;
import org.programmers.signalbuddyfinal.global.security.basic.CustomUserDetails;
import org.programmers.signalbuddyfinal.global.support.ServiceTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;

public class PostItServiceTest extends ServiceTest {

    @Autowired
    private PostItRepository postItRepository;
    @Autowired
    private PostItService postItService;
    @Autowired
    private MemberRepository memberRepository;

    private Member member1;
    private CustomUser2Member user1;
    private CustomUser2Member user2;
    private CustomUser2Member notUser;
    MockMultipartFile mockImage1;
    MockMultipartFile mockImage2;

    @BeforeEach
    protected void setUp() throws Exception {
        member1 = createMember("user1@gmail.com", "user1");

        mockImage1 = new MockMultipartFile(
            "image",
            "test-image.jpg",
            "image/jpeg",
            "test image content".getBytes()
        );
        mockImage2 = new MockMultipartFile(
            "image2",
            "test-image2.jpg",
            "image/jpeg",
            "test image content".getBytes()
        );

        user1 = new CustomUser2Member(
            new CustomUserDetails(1L, "user1@gmamil.com", "1234",
                "url2.jpg", "user1", MemberRole.USER, MemberStatus.ACTIVITY));
        user2 = new CustomUser2Member(
            new CustomUserDetails(2L, "user2@gmamil.com", "1234",
                "url2.jpg", "user2", MemberRole.USER, MemberStatus.ACTIVITY));
        notUser = new CustomUser2Member(
            new CustomUserDetails(10L, "notUser@gmamil.com", "1234",
                "notUser.jpg", "notUser", MemberRole.USER, MemberStatus.ACTIVITY));
    }

    @Test
    @DisplayName("포스트잇 등록 성공 테스트")
    public void createPostItSuccessTest() {
        PostItCreateRequest request = createPostItCreateRequest("제목", "내용",
            LocalDateTime.of(25, 1, 1, 0, 0));

        PostItResponse response = postItService.createPostIt(request, mockImage1, user1);

        assertThat(response.getContent()).isEqualTo(request.getContent());
        assertThat(response.getSubject()).isEqualTo(request.getSubject());

    }

    @Test
    @DisplayName("비회원 포스트잇 등록시 예외 발생 테스트")
    public void nonMemberCreatePostItTest() {
        PostItCreateRequest request = createPostItCreateRequest("제목", "내용",
            LocalDateTime.of(25, 1, 1, 0, 0));

        assertThrows(BusinessException.class,
            () -> postItService.createPostIt(request, mockImage1, notUser));
    }

    @Test
    @DisplayName("포스트잇 수정 성공 테스트")
    public void updatePostItTest() {

        postItRepository.save(
            createPostIt(Danger.NOTICE, PointUtil.toPoint(1.0203, 1.3048), "제목1",
                "제목1", "img1", LocalDateTime.of(2025, 1, 1, 0, 0), member1));
        PostItRequest request = createPostItRequest("제목", "내용");

        PostItResponse response = postItService.updatePostIt(1L, request, mockImage2, user1);

        assertThat(response.getContent()).isEqualTo(request.getContent());
        assertThat(response.getSubject()).isEqualTo(request.getSubject());
    }

    @Test
    @DisplayName("포스트잇 수정시 작성자와 수정자가 다른 경우 예외 발생 테스트")
    public void otherUserUpdatePostItTest() {

        postItRepository.save(
            createPostIt(Danger.NOTICE, PointUtil.toPoint(1.0203, 1.3048), "제목1",
                "제목1", "img1", LocalDateTime.of(2025, 1, 1, 0, 0), member1));
        PostItRequest request = createPostItRequest("제목", "내용");

        assertThrows(BusinessException.class,
            () -> postItService.updatePostIt(1L, request, mockImage2, notUser));
    }

    @Test
    @DisplayName("포스트잇 삭제 성공 테스트")
    public void deletePostItSuccessTest() {
        postItRepository.save(
            createPostIt(Danger.NOTICE, PointUtil.toPoint(1.0203, 1.3048), "제목1",
                "제목1", "img1", LocalDateTime.of(2025, 1, 1, 0, 0), member1));

        postItService.deletePostIt(1L, user1);

        assertThat(postItRepository.findById(1L).get().getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("포스트잇 삭제시 작성자와 수정자가 다른 경우 예외 발생 테스트")
    public void otherUserDeletePostItTest() {

        postItRepository.save(
            createPostIt(Danger.NOTICE, PointUtil.toPoint(1.0203, 1.3048), "제목1",
                "제목1", "img1", LocalDateTime.of(2025, 1, 1, 0, 0), member1));

        assertThrows(BusinessException.class,
            () -> postItService.deletePostIt(1L, user2));
    }

    private PostItRequest createPostItRequest(String subject, String content) {

        return PostItRequest.builder()
            .danger(Danger.NOTICE)
            .lat(1.02222)
            .lng(1.09099)
            .imageUrl("https://image.com/imageUrl")
            .subject(subject)
            .content(content)
            .build();
    }

    private PostItCreateRequest createPostItCreateRequest(String subject, String content,
        LocalDateTime createDate) {

        return PostItCreateRequest.builder()
            .danger(Danger.NOTICE)
            .lat(1.02222)
            .lng(1.09099)
            .subject(subject)
            .content(content)
            .createDate(createDate)
            .build();
    }

    private Postit createPostIt(Danger danger, Point coordinate, String subject,
        String content, String imageURl, LocalDateTime expiryDate,
        Member member) {
        Postit postit = Postit.builder()
            .danger(danger)
            .coordinate(coordinate)
            .subject(subject)
            .content(content)
            .imageUrl(imageURl)
            .expiryDate(expiryDate)
            .deletedAt(null)
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
