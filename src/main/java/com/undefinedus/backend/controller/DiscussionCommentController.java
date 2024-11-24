package com.undefinedus.backend.controller;

import com.undefinedus.backend.dto.MemberSecurityDTO;
import com.undefinedus.backend.dto.request.DiscussionCommentsScrollRequestDTO;
import com.undefinedus.backend.dto.response.ApiResponseDTO;
import com.undefinedus.backend.dto.response.ScrollResponseDTO;
import com.undefinedus.backend.dto.request.discussionComment.DiscussionCommentRequestDTO;
import com.undefinedus.backend.dto.response.discussionComment.DiscussionCommentListResponseDTO;
import com.undefinedus.backend.service.DiscussionCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/discussionComment")
public class DiscussionCommentController {

    private final DiscussionCommentService discussionCommentService;

    @PostMapping("/writeComment/{discussionId}")
    public ResponseEntity<ApiResponseDTO<Void>> writeComment(
        @AuthenticationPrincipal MemberSecurityDTO memberSecurityDTO,
        @PathVariable(name = "discussionId") Long discussionId,
        @RequestBody DiscussionCommentRequestDTO discussionCommentRequestDTO
    ) {

        Long memberId = memberSecurityDTO.getId();

        discussionCommentService.writeComment(discussionId, memberId, discussionCommentRequestDTO);

        return ResponseEntity.status(HttpStatus.OK)
            .body(ApiResponseDTO.success(null));
    }

    @PostMapping("/writeComment/{discussionId}/{discussionCommentId}")
    public ResponseEntity<ApiResponseDTO<Void>> writeReply(
        @AuthenticationPrincipal MemberSecurityDTO memberSecurityDTO,
        @PathVariable(name = "discussionId") Long discussionId,
        @PathVariable(name = "discussionCommentId") Long discussionCommentId,
        @RequestBody DiscussionCommentRequestDTO discussionCommentRequestDTO
    ) {

        Long memberId = memberSecurityDTO.getId();

        discussionCommentService.writeReply(discussionId, discussionCommentId, memberId,
            discussionCommentRequestDTO);

        return ResponseEntity.status(HttpStatus.OK)
            .body(ApiResponseDTO.success(null));
    }

    @GetMapping
    public ResponseEntity<ApiResponseDTO<ScrollResponseDTO<DiscussionCommentListResponseDTO>>> getCommentList(
        @ModelAttribute DiscussionCommentsScrollRequestDTO requestDTO
    ) {

        ScrollResponseDTO<DiscussionCommentListResponseDTO> response = discussionCommentService.getCommentList(
            requestDTO);

        return ResponseEntity.ok(ApiResponseDTO.success(response));
    }

    @PatchMapping("/addLike/{discussionCommentId}")
    public ResponseEntity<ApiResponseDTO<Void>> addLike(
        @AuthenticationPrincipal MemberSecurityDTO memberSecurityDTO,
        @PathVariable Long discussionCommentId
    ) {

        Long memberId = memberSecurityDTO.getId();

        discussionCommentService.addLike(memberId, discussionCommentId);

        return ResponseEntity.ok().body(ApiResponseDTO.success(null));
    }

    @PatchMapping("/addDislike/{discussionCommentId}")
    public ResponseEntity<ApiResponseDTO<Void>> addDislike(
        @AuthenticationPrincipal MemberSecurityDTO memberSecurityDTO,
        @PathVariable Long discussionCommentId
    ) {

        Long memberId = memberSecurityDTO.getId();

        discussionCommentService.addDislike(memberId, discussionCommentId);

        return ResponseEntity.ok().body(ApiResponseDTO.success(null));
    }

    @DeleteMapping("/{commentId}")
    // 필요 시 사용
    public ResponseEntity<ApiResponseDTO<Void>> deleteComment(
        @AuthenticationPrincipal MemberSecurityDTO memberSecurityDTO,
        @PathVariable(name = "commentId") Long commentId
    ) {

        Long memberId = memberSecurityDTO.getId();

        try {

            discussionCommentService.deleteComment(memberId, commentId);

        } catch (Exception e) {

            throw new RuntimeException("댓글 삭제에 실패했습니다." + e.getMessage());
        }

        return ResponseEntity.ok().body(ApiResponseDTO.success(null));
    }

}