package com.undefinedus.backend.controller;

import com.undefinedus.backend.dto.MemberSecurityDTO;
import com.undefinedus.backend.dto.request.discussion.DiscussionRegisterRequestDTO;
import com.undefinedus.backend.dto.request.discussion.DiscussionUpdateRequestDTO;
import com.undefinedus.backend.dto.request.discussionComment.DiscussionScrollRequestDTO;
import com.undefinedus.backend.dto.response.ApiResponseDTO;
import com.undefinedus.backend.dto.response.ScrollResponseDTO;
import com.undefinedus.backend.dto.response.discussion.DiscussionDetailResponseDTO;
import com.undefinedus.backend.dto.response.discussion.DiscussionListResponseDTO;
import com.undefinedus.backend.exception.discussion.DiscussionException;
import com.undefinedus.backend.service.DiscussionService;
import jakarta.validation.Valid;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/discussion")
public class DiscussionController {

    private final DiscussionService discussionService;

    // 발의 작성
    @PostMapping("/register")
    public ResponseEntity<ApiResponseDTO<Void>> discussionRegister(
        @AuthenticationPrincipal MemberSecurityDTO memberSecurityDTO,
        @RequestParam(name = "isbn13") String isbn13,
        @Valid @RequestBody DiscussionRegisterRequestDTO discussionRegisterRequestDTO) {

        Long memberId = memberSecurityDTO.getId();

        try {
            discussionService.discussionRegister(memberId, isbn13, discussionRegisterRequestDTO);

        } catch (DiscussionException e) {

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponseDTO.error("토론 생성에 실패했습니다."));
        }

        return ResponseEntity.status(HttpStatus.OK).body(ApiResponseDTO.success(null));
    }

    // 토론(토론 상태 값 입력시 입력한 상태의 토론 게시판) 목록 보기
    @GetMapping
    public ResponseEntity<ApiResponseDTO<ScrollResponseDTO<DiscussionListResponseDTO>>> getDiscussionList(
        @ModelAttribute DiscussionScrollRequestDTO requestDTO) {
        ScrollResponseDTO<DiscussionListResponseDTO> response = discussionService.getDiscussionList(
            requestDTO);

        return ResponseEntity.ok(ApiResponseDTO.success(response));
    }

    // 토론, 발의 상세 보기
    @GetMapping("/detail")
    public ResponseEntity<ApiResponseDTO<DiscussionDetailResponseDTO>> getDiscussionDetail(
        @RequestParam("discussionId") Long discussionId
    ) {
        DiscussionDetailResponseDTO discussionDetail = discussionService.getDiscussionDetail(
            discussionId);

        return ResponseEntity.ok(ApiResponseDTO.success(discussionDetail));
    }

    // 발의글에 찬성으로 참여하기
    @GetMapping("/joinAgree")
    public ResponseEntity<ApiResponseDTO<Void>> joinAgree(
        @AuthenticationPrincipal MemberSecurityDTO memberSecurityDTO,
        @RequestParam("discussionId") Long discussionId
    ) {

        try {
            discussionService.joinAgree(memberSecurityDTO.getId(), discussionId);

        } catch (DiscussionException e) {

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponseDTO.error("토론 찬성에 실패했습니다."));
        }

        return ResponseEntity.status(HttpStatus.OK).body(ApiResponseDTO.success(null));
    }

    // 발의글에 반대로 참석하기
    @GetMapping("/joinDisagree")
    public ResponseEntity<ApiResponseDTO<Void>> joinDisagree(
        @AuthenticationPrincipal MemberSecurityDTO memberSecurityDTO,
        @RequestParam("discussionId") Long discussionId
    ) {

        try {
            discussionService.joinDisagree(memberSecurityDTO.getId(), discussionId);

        } catch (DiscussionException e) {

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponseDTO.error("토론 반대에 실패했습니다."));
        }

        return ResponseEntity.status(HttpStatus.OK).body(ApiResponseDTO.success(null));
    }

    // 발의 상태일때만 수정하기
    @PatchMapping("/update")
    public ResponseEntity<ApiResponseDTO<Void>> discussionUpdate(
        @AuthenticationPrincipal MemberSecurityDTO memberSecurityDTO,
        @RequestParam("isbn13") String isbn13,
        @RequestParam("discussionId") Long discussionId,
        @Valid @RequestBody DiscussionUpdateRequestDTO discussionUpdateRequestDTO) {

        try {
            discussionService.discussionUpdate(memberSecurityDTO.getId(), isbn13, discussionId,
                discussionUpdateRequestDTO);

        } catch (Exception e) {

            throw new RuntimeException("토론 수정에 실패했습니다." + e.getMessage());
        }

        return ResponseEntity.ok().body(ApiResponseDTO.success(null));
    }

    // 내가 만든 토론 삭제
    @DeleteMapping("/{discussionId}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteDiscussion(
        @AuthenticationPrincipal MemberSecurityDTO memberSecurityDTO,
        @PathVariable("discussionId") Long discussionId) {

        try {
            discussionService.deleteDiscussion(memberSecurityDTO.getId(), discussionId);

        } catch (Exception e) {

            throw new RuntimeException("토론 삭제에 실패했습니다." + e.getMessage());
        }

        return ResponseEntity.ok().body(ApiResponseDTO.success(null));
    }

}
