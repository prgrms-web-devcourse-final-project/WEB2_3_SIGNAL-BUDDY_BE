package org.programmers.signalbuddyfinal.domain.member.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.programmers.signalbuddyfinal.domain.member.dto.MemberJoinRequest;
import org.programmers.signalbuddyfinal.domain.member.dto.MemberResponse;
import org.programmers.signalbuddyfinal.domain.member.dto.MemberUpdateRequest;
import org.programmers.signalbuddyfinal.domain.member.entity.Member;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberRole;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberStatus;
import org.programmers.signalbuddyfinal.domain.member.exception.MemberErrorCode;
import org.programmers.signalbuddyfinal.domain.member.mapper.MemberMapper;
import org.programmers.signalbuddyfinal.domain.member.repository.MemberRepository;
import org.programmers.signalbuddyfinal.domain.social.entity.SocialProvider;
import org.programmers.signalbuddyfinal.domain.social.repository.SocialProviderRepository;
import org.programmers.signalbuddyfinal.global.exception.BusinessException;
import org.programmers.signalbuddyfinal.global.service.AwsFileService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final AwsFileService awsFileService;
    private final SocialProviderRepository socialProviderRepository;
    private BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

    @Value("${default.profile.image.path}")
    private String defaultProfileImage;

    @Value("${cloud.aws.s3.folder.member}")
    private String memberDir;


    public MemberResponse getMember(Long id) {
        final Member member = memberRepository.findById(id)
            .orElseThrow(() -> new BusinessException(MemberErrorCode.NOT_FOUND_MEMBER));

        if (member.getMemberStatus() == MemberStatus.WITHDRAWAL) {
            throw new BusinessException(MemberErrorCode.WITHDRAWN_MEMBER);
        }

        return MemberMapper.INSTANCE.toDto(member);
    }

    @Transactional
    public MemberResponse updateMember(Long id, MemberUpdateRequest memberUpdateRequest) {
        final Member member = findMemberById(id);
        if (!member.getEmail().equalsIgnoreCase(memberUpdateRequest.getEmail())
            && memberRepository.existsByEmail(memberUpdateRequest.getEmail())) {
            throw new BusinessException(MemberErrorCode.ALREADY_EXIST_EMAIL);
        } else if (!member.getNickname().equalsIgnoreCase(memberUpdateRequest.getNickname())
                   && memberRepository.existsByNickname(memberUpdateRequest.getNickname())) {
            throw new BusinessException(MemberErrorCode.ALREADY_EXIST_NICKNAME);
        }

        final String encodedPassword = encodedPassword(memberUpdateRequest.getPassword());

        member.updateMember(memberUpdateRequest, encodedPassword);
        log.info("Member updated: {}", member);
        return MemberMapper.INSTANCE.toDto(member);
    }

    @Transactional
    public String saveProfileImage(Long id, MultipartFile file) {
        final Member member = findMemberById(id);
        final String profileImage = saveProfileImageIfPresent(file);
        final String imageUrl = awsFileService.getFileFromS3(profileImage, memberDir).toString();
        member.saveProfileImage(imageUrl);
        return imageUrl;
    }

    @Transactional
    public void deleteMember(Long id) {
        final Member member = findMemberById(id);
        member.softDelete();
        log.info("Member deleted: {}", member);
    }

    @Transactional
    public MemberResponse joinMember(MemberJoinRequest memberJoinRequest, MultipartFile image) {

        if (memberRepository.existsByEmail(memberJoinRequest.getEmail())) {
            throw new BusinessException(MemberErrorCode.ALREADY_EXIST_EMAIL);
        } else if (memberRepository.existsByNickname(memberJoinRequest.getNickname())) {
            throw new BusinessException(MemberErrorCode.ALREADY_EXIST_NICKNAME);
        }

        String profilePath = saveProfileImageIfPresent(image);
        String profileImageUrl = null;
        if (profilePath != null) {
            profileImageUrl = awsFileService.getFileFromS3(profilePath, memberDir).toString();
        } else {
            // 프로필 이미지를 저장하지 않았을 경우 기본 이미지
            profileImageUrl = awsFileService.getFileFromS3(defaultProfileImage, memberDir)
                .toString();
        }

        Member joinMember = Member.builder().email(memberJoinRequest.getEmail())
            .nickname(memberJoinRequest.getNickname())
            .password(bCryptPasswordEncoder.encode(memberJoinRequest.getPassword()))
            .profileImageUrl(profileImageUrl).memberStatus(MemberStatus.ACTIVITY)
            .role(MemberRole.USER).build();

        memberRepository.save(joinMember);

        if (memberJoinRequest.getProvider() != null
            && memberJoinRequest.getSocialUserId() != null) {
            socialProviderRepository.existsByOauthProviderAndSocialId(memberJoinRequest.getProvider(), memberJoinRequest.getSocialUserId());
            socialProviderRepository.save(SocialProvider.builder()
                .member(joinMember)
                .oauthProvider(memberJoinRequest.getProvider())
                .socialId(memberJoinRequest.getSocialUserId())
                .build());
        }

        return MemberMapper.INSTANCE.toDto(joinMember);
    }

    public boolean verifyPassword(String password, Long id) {
        final Member member = findMemberById(id);

        return bCryptPasswordEncoder.matches(password, member.getPassword());
    }

    private Member findMemberById(Long id) {
        return memberRepository.findById(id)
            .orElseThrow(() -> new BusinessException(MemberErrorCode.NOT_FOUND_MEMBER));
    }

    private String saveProfileImageIfPresent(MultipartFile imageFile) {
        if (imageFile == null) {
            return null;
        }
        return awsFileService.uploadFileToS3(imageFile, memberDir);
    }

    private String encodedPassword(String password) {
        if (password == null) {
            return null;
        }
        return bCryptPasswordEncoder.encode(password);
    }
}
