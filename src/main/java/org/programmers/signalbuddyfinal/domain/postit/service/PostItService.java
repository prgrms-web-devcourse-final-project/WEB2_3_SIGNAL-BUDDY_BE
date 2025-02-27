package org.programmers.signalbuddyfinal.domain.postit.service;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.programmers.signalbuddyfinal.global.util.PointUtil;
import org.programmers.signalbuddyfinal.domain.member.entity.Member;
import org.programmers.signalbuddyfinal.domain.member.repository.MemberRepository;
import org.programmers.signalbuddyfinal.domain.postit.dto.PostItCreateRequest;
import org.programmers.signalbuddyfinal.domain.postit.dto.PostItRequest;
import org.programmers.signalbuddyfinal.domain.postit.dto.PostItResponse;
import org.programmers.signalbuddyfinal.domain.postit.entity.Postit;
import org.programmers.signalbuddyfinal.domain.postit.exception.PostItErrorCode;
import org.programmers.signalbuddyfinal.domain.postit.mapper.PostItMapper;
import org.programmers.signalbuddyfinal.domain.postit.repository.PostItRepository;
import org.programmers.signalbuddyfinal.domain.postitsolve.repository.PostitSolveRepository;
import org.programmers.signalbuddyfinal.global.dto.CustomUser2Member;
import org.programmers.signalbuddyfinal.global.exception.BusinessException;
import org.programmers.signalbuddyfinal.global.service.AwsFileService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.locationtech.jts.geom.Point;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class PostItService {

    private final PostItRepository postItRepository;
    private final MemberRepository memberRepository;
    private final AwsFileService awsFileService;
    private final PostitSolveRepository postitSolveRepository;
    private final PostItComplete postItComplete;

    @Value("${cloud.aws.s3.folder.post-it}")
    private String postItDir;

    @Transactional
    public PostItResponse createPostIt(PostItCreateRequest postItCreateRequest, MultipartFile image, CustomUser2Member user) {

        Member member = memberRepository.findByIdOrThrow(user.getMemberId());

        String imageUrl = convertImageFile(image);
        Point coordinate = PointUtil.toPoint(postItCreateRequest.getLat(), postItCreateRequest.getLng());

        Postit postit = Postit.creator()
            .danger(postItCreateRequest.getDanger())
            .subject(postItCreateRequest.getSubject())
            .coordinate(coordinate)
            .content(postItCreateRequest.getContent())
            .imageUrl(imageUrl)
            .expiryDate(postItCreateRequest.getCreateDate().plusDays(7))
            .member(member)
            .build();
        postItRepository.save(postit);

        return PostItMapper.INSTANCE.toResponse(postit);
    }

    @Transactional
    public PostItResponse updatePostIt(Long postitId, PostItRequest postItRequest,
        MultipartFile image, CustomUser2Member user) {

        Postit postit = postItRepository.findByIdOrThrow(postitId);

        if (Member.isNotSameMember(user, postit.getMember())) {
            throw new BusinessException(PostItErrorCode.POSTIT_MODIFIER_NOT_AUTHORIZED);
        }
        postit.updatePostIt(postItRequest, convertImageFile(image));
        return PostItMapper.INSTANCE.toResponse(postit);
    }

    @Transactional
    public void deletePostIt(Long postitId, CustomUser2Member user){

        Postit postit = postItRepository.findByIdOrThrow(postitId);

        if(Member.isNotSameMember(user, postit.getMember())){
            throw new BusinessException(PostItErrorCode.POSTIT_MODIFIER_NOT_AUTHORIZED);
        }
        postit.delete();
    }

    @Transactional
    public PostItResponse completePostIt(Long postitId) {

        Postit postit = postItRepository.findByIdOrThrow(postitId);
        LocalDateTime deletedAt = LocalDateTime.now();

        postItComplete.completePostIt(postit,deletedAt);

        return PostItMapper.INSTANCE.toResponse(postit);
    }

    private String convertImageFile(MultipartFile image) {
        String imageUrl = null;
        if (image != null) {
            String fileName = awsFileService.uploadFileToS3(image, postItDir);
            imageUrl = awsFileService.getFileFromS3(fileName, postItDir).toString();
        }
        return imageUrl;
    }
}
