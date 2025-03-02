package org.programmers.signalbuddyfinal.domain.member.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.programmers.signalbuddyfinal.domain.auth.entity.Purpose;
import org.programmers.signalbuddyfinal.domain.auth.exception.AuthErrorCode;
import org.programmers.signalbuddyfinal.domain.member.dto.MemberJoinRequest;
import org.programmers.signalbuddyfinal.domain.member.dto.MemberResponse;
import org.programmers.signalbuddyfinal.domain.member.dto.MemberUpdateRequest;
import org.programmers.signalbuddyfinal.domain.member.dto.ResetPasswordRequest;
import org.programmers.signalbuddyfinal.domain.member.entity.Member;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberRole;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberStatus;
import org.programmers.signalbuddyfinal.domain.member.exception.MemberErrorCode;
import org.programmers.signalbuddyfinal.domain.member.mapper.MemberMapper;
import org.programmers.signalbuddyfinal.domain.member.repository.MemberRepository;
import org.programmers.signalbuddyfinal.global.exception.BusinessException;
import org.programmers.signalbuddyfinal.global.response.ApiResponse;
import org.programmers.signalbuddyfinal.global.service.AwsFileService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {

    private final RedisTemplate<String, String> redisTemplate;
    private final MemberRepository memberRepository;
    private final AwsFileService awsFileService;
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
            .password(encodedPassword(memberJoinRequest.getPassword()))
            .profileImageUrl(profileImageUrl).memberStatus(MemberStatus.ACTIVITY)
            .role(MemberRole.USER).build();

        memberRepository.save(joinMember);
        return MemberMapper.INSTANCE.toDto(joinMember);
    }

    public boolean verifyPassword(String password, Long id) {
        final Member member = findMemberById(id);

        return bCryptPasswordEncoder.matches(password, member.getPassword());
    }

    @Transactional
    public ResponseEntity<ApiResponse<Object>> resetPassword(
        ResetPasswordRequest resetPasswordRequest) {

        // 서비스에 등록된 이메일인지 확인
        Member member = memberRepository.findByEmail(resetPasswordRequest.getEmail())
            .orElseThrow(() -> new BusinessException(MemberErrorCode.NOT_FOUND_MEMBER));

        // 이메일 본인 인증을 완료한 사용자인지 확인
        if (Boolean.FALSE.equals(redisTemplate.hasKey(
            "auth:email:" + Purpose.NEW_PASSWORD.toString().toLowerCase() + ":"
                + member.getEmail()))) {
            throw new BusinessException(AuthErrorCode.EMAIL_VERIFICATION_REQUIRED);
        }

        MemberUpdateRequest onlyUpdatePassword = MemberUpdateRequest.builder().
            password(resetPasswordRequest.getNewPassword()).
            build();

        member.updateMember(onlyUpdatePassword,
            encodedPassword(resetPasswordRequest.getNewPassword()));

        // 이메일 본인 인증 데이터 삭제
        redisTemplate.delete("auth:email:" + Purpose.NEW_PASSWORD.toString().toLowerCase() + ":"
            + member.getEmail());

        return ResponseEntity.ok(ApiResponse.createSuccessWithNoData());
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
