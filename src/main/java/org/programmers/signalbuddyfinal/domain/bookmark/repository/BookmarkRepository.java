package org.programmers.signalbuddyfinal.domain.bookmark.repository;

import java.util.List;
import java.util.Optional;
import org.programmers.signalbuddyfinal.domain.bookmark.entity.Bookmark;
import org.programmers.signalbuddyfinal.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, Long>,
    BookmarkRepositoryCustom {

    Optional<Bookmark> findTopByMemberOrderBySequenceDesc(Member member);

    List<Bookmark> findAllByBookmarkIdInAndMemberMemberId(List<Long> bookmarkIds, Long id);

    List<Bookmark> findAllBySequenceInAndMemberMemberId(List<Integer> targetSequences, Long id);

    Optional<Bookmark> findByBookmarkIdAndMemberMemberId(Long id, Long memberId);
}
