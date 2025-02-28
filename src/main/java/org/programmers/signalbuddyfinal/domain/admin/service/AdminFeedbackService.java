package org.programmers.signalbuddyfinal.domain.admin.service;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.programmers.signalbuddyfinal.domain.feedback.dto.FeedbackResponse;
import org.programmers.signalbuddyfinal.domain.feedback.dto.FeedbackSearchRequest;
import org.programmers.signalbuddyfinal.domain.feedback.repository.FeedbackRepository;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberRole;
import org.programmers.signalbuddyfinal.global.constant.SearchTarget;
import org.programmers.signalbuddyfinal.global.dto.CustomUser2Member;
import org.programmers.signalbuddyfinal.global.dto.PageResponse;
import org.programmers.signalbuddyfinal.global.exception.BusinessException;
import org.programmers.signalbuddyfinal.global.exception.GlobalErrorCode;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminFeedbackService {

    private final FeedbackRepository feedbackRepository;

    public PageResponse<FeedbackResponse> searchFeedbackList(
        Pageable pageable,
        SearchTarget target,
        FeedbackSearchRequest request, Boolean deleted,
        LocalDate startDate, LocalDate endDate,
        CustomUser2Member user
    ) {
        if (!MemberRole.ADMIN.equals(user.getRole())) {
            throw new BusinessException(GlobalErrorCode.ADMIN_ONLY);
        }
        return new PageResponse<>(
            feedbackRepository.findAllByFilter(
                pageable, target, request.getKeyword(), request.getStatus(),
                request.getCategory(), startDate, endDate,deleted
            )
        );
    }
}
