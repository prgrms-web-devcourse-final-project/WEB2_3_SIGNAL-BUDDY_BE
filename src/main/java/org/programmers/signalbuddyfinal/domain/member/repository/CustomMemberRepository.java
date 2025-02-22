package org.programmers.signalbuddyfinal.domain.member.repository;

import java.util.List;
import org.programmers.signalbuddyfinal.domain.admin.dto.AdminMemberResponse;
import org.programmers.signalbuddyfinal.domain.admin.dto.MemberFilterRequest;
import org.programmers.signalbuddyfinal.domain.admin.dto.WithdrawalMemberResponse;
import org.programmers.signalbuddyfinal.domain.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomMemberRepository {
    Page<Member> findAllMembers(Pageable pageable);
    Page<WithdrawalMemberResponse> findAllWithdrawMembers(Pageable pageable);
    Page<AdminMemberResponse> findAllMemberWithFilter(Pageable pageable, MemberFilterRequest filter);
    Page<AdminMemberResponse> findMemberByEmailOrNickname(Pageable pageable,String content);
}
