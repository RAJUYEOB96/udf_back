package com.undefinedus.backend.service;

import com.undefinedus.backend.domain.entity.Discussion;
import com.undefinedus.backend.domain.enums.DiscussionStatus;
import com.undefinedus.backend.dto.request.discussion.DiscussionRegisterRequestDTO;
import com.undefinedus.backend.dto.request.discussion.DiscussionUpdateRequestDTO;
import com.undefinedus.backend.dto.request.discussionComment.DiscussionScrollRequestDTO;
import com.undefinedus.backend.dto.response.ScrollResponseDTO;
import com.undefinedus.backend.dto.response.discussion.DiscussionDetailResponseDTO;
import com.undefinedus.backend.dto.response.discussion.DiscussionListResponseDTO;
import org.quartz.SchedulerException;

public interface DiscussionService {

    Long discussionRegister(Long memberId, DiscussionRegisterRequestDTO discussionRegisterRequestDTO);

    Discussion changeStatus(Long discussionId, DiscussionStatus discussionStatus);

    ScrollResponseDTO<DiscussionListResponseDTO> getDiscussionList(
        DiscussionScrollRequestDTO discussionScrollRequestDTO);

    DiscussionDetailResponseDTO getDiscussionDetail(Long discussionId);

    Long discussionUpdate(Long memberId, String isbn13, Long discussionId,
        DiscussionUpdateRequestDTO discussionUpdateRequestDTO) throws Exception;

    void joinAgree(Long memberId, Long discussionId);

    void joinDisagree(Long memberId, Long discussionId);

    void deleteDiscussion(Long memberId, Long discussionId) throws SchedulerException;
}
