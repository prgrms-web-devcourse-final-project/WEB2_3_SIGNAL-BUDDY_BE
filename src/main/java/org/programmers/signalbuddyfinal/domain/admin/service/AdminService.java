package org.programmers.signalbuddyfinal.domain.admin.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.programmers.signalbuddyfinal.domain.admin.dto.AdminJoinRequest;
import org.programmers.signalbuddyfinal.domain.member.dto.MemberResponse;
import org.programmers.signalbuddyfinal.domain.member.entity.Member;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberRole;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberStatus;
import org.programmers.signalbuddyfinal.domain.member.exception.MemberErrorCode;
import org.programmers.signalbuddyfinal.domain.member.mapper.MemberMapper;
import org.programmers.signalbuddyfinal.domain.member.repository.MemberRepository;
import org.programmers.signalbuddyfinal.domain.member.service.MemberService;
import org.programmers.signalbuddyfinal.global.exception.BusinessException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final MemberRepository memberRepository;
    private final MemberService memberService;
    private BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

    @Transactional
    public MemberResponse joinAdminMember(AdminJoinRequest adminJoinRequest, MultipartFile image) {

        String profileImageUrl = memberService.settingProfileImage(image);

        // 이미 존재하는 사용자인지 확인
        if (memberRepository.existsByEmail(adminJoinRequest.getEmail())) {
            throw new BusinessException(MemberErrorCode.ALREADY_EXIST_EMAIL);
        }

        Member joinAdminMember = Member.builder()
            .email(adminJoinRequest.getEmail())
            .password(bCryptPasswordEncoder.encode(adminJoinRequest.getPassword()))
            .nickname("관리자_"+adminJoinRequest.getNickname())
            .profileImageUrl(profileImageUrl)
            .memberStatus(MemberStatus.ACTIVITY)
            .role(MemberRole.ADMIN)
            .build();

        memberRepository.save(joinAdminMember);
        return MemberMapper.INSTANCE.toDto(joinAdminMember);
    }
}
