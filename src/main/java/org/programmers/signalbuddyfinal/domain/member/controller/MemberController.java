package org.programmers.signalbuddyfinal.domain.member.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.programmers.signalbuddyfinal.domain.bookmark.dto.BookmarkRequest;
import org.programmers.signalbuddyfinal.domain.bookmark.dto.BookmarkResponse;
import org.programmers.signalbuddyfinal.domain.bookmark.dto.BookmarkSequenceUpdateRequest;
import org.programmers.signalbuddyfinal.domain.bookmark.service.BookmarkService;
import org.programmers.signalbuddyfinal.domain.feedback.dto.FeedbackResponse;
import org.programmers.signalbuddyfinal.domain.feedback.service.FeedbackService;
import org.programmers.signalbuddyfinal.domain.member.dto.MemberJoinRequest;
import org.programmers.signalbuddyfinal.domain.member.dto.MemberResponse;
import org.programmers.signalbuddyfinal.domain.member.dto.MemberUpdateRequest;
import org.programmers.signalbuddyfinal.domain.member.service.MemberService;
import org.programmers.signalbuddyfinal.domain.recentpath.dto.RecentPathRequest;
import org.programmers.signalbuddyfinal.domain.recentpath.dto.RecentPathResponse;
import org.programmers.signalbuddyfinal.domain.recentpath.service.RecentPathService;
import org.programmers.signalbuddyfinal.global.dto.PageResponse;
import org.programmers.signalbuddyfinal.global.response.ApiResponse;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
@Slf4j
public class MemberController {

    private final MemberService memberService;
    private final FeedbackService feedbackService;
    private final BookmarkService bookmarkService;
    private final RecentPathService recentPathService;

    @GetMapping("{id}")
    public ResponseEntity<ApiResponse<MemberResponse>> findById(@PathVariable Long id) {
        final MemberResponse memberResponse = memberService.getMember(id);
        return ResponseEntity.ok(ApiResponse.createSuccess(memberResponse));
    }

    @PatchMapping("{id}")
    public ResponseEntity<ApiResponse<MemberResponse>> updateMember(@PathVariable Long id,
        @RequestBody MemberUpdateRequest memberUpdateRequest, HttpServletRequest request) {
        log.info("id : {}, UpdateRequest: {}", id, memberUpdateRequest);
        final MemberResponse updated = memberService.updateMember(id, memberUpdateRequest, request);
        return ResponseEntity.ok(ApiResponse.createSuccess(updated));
    }

    @PatchMapping("{id}/profile-image")
    public ResponseEntity<ApiResponse<String>> updateProfileImage(@PathVariable Long id,
        @RequestPart("imageFile") MultipartFile imageFile) {
        final String saveProfileImage = memberService.saveProfileImage(id, imageFile);
        return ResponseEntity.ok(ApiResponse.createSuccess(saveProfileImage));
    }

    @DeleteMapping("{id}")
    public ResponseEntity<ApiResponse<Object>> deleteMember(@PathVariable Long id) {
        memberService.deleteMember(id);
        return ResponseEntity.ok(ApiResponse.createSuccessWithNoData());
    }

    @PostMapping("{id}/verify-password")
    public ResponseEntity<ApiResponse<Boolean>> verifyPassword(@PathVariable Long id,
        @RequestParam String password) {
        final boolean verified = memberService.verifyPassword(password, id);
        return ResponseEntity.ok(ApiResponse.createSuccess(verified));
    }

    /**
     * ApiResponse 는 JSON 형태의 응답을 생성하는데 사용되므로 <br> 이미지 파일을 직접 반환하기 위해
     * {@code ResponseEntity<Resource>}를 사용.
     *
     * @param id Member ID
     * @return Resource 객체
     */
    @GetMapping("{id}/profile-image")
    public ResponseEntity<Resource> getImageFile(@PathVariable Long id) {
        final Resource profileImage = memberService.getProfileImage(id);
        return ResponseEntity.ok(profileImage);
    }

    @GetMapping("{id}/feedbacks")
    public ResponseEntity<ApiResponse<PageResponse<FeedbackResponse>>> getFeedbacks(
        @PathVariable Long id, @PageableDefault(page = 0, size = 10) Pageable pageable) {
        final PageResponse<FeedbackResponse> feedbacks = feedbackService.findPagedExcludingMember(
            id, pageable);
        return ResponseEntity.ok(ApiResponse.createSuccess(feedbacks));
    }

    @GetMapping("{id}/bookmarks")
    public ResponseEntity<ApiResponse<PageResponse<BookmarkResponse>>> getBookmarks(
        @PathVariable Long id, @PageableDefault(page = 0, size = 10) Pageable pageable) {
        final PageResponse<BookmarkResponse> bookmarks = bookmarkService.findPagedBookmarks(
            pageable, id);
        return ResponseEntity.ok(ApiResponse.createSuccess(bookmarks));
    }

    @PostMapping("{id}/bookmarks")
    public ResponseEntity<ApiResponse<BookmarkResponse>> saveBookmark(@PathVariable Long id,
        @RequestBody @Valid BookmarkRequest bookmarkRequest) {
        final BookmarkResponse saved = bookmarkService.createBookmark(bookmarkRequest, id);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.createSuccess(saved));
    }

    @GetMapping("{id}/bookmarks/{bookmarkId}")
    public ResponseEntity<ApiResponse<BookmarkResponse>> getBookmark(@PathVariable Long id,
        @PathVariable Long bookmarkId) {
        final BookmarkResponse bookmark = bookmarkService.getBookmark(id, bookmarkId);
        return ResponseEntity.ok(ApiResponse.createSuccess(bookmark));
    }

    @PatchMapping("{id}/bookmarks/{bookmarkId}")
    public ResponseEntity<ApiResponse<BookmarkResponse>> updateBookmark(@PathVariable Long id,
        @PathVariable Long bookmarkId, @RequestBody @Valid BookmarkRequest bookmarkRequest) {
        final BookmarkResponse updated = bookmarkService.updateBookmark(bookmarkRequest, bookmarkId,
            id);
        return ResponseEntity.ok(ApiResponse.createSuccess(updated));
    }

    @DeleteMapping("{id}/bookmarks")
    public ResponseEntity<ApiResponse<Object>> deleteBookmark(@PathVariable Long id,
        @RequestParam List<Long> bookmarkIds) {
        bookmarkService.deleteBookmark(bookmarkIds, id);
        return ResponseEntity.ok(ApiResponse.createSuccessWithNoData());
    }

    @PatchMapping("{id}/bookmarks/sequence/reorder")
    public ResponseEntity<ApiResponse<List<BookmarkResponse>>> updateBookmarkSequences(
        @PathVariable Long id, @RequestBody @Valid List<BookmarkSequenceUpdateRequest> requests) {
        final List<BookmarkResponse> updated = bookmarkService.updateBookmarkSequences(id,
            requests);
        return ResponseEntity.ok(ApiResponse.createSuccess(updated));
    }

    @PostMapping("{id}/recent-path")
    public ResponseEntity<ApiResponse<RecentPathResponse>> saveRecentPath(@PathVariable Long id,
        @RequestBody RecentPathRequest request) {
        final RecentPathResponse response = recentPathService.saveRecentPath(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.createSuccess(response));
    }

    @GetMapping("{id}/recent-path")
    public ResponseEntity<ApiResponse<List<RecentPathResponse>>> getRecentPathList(
        @PathVariable Long id) {
        final List<RecentPathResponse> recentPathList = recentPathService.getRecentPathList(id);
        return ResponseEntity.ok(ApiResponse.createSuccess(recentPathList));
    }

    @PostMapping("/join")
    public ResponseEntity<ApiResponse<MemberResponse>> joinMember(
        @Valid @RequestPart("memberJoinRequest") MemberJoinRequest memberJoinRequest,
        @RequestPart("profileImageUrl") MultipartFile profileImage
    ){
        MemberResponse memberResponse = memberService.joinMember(memberJoinRequest, profileImage);
        return ResponseEntity.ok(ApiResponse.createSuccess(memberResponse));
    }

}
