package com.undefinedus.backend.controller;

import com.undefinedus.backend.dto.MemberSecurityDTO;
import com.undefinedus.backend.dto.request.discussionComment.DiscussionCommentRequestDTO;
import com.undefinedus.backend.dto.request.discussionComment.DiscussionCommentsScrollRequestDTO;
import com.undefinedus.backend.dto.response.ApiResponseDTO;
import com.undefinedus.backend.dto.response.ScrollResponseDTO;
import com.undefinedus.backend.dto.response.discussionComment.DiscussionCommentResponseDTO;
import com.undefinedus.backend.service.DiscussionCommentService;
import jakarta.validation.Valid;
import java.util.List;
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

    // 댓글 달기
    @PostMapping("/writeComment/{discussionId}")
    public ResponseEntity<ApiResponseDTO<Void>> writeComment(
        @AuthenticationPrincipal MemberSecurityDTO memberSecurityDTO,
        @PathVariable("discussionId") Long discussionId,
        @Valid @RequestBody DiscussionCommentRequestDTO discussionCommentRequestDTO
    ) {

        Long memberId = memberSecurityDTO.getId();

        discussionCommentService.writeComment(discussionId, memberId, discussionCommentRequestDTO);

        return ResponseEntity.status(HttpStatus.OK)
            .body(ApiResponseDTO.success(null));
    }

    // 답글 달기
    @PostMapping("/writeComment/{discussionId}/{discussionCommentId}")
    public ResponseEntity<ApiResponseDTO<Void>> writeReply(
        @AuthenticationPrincipal MemberSecurityDTO memberSecurityDTO,
        @PathVariable("discussionId") Long discussionId,
        @PathVariable("discussionCommentId") Long discussionCommentId,
        @Valid @RequestBody DiscussionCommentRequestDTO discussionCommentRequestDTO
    ) {

        Long memberId = memberSecurityDTO.getId();

        discussionCommentService.writeReply(discussionId, discussionCommentId, memberId,
            discussionCommentRequestDTO);

        return ResponseEntity.status(HttpStatus.OK)
            .body(ApiResponseDTO.success(null));
    }

    // 베스트 3 댓글 목록
    @GetMapping("/bestComment/{discussionId}")
    public ResponseEntity<ApiResponseDTO<List<DiscussionCommentResponseDTO>>> getBest3CommentsList(
        @PathVariable("discussionId") Long discussionId) {

        List<DiscussionCommentResponseDTO> best3CommentByCommentLikes = discussionCommentService.getBest3CommentByCommentLikes(
            discussionId);
        return ResponseEntity.ok(ApiResponseDTO.success(best3CommentByCommentLikes));
    }

    // 댓글 리스트
    @GetMapping("/{discussionId}")
    public ResponseEntity<ApiResponseDTO<ScrollResponseDTO<DiscussionCommentResponseDTO>>> getCommentList(
            @AuthenticationPrincipal MemberSecurityDTO memberSecurityDTO,
            @PathVariable("discussionId") Long discussionId,
            @ModelAttribute DiscussionCommentsScrollRequestDTO requestDTO
    ) {
        
        ScrollResponseDTO<DiscussionCommentResponseDTO> response = discussionCommentService.getCommentList(
            memberSecurityDTO.getId(), requestDTO, discussionId);

        return ResponseEntity.ok(ApiResponseDTO.success(response));
    }

    // 댓글에 좋아요 달기
    @PatchMapping("/addLike/{discussionCommentId}")
    public ResponseEntity<ApiResponseDTO<Void>> addLike(
        @AuthenticationPrincipal MemberSecurityDTO memberSecurityDTO,
        @PathVariable Long discussionCommentId
    ) {

        Long memberId = memberSecurityDTO.getId();

        discussionCommentService.addLike(memberId, discussionCommentId);

        return ResponseEntity.ok().body(ApiResponseDTO.success(null));
    }

    // 댓글에 싫어요 달기
    @PatchMapping("/addDislike/{discussionCommentId}")
    public ResponseEntity<ApiResponseDTO<Void>> addDislike(
        @AuthenticationPrincipal MemberSecurityDTO memberSecurityDTO,
        @PathVariable Long discussionCommentId
    ) {

        Long memberId = memberSecurityDTO.getId();

        discussionCommentService.addDislike(memberId, discussionCommentId);

        return ResponseEntity.ok().body(ApiResponseDTO.success(null));
    }

    // 필요 시 사용 // 댓글 삭제
    @DeleteMapping("/{commentId}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteComment(
        @AuthenticationPrincipal MemberSecurityDTO memberSecurityDTO,
        @PathVariable("commentId") Long commentId
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