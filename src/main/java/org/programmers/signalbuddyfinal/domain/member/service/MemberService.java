package org.programmers.signalbuddyfinal.domain.member.service;

import java.util.Optional;
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
import org.programmers.signalbuddyfinal.domain.social.entity.SocialProvider;
import org.programmers.signalbuddyfinal.domain.social.repository.SocialProviderRepository;
import org.programmers.signalbuddyfinal.global.exception.BusinessException;
import org.programmers.signalbuddyfinal.global.exception.GlobalErrorCode;
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

        // 프로필 이미지 설정
        String profileImageUrl = settingProfileImage(image);

        // 소셜 회원가입
        if(memberJoinRequest.getProvider()!=null && memberJoinRequest.getSocialUserId()!=null){
            // 이미 소셜 회원가입을 한 경우
            if(socialProviderRepository.existsByOauthProviderAndSocialId(memberJoinRequest.getProvider(), memberJoinRequest.getSocialUserId())){
                throw new BusinessException(MemberErrorCode.ALREADY_EXIST_SOCIAL_ACCOUNT);
            }
            return MemberMapper.INSTANCE.toDto(saveMember(memberJoinRequest, profileImageUrl, "social"));
        }
        // 기본 회원가입
        else{
            if(memberJoinRequest.getEmail() == null) throw new BusinessException(MemberErrorCode.REQUIRED_EMAIL_DATA);
            return MemberMapper.INSTANCE.toDto(saveMember(memberJoinRequest, profileImageUrl, "basic"));
        }
    }

    // 사용자 정보 저장
    private Member saveMember(MemberJoinRequest memberJoinRequest, String profileImageUrl,
        String type) {

        if (memberRepository.existsByNickname(memberJoinRequest.getNickname())) {
            throw new BusinessException(MemberErrorCode.ALREADY_EXIST_NICKNAME);
        }

        Optional<Member> existingMember = memberRepository.findByEmail(
            memberJoinRequest.getEmail());

        if (existingMember.isPresent()) {
            if (type.equals("social")) {
                return linkWithAlreadyMember(existingMember.get(), memberJoinRequest);
            }
            throw new BusinessException(MemberErrorCode.ALREADY_EXIST_EMAIL);
        }

        Member savedMember = memberRepository.save(Member.builder()
            .email(memberJoinRequest.getEmail())
            .password(encodedPassword(memberJoinRequest.getPassword()))
            .nickname(memberJoinRequest.getNickname())
            .profileImageUrl(profileImageUrl)
            .role(MemberRole.USER)
            .memberStatus(MemberStatus.ACTIVITY)
            .build());

        return type.equals("social") ? linkWithAlreadyMember(savedMember, memberJoinRequest)
            : savedMember;

    }

    // 사용자 정보와 연동
    private Member linkWithAlreadyMember(Member member, MemberJoinRequest memberJoinRequest) {
        socialProviderRepository.save(SocialProvider.builder()
                .member(member)
                .oauthProvider(memberJoinRequest.getProvider())
                .socialId(memberJoinRequest.getSocialUserId())
            .build());

        return member;
    }

    public boolean verifyPassword(String password, Long id) {
        final Member member = findMemberById(id);

        return bCryptPasswordEncoder.matches(password, member.getPassword());
    }

    @Transactional
    public ResponseEntity<ApiResponse<Object>> resetPassword(
        ResetPasswordRequest resetPasswordRequest) {

        Member member = validateEmailAndEmailAuthentication(Purpose.NEW_PASSWORD, resetPasswordRequest.getEmail());

        MemberUpdateRequest onlyUpdatePassword = MemberUpdateRequest.builder().
            password(resetPasswordRequest.getNewPassword()).
            build();

        member.updateMember(onlyUpdatePassword,
            encodedPassword(resetPasswordRequest.getNewPassword()));

        deleteEmailAuthenticationData(Purpose.NEW_PASSWORD, resetPasswordRequest.getEmail());

        return ResponseEntity.ok(ApiResponse.createSuccessWithNoData());
    }

    // 프로필 이미지 설정
    private String settingProfileImage(MultipartFile image){

        String profilePath = saveProfileImageIfPresent(image);

        if (profilePath != null) {
            return awsFileService.getFileFromS3(profilePath, memberDir).toString();
        } else {
            // 프로필 이미지를 저장하지 않았을 경우 기본 이미지
            return awsFileService.getFileFromS3(defaultProfileImage, memberDir)
                .toString();
        }
    }

    // 서비스에 등록된 이메일인지 확인 및 이메일 인증 여부 확인
    private Member validateEmailAndEmailAuthentication(Purpose purpose, String email) {

        // 서비스에 등록된 이메일인지 확인
        Member member = memberRepository.findByEmail(email)
            .orElseThrow(() -> new BusinessException(MemberErrorCode.NOT_FOUND_MEMBER));

        // 이메일 본인 인증을 완료한 사용자인지 확인
        String prefix = "auth:email:" + purpose.name().toLowerCase() + ":";

        Boolean hasKeyInMemory = redisTemplate.hasKey(prefix + email);

        if (Boolean.FALSE.equals(hasKeyInMemory)) {
            throw new BusinessException(AuthErrorCode.EMAIL_VERIFICATION_REQUIRED);
        }else if(hasKeyInMemory == null){
            throw new BusinessException(GlobalErrorCode.SERVER_ERROR);
        }

        return member;
    }

    // 인증 데이터 사용 후, 레디스에서 삭제
    private void deleteEmailAuthenticationData(Purpose purpose, String email){

        redisTemplate.delete("auth:email:" + purpose.name().toLowerCase() + ":" + email);
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
