package org.programmers.signalbuddyfinal.domain.admin.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.programmers.signalbuddyfinal.domain.admin.dto.AdminJoinRequest;
import org.programmers.signalbuddyfinal.domain.admin.dto.AdminMemberDetailResponse;
import org.programmers.signalbuddyfinal.domain.admin.dto.AdminMemberResponse;
import org.programmers.signalbuddyfinal.domain.admin.dto.MemberFilterRequest;
import org.programmers.signalbuddyfinal.domain.admin.dto.WithdrawalMemberResponse;
import org.programmers.signalbuddyfinal.domain.admin.exception.AdminErrorCode;
import org.programmers.signalbuddyfinal.domain.admin.mapper.AdminMapper;
import org.programmers.signalbuddyfinal.domain.bookmark.dto.AdminBookmarkResponse;
import org.programmers.signalbuddyfinal.domain.bookmark.repository.BookmarkRepository;
import org.programmers.signalbuddyfinal.domain.member.dto.MemberResponse;
import org.programmers.signalbuddyfinal.domain.member.entity.Member;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberRole;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberStatus;
import org.programmers.signalbuddyfinal.domain.member.exception.MemberErrorCode;
import org.programmers.signalbuddyfinal.domain.member.mapper.MemberMapper;
import org.programmers.signalbuddyfinal.domain.member.repository.MemberRepository;
import org.programmers.signalbuddyfinal.global.exception.BusinessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final MemberRepository memberRepository;
    private final BookmarkRepository bookmarkRepository;
    private BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

    public Page<AdminMemberDetailResponse> getAllMembers(Pageable pageable) {
        Page<Member> membersPage = memberRepository.findAllMembers(pageable);

        Page<AdminMemberDetailResponse> adminMemberResponses = membersPage.map(member -> {

            List<AdminBookmarkResponse> adminBookmarkResponses = bookmarkRepository.findBookmarkByMember(
                member.getMemberId());
            AdminMemberDetailResponse response = AdminMapper.INSTANCE.toAdminMemberResponse(member,
                adminBookmarkResponses);

            return response;
        });

        return adminMemberResponses;
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

    // 회원 필터링
    public Page<AdminMemberResponse> getAllMemberWithFilter(Pageable pageable, MemberFilterRequest memberFilterRequest) {

        if((memberFilterRequest.getStartDate() != null && memberFilterRequest.getEndDate() != null)&& memberFilterRequest.getAgo() !=null){
            throw new BusinessException(AdminErrorCode.DUPLICATED_DATE);
        }
        Page<AdminMemberResponse> members = memberRepository.findAllMemberWithFilter(pageable,
            memberFilterRequest);

        return members;
    }

    // 회원 검색
    public Page<AdminMemberResponse> searchMember (Pageable pageable,String content){
        return memberRepository.findMemberByEmailOrNickname(pageable,content);
    }

    @Transactional
    public MemberResponse joinAdminMember(AdminJoinRequest adminJoinRequest) {

        // 이미 존재하는 사용자인지 확인
        if (memberRepository.existsByEmail(adminJoinRequest.getEmail())) {
            throw new BusinessException(MemberErrorCode.ALREADY_EXIST_EMAIL);
        }

        Member joinMember = Member.builder()
            .email(adminJoinRequest.getEmail())
            .password(bCryptPasswordEncoder.encode(adminJoinRequest.getPassword()))
            .nickname("관리자")
            .memberStatus(MemberStatus.ACTIVITY)
            .role(MemberRole.ADMIN)
            .build();

        memberRepository.save(joinMember);
        return MemberMapper.INSTANCE.toDto(joinMember);
    }
}
