//package org.programmers.signalbuddyfinal.domain.admin.service;
//
//import lombok.RequiredArgsConstructor;
//import org.programmers.signalbuddyfinal.domain.admin.dto.AdminMemberResponse;
//import org.programmers.signalbuddyfinal.domain.postit.repository.PostItRepository;
//import org.programmers.signalbuddyfinal.global.dto.PageResponse;
//import org.springframework.data.domain.Pageable;
//import org.springframework.stereotype.Service;
//
//@Service
//@RequiredArgsConstructor
//public class AdminPostItService {
//
//    private final PostItRepository postItRepository;
//
//    public PageResponse<AdminMemberResponse> getAllPostIt(Pageable pageable) {
//        PageResponse<AdminMemberResponse> membersPage = postItRepository.(pageable);
//
//        return membersPage;
//    }
//}
