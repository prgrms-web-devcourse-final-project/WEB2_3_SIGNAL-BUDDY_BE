package org.programmers.signalbuddyfinal.domain.postit_report.repository;

import org.programmers.signalbuddyfinal.domain.postit_report.entity.PostItReport;
import org.programmers.signalbuddyfinal.domain.postit_report.exeception.PostItReportErrorCode;
import org.programmers.signalbuddyfinal.global.exception.BusinessException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PostItReportRepository extends JpaRepository<PostItReport, Long> {

    @Query("SELECT p FROM postit_report p WHERE p.postit.postitId = :postItId and p.member.memberId = :memberId")
    PostItReport findPostItReportByPostItIdAndMemberId(@Param("postItId") Long postItId,
        @Param("memberId") Long memberId);

    @Modifying
    @Query("DELETE FROM postit_report p WHERE p.postit.postitId = :postItId and p.member.memberId = :memberId")
    void deleteByPostItIdAndUserId(@Param("postItId") Long postItId,
        @Param("memberId") Long memberId);

    @Modifying
    @Query("DELETE FROM postit_report ps WHERE ps.postit.postitId = :postitId")
    void deleteByPostItId(@Param("postitId") Long postitId);

    default PostItReport findByIdOrThrow(Long id) {
        return findById(id).orElseThrow(
            () -> new BusinessException(PostItReportErrorCode.NOT_FOUND_POSTIT_REPORT));
    }
}
