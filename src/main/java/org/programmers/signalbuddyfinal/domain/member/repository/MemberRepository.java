package org.programmers.signalbuddyfinal.domain.member.repository;

import org.programmers.signalbuddyfinal.domain.member.entity.Member;
import org.programmers.signalbuddyfinal.domain.member.exception.MemberErrorCode;
import org.programmers.signalbuddyfinal.domain.social.entity.Provider;
import org.programmers.signalbuddyfinal.global.exception.BusinessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long>, CustomMemberRepository {

    Page<Member> findAll(Pageable pageable);

    boolean existsByEmail(String email);

    Optional<Member> findByEmail(String email);

    Member save(Member member);

    void delete(Member member);

    default Member findByIdOrThrow(Long id) {
        return findById(id)
            .orElseThrow(() -> new BusinessException(MemberErrorCode.NOT_FOUND_MEMBER));
    }

    boolean existsByNickname(String nickName);

    @Query("select m from members m "
        + "inner join SocialProvider s "
        + "on m.memberId = s.member.memberId "
        + "where s.oauthProvider = :provider and s.socialId = :socialId")
    Optional<Member> findBySocialIdAndProviderId(Provider provider, String socialId);
}
