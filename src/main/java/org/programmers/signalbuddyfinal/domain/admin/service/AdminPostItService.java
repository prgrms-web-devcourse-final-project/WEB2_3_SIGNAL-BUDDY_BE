package org.programmers.signalbuddyfinal.domain.admin.service;

import lombok.RequiredArgsConstructor;
import org.programmers.signalbuddyfinal.domain.admin.dto.AdminPostItResponse;
import org.programmers.signalbuddyfinal.domain.postit.repository.PostItRepository;
import org.programmers.signalbuddyfinal.global.dto.PageResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminPostItService {

    private final PostItRepository postItRepository;

    public PageResponse<AdminPostItResponse> getAllPostIt(Pageable pageable) {
        PageResponse<AdminPostItResponse> postItResponses = postItRepository.findAllPostIt(pageable);

        return postItResponses;
    }
}
