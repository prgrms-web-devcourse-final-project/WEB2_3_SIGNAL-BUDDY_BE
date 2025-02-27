package org.programmers.signalbuddyfinal.domain.admin.dto;


import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.programmers.signalbuddyfinal.domain.admin.dto.enums.Periods;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberRole;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberStatus;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberFilterRequest {

    private MemberRole role;

    private MemberStatus status;

    private String oAuthProvider;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime startDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime endDate;

    private Periods periods;

    private String search;
}
