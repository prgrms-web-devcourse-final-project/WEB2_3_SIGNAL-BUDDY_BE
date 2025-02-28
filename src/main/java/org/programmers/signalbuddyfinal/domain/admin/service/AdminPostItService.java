package org.programmers.signalbuddyfinal.domain.admin.service;

import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.programmers.signalbuddyfinal.domain.admin.dto.AdminPostItResponse;
import org.programmers.signalbuddyfinal.domain.admin.dto.PostItFilterRequest;
import org.programmers.signalbuddyfinal.domain.admin.exception.AdminErrorCode;
import org.programmers.signalbuddyfinal.domain.member.entity.Member;
import org.programmers.signalbuddyfinal.domain.member.repository.MemberRepository;
import org.programmers.signalbuddyfinal.domain.postit.dto.PostItResponse;
import org.programmers.signalbuddyfinal.domain.postit.entity.Postit;
import org.programmers.signalbuddyfinal.domain.postit.mapper.PostItMapper;
import org.programmers.signalbuddyfinal.domain.postit.repository.PostItRepository;
import org.programmers.signalbuddyfinal.domain.postit.service.PostItComplete;
import org.programmers.signalbuddyfinal.domain.postitsolve.entity.PostitSolve;
import org.programmers.signalbuddyfinal.domain.postitsolve.repository.PostitSolveRepository;
import org.programmers.signalbuddyfinal.global.dto.PageResponse;
import org.programmers.signalbuddyfinal.global.exception.BusinessException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminPostItService {
    // TODO : 만료일만 변경하는 기능 추가,,,
    private final PostItRepository postItRepository;
    private final MemberRepository memberRepository;
    private final PostitSolveRepository postitSolveRepository;

    public PageResponse<AdminPostItResponse> getAllPostIt(Pageable pageable) {

        return postItRepository.findAllPostIt(pageable);
    }

    @Transactional
    public PostItResponse completePostIt(Long postitId, LocalDateTime expireDate) {

        Postit postit = postItRepository.findByIdOrThrow(postitId);
        if ( expireDate!=null && expireDate.isBefore(LocalDateTime.now())) {
            throw new BusinessException(AdminErrorCode.EXPIRED_DATE_INVALID);
        }

        return PostItMapper.INSTANCE.toResponse(completePostIt(postit, expireDate));
    }

    public PageResponse<AdminPostItResponse> getAllPostItWithFilter(Pageable pageable,
        PostItFilterRequest postItFilterRequest) {

        checkFilterException(postItFilterRequest);

        return postItRepository.findAllPostItWithFilter(pageable, postItFilterRequest);
    }

    private void checkFilterException(PostItFilterRequest postItFilterRequest) {

        if (postItFilterRequest.getStartDate() != null
            && postItFilterRequest.getEndDate() == null) {
            throw new BusinessException(AdminErrorCode.END_DATE_NOT_SELECTED);
        }

        if (postItFilterRequest.getStartDate() == null
            && postItFilterRequest.getEndDate() != null) {
            throw new BusinessException(AdminErrorCode.START_DATE_NOT_SELECTED);
        }

        if (postItFilterRequest.getStartDate().isAfter(postItFilterRequest.getEndDate())) {
            throw new BusinessException(AdminErrorCode.START_DATE_AFTER_END_DATE);
        }
    }

    private Postit completePostIt(Postit postit, LocalDateTime expireDate) {

        if (postit.getDeletedAt() == null) {
            // 미해결 -> 해결
            LocalDateTime deletedAt = LocalDateTime.now();

            postit.completePostIt(deletedAt);
            postitSolveRepository.save(PostitSolve.creator()
                .content(postit.getContent())
                .deletedAt(deletedAt)
                .imageUrl(postit.getImageUrl())
                .member(postit.getMember())
                .postit(postit)
                .build());

        } else {
            // 해결 -> 미해결
            postitSolveRepository.delete(
                postitSolveRepository.findByPostItId(postit.getPostitId()));
            postit.returnCompletePostIt(expireDate);
        }
        return postit;
    }
}
