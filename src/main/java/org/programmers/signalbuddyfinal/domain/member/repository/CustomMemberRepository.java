package org.programmers.signalbuddyfinal.domain.member.repository;

import org.programmers.signalbuddyfinal.domain.admin.dto.WithdrawalMemberResponse;
import org.programmers.signalbuddyfinal.domain.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomMemberRepository {
    Page<Member> findAllMembers(Pageable pageable);
    Page<WithdrawalMemberResponse> findAllWithdrawMembers(Pageable pageable);
}
