package org.programmers.signalbuddyfinal.domain.admin.service;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.programmers.signalbuddyfinal.domain.admin.dto.AdminPostItResponse;
import org.programmers.signalbuddyfinal.domain.admin.dto.PostItFilterRequest;
import org.programmers.signalbuddyfinal.domain.admin.exception.AdminErrorCode;
import org.programmers.signalbuddyfinal.domain.postit.dto.PostItResponse;
import org.programmers.signalbuddyfinal.domain.postit.entity.Postit;
import org.programmers.signalbuddyfinal.domain.postit.mapper.PostItMapper;
import org.programmers.signalbuddyfinal.domain.postit.repository.PostItRepository;
import org.programmers.signalbuddyfinal.domain.postit.service.PostItComplete;
import org.programmers.signalbuddyfinal.global.dto.PageResponse;
import org.programmers.signalbuddyfinal.global.exception.BusinessException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminPostItService {

    private final PostItRepository postItRepository;
    private final PostItComplete postItComplete;

    public PageResponse<AdminPostItResponse> getAllPostIt(Pageable pageable) {
        PageResponse<AdminPostItResponse> postItResponses = postItRepository.findAllPostIt(
            pageable);

        return postItResponses;
    }

    @Transactional
    public PostItResponse completePostIt(Long postitId) {

        Postit postit = postItRepository.findByIdOrThrow(postitId);

        LocalDateTime deletedAt = LocalDateTime.now();
        postItComplete.completePostIt(postit, deletedAt);

        return PostItMapper.INSTANCE.toResponse(postit);
    }

    public PageResponse<AdminPostItResponse> getAllPostItWithFilter(Pageable pageable,
        PostItFilterRequest postItFilterRequest) {

        checkFilterException(postItFilterRequest);

        PageResponse<AdminPostItResponse> postIts = postItRepository.findAllPostItWithFilter(
            pageable,
            postItFilterRequest);

        return postIts;
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
}
