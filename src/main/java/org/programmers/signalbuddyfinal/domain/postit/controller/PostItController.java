package org.programmers.signalbuddyfinal.domain.postit.controller;

import lombok.RequiredArgsConstructor;
import org.programmers.signalbuddyfinal.domain.postit.dto.PostItRequest;
import org.programmers.signalbuddyfinal.domain.postit.dto.PostItResponse;
import org.programmers.signalbuddyfinal.domain.postit.service.PostItService;
import org.programmers.signalbuddyfinal.global.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/postits")
@RequiredArgsConstructor
public class PostItController {

    private final PostItService postItService;

    @PostMapping
    public ResponseEntity<ApiResponse<PostItResponse>> createPostIt(
        @RequestBody final PostItRequest postItRequest) {
        return ResponseEntity.ok(
            ApiResponse.createSuccess(postItService.createPostIt(postItRequest)));
    }
}
