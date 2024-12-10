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
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.quartz.SchedulerException;
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
    public ResponseEntity<ApiResponseDTO<Map<String, Long>>> discussionRegister(
        @AuthenticationPrincipal MemberSecurityDTO memberSecurityDTO,
        @Valid @RequestBody DiscussionRegisterRequestDTO discussionRegisterRequestDTO) {

        Long memberId = memberSecurityDTO.getId();
        
        Long id = discussionService.discussionRegister(memberId, discussionRegisterRequestDTO);
        
        Map<String, Long> result = new HashMap<>();
        result.put("id", id);
        
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponseDTO.success(result));
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
            @AuthenticationPrincipal MemberSecurityDTO memberSecurityDTO,
            @RequestParam("discussionId") Long discussionId
    ) {
        DiscussionDetailResponseDTO discussionDetail = discussionService.getDiscussionDetail(
                memberSecurityDTO.getId(), discussionId);

        return ResponseEntity.ok(ApiResponseDTO.success(discussionDetail));
    }

    // 발의글에 찬성으로 참여하기
    @PostMapping("/joinAgree")
    public ResponseEntity<ApiResponseDTO<Void>> joinAgree(
        @AuthenticationPrincipal MemberSecurityDTO memberSecurityDTO,
        @RequestParam("discussionId") Long discussionId
    ) {

        discussionService.joinAgree(memberSecurityDTO.getId(), discussionId);
        
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponseDTO.success(null));
    }

    // 발의글에 반대로 참석하기
    @PostMapping("/joinDisagree")
    public ResponseEntity<ApiResponseDTO<Void>> joinDisagree(
        @AuthenticationPrincipal MemberSecurityDTO memberSecurityDTO,
        @RequestParam("discussionId") Long discussionId
    ) {

        discussionService.joinDisagree(memberSecurityDTO.getId(), discussionId);

        return ResponseEntity.status(HttpStatus.OK).body(ApiResponseDTO.success(null));
    }

    // 발의 상태일때만 수정하기
    @PatchMapping("/update")
    public ResponseEntity<ApiResponseDTO<Map<String, Long>>> discussionUpdate(
        @AuthenticationPrincipal MemberSecurityDTO memberSecurityDTO,
        @RequestParam("isbn13") String isbn13,  // 수정할 책의 isbn13
        @RequestParam("discussionId") Long discussionId,
        @Valid @RequestBody DiscussionUpdateRequestDTO discussionUpdateRequestDTO) throws Exception {
        
        Map<String, Long> result = new HashMap<>();
        
        Long id = discussionService.discussionUpdate(memberSecurityDTO.getId(), isbn13, discussionId,
                    discussionUpdateRequestDTO);
        result.put("id", id);
        
        return ResponseEntity.ok().body(ApiResponseDTO.success(result));
    }

    // 내가 만든 토론 삭제
    @DeleteMapping("/{discussionId}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteDiscussion(
        @AuthenticationPrincipal MemberSecurityDTO memberSecurityDTO,
        @PathVariable("discussionId") Long discussionId) throws SchedulerException {

        discussionService.deleteDiscussion(memberSecurityDTO.getId(), discussionId);

     
        return ResponseEntity.ok().body(ApiResponseDTO.success(null));
    }

}
