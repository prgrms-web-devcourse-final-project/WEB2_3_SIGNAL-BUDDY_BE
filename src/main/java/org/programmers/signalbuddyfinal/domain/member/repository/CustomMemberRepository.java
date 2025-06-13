package org.programmers.signalbuddyfinal.domain.member.repository;

import java.util.Optional;
import org.programmers.signalbuddyfinal.domain.admin.dto.AdminMemberResponse;
import org.programmers.signalbuddyfinal.domain.admin.dto.MemberFilterRequest;
import org.programmers.signalbuddyfinal.domain.member.entity.Member;
import org.programmers.signalbuddyfinal.domain.social.entity.Provider;
import org.programmers.signalbuddyfinal.global.dto.PageResponse;
import org.springframework.data.domain.Pageable;

public interface CustomMemberRepository {
    PageResponse<AdminMemberResponse> findAllMembers(Pageable pageable);
    PageResponse<AdminMemberResponse> findAllMemberWithFilter(Pageable pageable, MemberFilterRequest filter);
    Optional<Member> findByProviderAndSocialId(Provider provider, String socialId);
}
