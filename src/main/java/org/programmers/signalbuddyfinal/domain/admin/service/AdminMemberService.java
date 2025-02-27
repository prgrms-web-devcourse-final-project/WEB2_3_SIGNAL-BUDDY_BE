package org.programmers.signalbuddyfinal.domain.admin.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.programmers.signalbuddyfinal.domain.admin.dto.AdminMemberDetailResponse;
import org.programmers.signalbuddyfinal.domain.admin.dto.AdminMemberResponse;
import org.programmers.signalbuddyfinal.domain.admin.dto.MemberFilterRequest;
import org.programmers.signalbuddyfinal.domain.admin.dto.WithdrawalMemberResponse;
import org.programmers.signalbuddyfinal.domain.admin.exception.AdminErrorCode;
import org.programmers.signalbuddyfinal.domain.admin.mapper.AdminMapper;
import org.programmers.signalbuddyfinal.domain.bookmark.dto.AdminBookmarkResponse;
import org.programmers.signalbuddyfinal.domain.bookmark.repository.BookmarkRepository;
import org.programmers.signalbuddyfinal.domain.member.entity.Member;
import org.programmers.signalbuddyfinal.domain.member.exception.MemberErrorCode;
import org.programmers.signalbuddyfinal.domain.member.repository.MemberRepository;
import org.programmers.signalbuddyfinal.global.dto.PageResponse;
import org.programmers.signalbuddyfinal.global.exception.BusinessException;
import org.springframework.data.domain.Page;
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
        PageResponse<AdminMemberResponse> membersPage = memberRepository.findAllMembers(pageable);

        return membersPage;
    }

    public AdminMemberDetailResponse getMember(Long id) {
        Member member = memberRepository.findById(id).orElseThrow(() -> new BusinessException(
            MemberErrorCode.NOT_FOUND_MEMBER));

        List<AdminBookmarkResponse> adminBookmarkResponses = bookmarkRepository.findBookmarkByMember(
            member.getMemberId());

        AdminMemberDetailResponse response = AdminMapper.INSTANCE.toAdminMemberResponse(member,
            adminBookmarkResponses);

        return response;
    }

    public Page<WithdrawalMemberResponse> getAllWithdrawalMembers(Pageable pageable) {
        Page<WithdrawalMemberResponse> membersPage = memberRepository.findAllWithdrawMembers(pageable);

        return membersPage;
    }

    public PageResponse<AdminMemberResponse> getAllMemberWithFilter(Pageable pageable,
        MemberFilterRequest memberFilterRequest) {

        checkFilterException(memberFilterRequest);

        PageResponse<AdminMemberResponse> members = memberRepository.findAllMemberWithFilter(
            pageable,
            memberFilterRequest);

        return members;
    }

    private void checkFilterException(MemberFilterRequest memberFilterRequest) {
        if ((memberFilterRequest.getStartDate() != null && memberFilterRequest.getEndDate() != null)
            && memberFilterRequest.getPeriods() != null) {
            throw new BusinessException(AdminErrorCode.DUPLICATED_PERIOD);
        }

        if (memberFilterRequest.getStartDate() != null
            && memberFilterRequest.getEndDate() == null) {
            throw new BusinessException(AdminErrorCode.END_DATE_NOT_SELECTED);
        }

        if (memberFilterRequest.getStartDate() == null
            && memberFilterRequest.getEndDate() != null) {
            throw new BusinessException(AdminErrorCode.START_DATE_NOT_SELECTED);
        }

        if (memberFilterRequest.getStartDate().isAfter(memberFilterRequest.getEndDate())) {
            throw new BusinessException(AdminErrorCode.START_DATE_AFTER_END_DATE);
        }
    }

}
