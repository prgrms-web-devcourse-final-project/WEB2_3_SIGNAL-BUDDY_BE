package org.programmers.signalbuddyfinal.domain.postit.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.programmers.signalbuddyfinal.domain.postit.dto.PostItCreateRequest;
import org.programmers.signalbuddyfinal.domain.postit.dto.PostItRequest;
import org.programmers.signalbuddyfinal.domain.postit.dto.PostItResponse;
import org.programmers.signalbuddyfinal.domain.postit.service.PostItService;
import org.programmers.signalbuddyfinal.global.annotation.CurrentUser;
import org.programmers.signalbuddyfinal.global.dto.CustomUser2Member;
import org.programmers.signalbuddyfinal.global.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/postits")
@RequiredArgsConstructor
public class PostItController {

    private final PostItService postItService;

    @PostMapping
    public ResponseEntity<ApiResponse<PostItResponse>> createPostIt(
        @Valid @RequestPart("request") final PostItCreateRequest postItCreateRequest,
        @RequestPart(value = "imageFile") MultipartFile image,
        @CurrentUser CustomUser2Member user
    ) {
        return ResponseEntity.ok(
            ApiResponse.createSuccess(postItService.createPostIt(postItCreateRequest, image, user)));
    }

    @PatchMapping("/{postitId}")
    public ResponseEntity<ApiResponse<PostItResponse>> updatePostIt(
        @PathVariable(value = "postitId") final Long postitId,
        @Valid @RequestPart("request") PostItRequest postItRequest,
        @RequestPart(value = "imageFile") MultipartFile image,
        @CurrentUser CustomUser2Member user
    ) {
        return ResponseEntity.ok(
            ApiResponse.createSuccess(
                postItService.updatePostIt(postitId, postItRequest, image, user)));
    }

    @DeleteMapping("/{postitId}")
    public ResponseEntity deletePostIt(
        @PathVariable(value = "postitId") final Long postitId,
        @CurrentUser CustomUser2Member user
    ) {
        postItService.deletePostIt(postitId, user);
        return ResponseEntity.ok(
            ApiResponse.createSuccessWithNoData());
    }
}
