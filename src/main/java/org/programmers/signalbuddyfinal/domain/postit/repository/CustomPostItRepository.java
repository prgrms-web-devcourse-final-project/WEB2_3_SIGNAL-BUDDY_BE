package org.programmers.signalbuddyfinal.domain.postit.repository;

import org.programmers.signalbuddyfinal.domain.admin.dto.AdminPostItResponse;
import org.programmers.signalbuddyfinal.global.dto.PageResponse;
import org.springframework.data.domain.Pageable;

public interface CustomPostItRepository {
    PageResponse<AdminPostItResponse> findAllPostIt(Pageable pageable);
}
