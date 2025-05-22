package org.programmers.signalbuddyfinal.domain.admin.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.programmers.signalbuddyfinal.domain.admin.dto.AdminMemberDetailResponse;
import org.programmers.signalbuddyfinal.domain.admin.dto.AdminMemberResponse;
import org.programmers.signalbuddyfinal.domain.admin.dto.MemberFilterRequest;
import org.programmers.signalbuddyfinal.domain.admin.mapper.AdminMapper;
import org.programmers.signalbuddyfinal.domain.bookmark.dto.AdminBookmarkResponse;
import org.programmers.signalbuddyfinal.domain.bookmark.repository.BookmarkRepository;
import org.programmers.signalbuddyfinal.domain.member.entity.Member;
import org.programmers.signalbuddyfinal.domain.member.repository.MemberRepository;
import org.programmers.signalbuddyfinal.global.dto.PageResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminMemberService {

    private final MemberRepository memberRepository;
    private final BookmarkRepository bookmarkRepository;
    private BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

    public PageResponse<AdminMemberResponse> getAllMembers(Pageable pageable) {
        return memberRepository.findAllMembers(pageable);
    }

    public AdminMemberDetailResponse getMember(Long id) {
        Member member = findMember(id);
        return convertorDetailResponse(member, findBookmark(member));
    }

    public PageResponse<AdminMemberResponse> getAllMembersWithFilter(Pageable pageable,
        MemberFilterRequest memberFilterRequest) {

        memberFilterRequest.validateDateRange();
        return memberRepository.findAllMemberWithFilter(pageable, memberFilterRequest);
    }

    private List<AdminBookmarkResponse> findBookmark(Member member) {
        return bookmarkRepository.findBookmarkByMember(member.getMemberId());
    }

    private Member findMember(Long id) {
        return memberRepository.findByIdOrThrow(id);
    }

    private AdminMemberDetailResponse convertorDetailResponse(Member member,
        List<AdminBookmarkResponse> bookmarkResponse) {
        return AdminMapper.INSTANCE.toAdminMemberResponse(member, bookmarkResponse);
    }

}
