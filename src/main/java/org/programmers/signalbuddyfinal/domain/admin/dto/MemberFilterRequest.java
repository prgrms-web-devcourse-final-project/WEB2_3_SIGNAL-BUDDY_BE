package org.programmers.signalbuddyfinal.domain.admin.dto;


import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.programmers.signalbuddyfinal.domain.admin.exception.AdminErrorCode;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberRole;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberStatus;
import org.programmers.signalbuddyfinal.domain.social.entity.Provider;
import org.programmers.signalbuddyfinal.global.exception.BusinessException;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberFilterRequest {

    private MemberRole role;

    private MemberStatus status;

    private Provider oAuthProvider;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime startDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime endDate;

    private String search;

    public void validateDateRange() {
        if (startDate == null && endDate == null) return;
        if (startDate == null) throw new BusinessException(AdminErrorCode.START_DATE_NOT_SELECTED);
        if (endDate == null) throw new BusinessException(AdminErrorCode.END_DATE_NOT_SELECTED);
        if (startDate.isAfter(endDate)) throw new BusinessException(AdminErrorCode.START_DATE_AFTER_END_DATE);
    }

}
