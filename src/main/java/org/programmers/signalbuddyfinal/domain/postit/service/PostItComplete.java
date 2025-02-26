package org.programmers.signalbuddyfinal.domain.postit.service;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.programmers.signalbuddyfinal.domain.member.entity.Member;
import org.programmers.signalbuddyfinal.domain.member.repository.MemberRepository;
import org.programmers.signalbuddyfinal.domain.postit.entity.Postit;
import org.programmers.signalbuddyfinal.domain.postitsolve.entity.PostitSolve;
import org.programmers.signalbuddyfinal.domain.postitsolve.repository.PostitSolveRepository;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class PostItComplete {

    private final MemberRepository memberRepository;
    private final PostitSolveRepository postitSolveRepository;

    public void completePostIt(Postit postit, LocalDateTime deletedAt) {

        postit.completePostIt(deletedAt);

        Member member = memberRepository.findById(postit.getMember().getMemberId())
            .orElse(null);

        PostitSolve postitSolve = PostitSolve.creator()
            .content(postit.getContent())
            .deletedAt(deletedAt)
            .imageUrl(postit.getImageUrl())
            .member(member)
            .postit(postit)
            .build();

        postitSolveRepository.save(postitSolve);
    }
}

