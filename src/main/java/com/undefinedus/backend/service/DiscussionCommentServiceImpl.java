package com.undefinedus.backend.service;

import com.undefinedus.backend.domain.entity.CommentLike;
import com.undefinedus.backend.domain.entity.Discussion;
import com.undefinedus.backend.domain.entity.DiscussionComment;
import com.undefinedus.backend.domain.entity.DiscussionParticipant;
import com.undefinedus.backend.domain.entity.Member;
import com.undefinedus.backend.domain.entity.Report;
import com.undefinedus.backend.domain.enums.ViewStatus;
import com.undefinedus.backend.domain.enums.VoteType;
import com.undefinedus.backend.dto.request.discussionComment.DiscussionCommentRequestDTO;
import com.undefinedus.backend.dto.request.discussionComment.DiscussionCommentsScrollRequestDTO;
import com.undefinedus.backend.dto.response.ScrollResponseDTO;
import com.undefinedus.backend.dto.response.discussionComment.DiscussionCommentResponseDTO;
import com.undefinedus.backend.exception.discussion.DiscussionNotFoundException;
import com.undefinedus.backend.exception.discussionComment.DiscussionCommentNotFoundException;
import com.undefinedus.backend.exception.discussionParticipant.DiscussionParticipantNotFoundException;
import com.undefinedus.backend.exception.member.MemberNotFoundException;
import com.undefinedus.backend.repository.CommentLikeRepository;
import com.undefinedus.backend.repository.DiscussionCommentRepository;
import com.undefinedus.backend.repository.DiscussionParticipantRepository;
import com.undefinedus.backend.repository.DiscussionRepository;
import com.undefinedus.backend.repository.MemberRepository;
import com.undefinedus.backend.repository.ReportRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Log4j2
public class DiscussionCommentServiceImpl implements DiscussionCommentService {

    private final DiscussionRepository discussionRepository;
    private final MemberRepository memberRepository;
    private final DiscussionCommentRepository discussionCommentRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final DiscussionParticipantRepository discussionParticipantRepository;
    private final ReportRepository reportRepository;

    // 댓글 달기
    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void writeComment(Long discussionId, Long memberId,
        DiscussionCommentRequestDTO discussionCommentRequestDTO) {

        // VoteType 값 변환
        VoteType voteType;
        try {
            voteType = VoteType.valueOf(discussionCommentRequestDTO.getVoteType().toUpperCase());
        } catch (IllegalArgumentException e) {
            log.error("Invalid VoteType: " + discussionCommentRequestDTO.getVoteType(), e);
            throw new IllegalArgumentException("유효하지 않은 VoteType 값입니다.");
        }

        Discussion discussion = discussionRepository.findById(discussionId).orElseThrow(
            () -> new DiscussionNotFoundException("해당 토론을 찾을 수 없습니다. : " + discussionId));

        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new MemberNotFoundException("해당 멤버를 찾을 수 없습니다. : " + memberId));

        Long groupId = discussionCommentRepository.findMaxGroupId() + 1;

        Long topTotalOrder =
            discussionCommentRepository.findTopTotalOrder(discussionId).orElse(0L) + 1;

        DiscussionComment discussionComment = DiscussionComment.builder()
            .discussion(discussion)
            .member(member)
            .isChild(false)
            .groupOrder(0L)
            .totalOrder(topTotalOrder)
            .groupId(groupId)
            .voteType(voteType)
            .content(discussionCommentRequestDTO.getContent())
            .build();

        discussionCommentRepository.save(discussionComment);

        DiscussionParticipant savedDiscussionParticipant = discussionParticipantRepository.findByDiscussionAndMember(
            discussion, member).orElse(null);

        // VoteType에 따라 isAgree 값을 결정
        boolean newIsAgree = voteType == VoteType.AGREE;

        // 이미 저장된 Participant가 있는 경우, 기존의 isAgree와 비교
        if (savedDiscussionParticipant != null) {

            if (savedDiscussionParticipant.isAgree() == newIsAgree) {

                // 기존의 isAgree 값과 동일하다면 추가 작업을 하지 않음
                return;
            } else {
                discussionParticipantRepository.deleteById(savedDiscussionParticipant.getId());
            }
        }

        // 새로운 isAgree 값인 경우에만 추가
        addParticipant(discussion, member, voteType);
    }

    // 답글 달기
    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void writeReply(Long discussionId, Long discussionCommentId, Long memberId,
        DiscussionCommentRequestDTO discussionCommentRequestDTO) {

        // VoteType 값 변환
        VoteType voteType;
        try {
            voteType = VoteType.valueOf(discussionCommentRequestDTO.getVoteType().toUpperCase());
        } catch (IllegalArgumentException e) {
            log.error("Invalid VoteType: " + discussionCommentRequestDTO.getVoteType(), e);
            throw new IllegalArgumentException("유효하지 않은 VoteType 값입니다.");
        }

        Discussion discussion = discussionRepository.findById(discussionId).orElseThrow(
            () -> new DiscussionNotFoundException("해당 토론을 찾을 수 없습니다. : " + discussionId));
        DiscussionComment parentDiscussionComment = discussionCommentRepository.findById(
            discussionCommentId).orElseThrow(() -> new DiscussionCommentNotFoundException(
            "해당 댓글을 찾을 수 없습니다. : " + discussionCommentId));
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new MemberNotFoundException("해당 멤버를 찾을 수 없습니다. : " + memberId));

        Long parentId = parentDiscussionComment.getId();

        Long topOrder = discussionCommentRepository.findTopOrder(discussionId,
            parentId).orElse(0L) + 1;

        // 부모 댓글의 그룹 아이디
        Long groupIdFromParent = parentDiscussionComment.getGroupId();

        // 부모 댓글에 있는 자식 댓글 중 가장 큰 토탈 아이디를 찾고
        Long topTotalOrderFromChild =
            discussionCommentRepository.findMaxTotalOrderFromChild(groupIdFromParent) + 1;

        // 기존 댓글들의 totalOrder를 한 번의 쿼리로 업데이트
        discussionCommentRepository.incrementTotalOrderFrom(topTotalOrderFromChild);

        DiscussionComment childDiscussionComment = DiscussionComment.builder()
            .discussion(discussion)
            .member(member)
            .parentId(parentId)
            .groupId(groupIdFromParent)
            .isChild(true)
            .groupOrder(topOrder)
            .totalOrder(topTotalOrderFromChild)
            .voteType(voteType)
            .content(discussionCommentRequestDTO.getContent())
            .build();

        discussionCommentRepository.save(childDiscussionComment);

        DiscussionParticipant savedDiscussionParticipant = discussionParticipantRepository.findByDiscussionAndMember(
            discussion, member).orElse(null);

        // VoteType에 따라 isAgree 값을 결정
        boolean newIsAgree = voteType == VoteType.AGREE;

        // 이미 저장된 Participant가 있는 경우, 기존의 isAgree와 비교
        if (savedDiscussionParticipant != null) {

            if (savedDiscussionParticipant.isAgree() == newIsAgree) {

                // 기존의 isAgree 값과 동일하다면 추가 작업을 하지 않음
                return;
            } else {

                discussionParticipantRepository.deleteById(savedDiscussionParticipant.getId());
            }
        }

        // 새로운 isAgree 값인 경우에만 추가
        addParticipant(discussion, member, voteType);
    }

    private void addParticipant(Discussion discussion, Member member, VoteType voteType) {

        DiscussionParticipant discussionParticipant = DiscussionParticipant.builder()
            .discussion(discussion)
            .member(member)
            .isAgree(voteType == VoteType.AGREE)
            .build();

        discussionParticipantRepository.save(discussionParticipant);
    }

    @Override
    public ScrollResponseDTO<DiscussionCommentResponseDTO> getCommentList(
        Long loginMemberId, DiscussionCommentsScrollRequestDTO discussionCommentsScrollRequestDTO,
        Long discussionId) {

        List<DiscussionComment> discussionCommentList = discussionCommentRepository.findDiscussionCommentListWithScroll(
            discussionCommentsScrollRequestDTO, discussionId);

        boolean hasNext = false;
        if (discussionCommentList.size()
            > discussionCommentsScrollRequestDTO.getSize()) { // 11 > 10 이면 있다는 뜻
            hasNext = true;
            discussionCommentList.remove(discussionCommentList.size() - 1); // 11개 가져온 걸 10개를 보내기 위해
        }

        // 결과를 담을 리스트
        List<DiscussionCommentResponseDTO> responseDTOList = new ArrayList<>();

        // 한번에 신고된 댓글 ID들을 가져옴
        Set<Long> reportedCommentIds = discussionCommentRepository.findDiscussionCommentIdsByReporterId(
            loginMemberId);

        for (DiscussionComment discussionComment : discussionCommentList) {

            // Set에서 해당 댓글 ID가 있는지 확인
            boolean isReport = reportedCommentIds.contains(discussionComment.getId());

            // 각 토론 댓글의 관련 정보를 추출

            Long memberId = discussionComment.getMember().getId();
            String profileImage =
                discussionComment.getMember().isDeleted() ? "defaultProfileImage.jpg"
                    : discussionComment.getMember().getProfileImage();
            String nickname =
                discussionComment.getMember().isDeleted() ? "탈퇴한 회원"
                    : discussionComment.getMember().getNickname();
            String honorific =
                discussionComment.getMember().isDeleted() ? "탈퇴한 회원입니다"
                    : discussionComment.getMember().getHonorific();

            Long groupId = discussionComment.getGroupId();
            Long commentId = discussionComment.getId();
            Long parentId = discussionComment.getParentId();

            Optional<DiscussionComment> parentComment = null;

            if (parentId != null) {
                parentComment = discussionCommentRepository.findById(parentId);
            }

            String parentNickname = null;
            if (parentComment != null && parentComment.isPresent()) {
                parentNickname = parentComment.get().getMember().getNickname();
            }

            Long order = discussionComment.getGroupOrder();
            boolean isChild = discussionComment.isChild();
            VoteType voteType = discussionComment.getVoteType();
            String content = discussionComment.getContent();
            long likeCount = discussionComment.getLikes().stream()
                .filter(commentLike -> commentLike.isLike() == true).count();
            long dislikeCount = discussionComment.getLikes().size() - likeCount;
            LocalDateTime createdDate = discussionComment.getCreatedDate();
            Long totalOrder = discussionComment.getTotalOrder();
            ViewStatus viewStatus = discussionComment.getViewStatus();

            // DTO 객체 생성 후 리스트에 추가
            DiscussionCommentResponseDTO dto = DiscussionCommentResponseDTO.builder()
                .commentId(commentId)
                .discussionId(discussionId)
                .memberId(memberId)
                .profileImage(profileImage)
                .nickname(nickname)
                .honorific(honorific)
                .parentId(parentId)
                .parentNickname(parentNickname)
                .groupId(groupId)
                .groupOrder(order)
                .totalOrder(totalOrder)
                .isChild(isChild)
                .voteType(String.valueOf(voteType))
                .content(content)
                .like(likeCount)
                .dislike(dislikeCount)
                .isSelected(false)
                .createTime(createdDate)
                .viewStatus(viewStatus)
                .isReport(isReport)  // 신고 여부 추가
                .build();

            responseDTOList.add(dto);
        }

        // 마지막 항목의 ID 설정
        Long lastId = discussionCommentList.isEmpty() ?
            discussionCommentsScrollRequestDTO.getLastId() :    // 조회된 목록이 비어있는 경우를 대비해 삼항 연산자 사용
            discussionCommentList.get(discussionCommentList.size() - 1)
                .getId(); // lastId를 요청 DTO의 값이 아닌, 실제 조회된 마지막 항목의 ID로 설정

        return ScrollResponseDTO.<DiscussionCommentResponseDTO>withAll()
            .content(responseDTOList)
            .hasNext(hasNext)
            .lastId(lastId) // 조회된 목록의 마지막 항목의 ID ? INDEX ?
            .numberOfElements(responseDTOList.size())
            .totalElements(discussionCommentList.stream().count())
            .build();
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public DiscussionCommentResponseDTO addLike(Long memberId, Long discussionCommentId) {

        DiscussionComment discussionComment = discussionCommentRepository.findById(
            discussionCommentId).orElseThrow(() -> new DiscussionCommentNotFoundException(
            "해당 댓글을 찾을 수 없습니다. : " + discussionCommentId));

        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new MemberNotFoundException("해당 멤버를 찾을 수 없습니다. : " + memberId));

        // 현재 좋아요/싫어요 상태 확인
        CommentLike existingLikeOpt = commentLikeRepository.findByCommentAndMember(
                discussionComment, member)
            .orElse(null);

        if (existingLikeOpt != null) {
            // 한번 더 눌렀는데 기존 상태가 좋아요라면 삭제
            if (existingLikeOpt.isLike()) {

                commentLikeRepository.deleteById(existingLikeOpt.getId());

                return getComment(discussionComment, member);
            }
            // 기존 상태가 싫어요라면 싫어요를 삭제
            commentLikeRepository.delete(existingLikeOpt);
        }

        CommentLike commentLike = CommentLike.builder()
            .comment(discussionComment)
            .member(member)
            .isLike(true)
            .build();

        commentLikeRepository.save(commentLike);

        return getComment(discussionComment, member);
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public DiscussionCommentResponseDTO addDislike(Long memberId, Long discussionCommentId) {

        DiscussionComment discussionComment = discussionCommentRepository.findById(
            discussionCommentId).orElseThrow(() -> new DiscussionCommentNotFoundException(
            "해당 댓글을 찾을 수 없습니다. : " + discussionCommentId));

        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new MemberNotFoundException("해당 멤버를 찾을 수 없습니다. : " + memberId));

        // 현재 좋아요/싫어요 상태 확인
        CommentLike existingLikeOpt = commentLikeRepository.findByCommentAndMember(
                discussionComment, member)
            .orElse(null);

        if (existingLikeOpt != null) {
            // 한번 더 눌렀는데 기존 상태가 싫어요라면 삭제
            if (!existingLikeOpt.isLike()) {

                commentLikeRepository.deleteById(existingLikeOpt.getId());



                return getComment(discussionComment, member);
            }
            commentLikeRepository.deleteById(existingLikeOpt.getId());
        }

        CommentLike commentLike = CommentLike.builder()
            .comment(discussionComment)
            .member(member)
            .isLike(false)
            .build();

        commentLikeRepository.save(commentLike);

        return getComment(discussionComment, member);
    }

    @Override
    public DiscussionCommentResponseDTO getComment(DiscussionComment discussionComment, Member member) {

        Optional<Report> commentReported = reportRepository.findByReporterAndComment(
            member, discussionComment);

        Optional<DiscussionComment> parentComment = discussionCommentRepository.findById(discussionComment.getParentId() != null ? discussionComment.getParentId() : 0L);

        List<CommentLike> commentLikeList = commentLikeRepository.findByComment(discussionComment);

        long likeCount = commentLikeList.stream()
            .filter(commentLike -> commentLike.isLike() == true).count();
        long dislikeCount = commentLikeList.size() - likeCount;

        DiscussionCommentResponseDTO discussionCommentResponseDTO = DiscussionCommentResponseDTO.builder()
            .commentId(discussionComment.getId())
            .discussionId(discussionComment.getDiscussion().getId())
            .memberId(discussionComment.getMember().getId())
            .profileImage(discussionComment.getMember().getProfileImage())
            .nickname(discussionComment.getMember().getNickname())
            .parentNickname(
                parentComment.map(comment -> comment.getMember().getNickname()).orElse(null))
            .honorific(discussionComment.getMember().getHonorific())
            .parentId(parentComment.map(DiscussionComment::getId).orElse(null))
            .groupId(discussionComment.getGroupId() != null ? discussionComment.getGroupId() : null)
            .groupOrder(discussionComment.getGroupOrder() != null ? discussionComment.getGroupOrder() : null)
            .totalOrder(discussionComment.getTotalOrder())
            .isChild(discussionComment.isChild())
            .voteType(String.valueOf(discussionComment.getVoteType()))
            .content(discussionComment.getContent())
            .like(likeCount)
            .dislike(dislikeCount)
            .createTime(discussionComment.getCreatedDate())
            .viewStatus(discussionComment.getViewStatus())
            .isReport(commentReported.isPresent())
            .build();

        return discussionCommentResponseDTO;
    }

    @Override
    public void deleteComment(Long memberId, Long commentId) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new MemberNotFoundException("해당 멤버를 찾을 수 없습니다. : " + memberId));

        DiscussionComment discussionComment = discussionCommentRepository.findById(commentId)
            .orElseThrow(
                () -> new DiscussionCommentNotFoundException("해당 댓글을 찾을 수 없습니다 : " + commentId));

        Long commentMemberId = discussionComment.getMember().getId();

        // 삭제하려는 댓글의 작성자가 해당 멤버인지 확인
        if (Objects.equals(member.getId(), commentMemberId)) {
            // 삭제된 댓글의 voteType (찬성/반대)
            VoteType voteType = discussionComment.getVoteType();

            // 댓글 삭제
            discussionCommentRepository.deleteById(commentId);

            // 삭제된 댓글이 찬성 댓글인지 반대 댓글인지 확인 후 해당 카운트 감소
            if (voteType == VoteType.AGREE) {

                // 찬성 댓글 삭제 시 마지막 찬성 댓글인지 확인
                long countAgreeComments = countCommentsForDiscussion(
                    discussionComment.getDiscussion().getId(), VoteType.AGREE);

                if (countAgreeComments == 0) {  // 마지막 찬성 댓글이 삭제되었으면

                    // 찬성 상태였던 참여자 삭제
                    DiscussionParticipant participant = findParticipantByDiscussionAndMember(
                        discussionComment.getDiscussion(), member);
                    discussionParticipantRepository.delete(participant); // 해당 찬성 상태 삭제
                }

            } else if (voteType == VoteType.DISAGREE) {

                // 반대 댓글 삭제 시 마지막 반대 댓글인지 확인
                long countDisagreeComments = countCommentsForDiscussion(
                    discussionComment.getDiscussion().getId(), VoteType.DISAGREE);

                if (countDisagreeComments == 0) {  // 마지막 반대 댓글이 삭제되었으면

                    // 반대 상태였던 참여자 삭제
                    DiscussionParticipant participant = findParticipantByDiscussionAndMember(
                        discussionComment.getDiscussion(), member);
                    discussionParticipantRepository.delete(participant); // 해당 반대 상태 삭제
                }
            }
        }
    }

    private long countCommentsForDiscussion(Long discussionId, VoteType voteType) {
        return discussionCommentRepository.countCommentsForDiscussionAndVoteType(discussionId,
            voteType);
    }

    private DiscussionParticipant findParticipantByDiscussionAndMember(Discussion discussion,
        Member member) {
        return discussionParticipantRepository.findByDiscussionAndMember(discussion, member)
            .orElseThrow(() -> new DiscussionParticipantNotFoundException("해당 참여자를 찾을 수 없습니다."));
    }

    @Override
    public List<DiscussionCommentResponseDTO> getBest3CommentByCommentLikes(Long loginMemberId,
        Long discussionId) {

        List<DiscussionComment> bestCommentTop3List = discussionCommentRepository.findBest3CommentList(
            discussionId).orElseThrow(
            () -> new DiscussionCommentNotFoundException("댓글을 찾을 수 없습니다.")
        );

        // 결과를 담을 리스트
        List<DiscussionCommentResponseDTO> responseDTOList = new ArrayList<>();

        // 한번에 신고된 댓글 ID들을 가져옴
        Set<Long> reportedCommentIds = discussionCommentRepository.findDiscussionCommentIdsByReporterId(
            loginMemberId);

        for (DiscussionComment discussionComment : bestCommentTop3List) {
            // 각 토론 댓글의 관련 정보를 추출

            // Set에서 해당 댓글 ID가 있는지 확인
            boolean isReport = reportedCommentIds.contains(discussionComment.getId());

            Long memberId = discussionComment.getMember().getId();
            String profileImage =
                discussionComment.getMember().isDeleted() ? "defaultProfileImage.jpg"
                    : discussionComment.getMember().getProfileImage();
            String nickname =
                discussionComment.getMember().isDeleted() ? "탈퇴한 회원"
                    : discussionComment.getMember().getNickname();
            String honorific =
                discussionComment.getMember().isDeleted() ? "탈퇴한 회원입니다" :
                    discussionComment.getMember().getHonorific();

            Long commentId = discussionComment.getId();
            Long parentId = discussionComment.getParentId();
            Long order = discussionComment.getGroupOrder();
            boolean isChild = discussionComment.isChild();
            VoteType voteType = discussionComment.getVoteType();
            String content = discussionComment.getContent();
            long likeCount = discussionComment.getLikes().stream()
                .filter(commentLike -> commentLike.isLike() == true).count();
            long dislikeCount = discussionComment.getLikes().size() - likeCount;
            LocalDateTime createdDate = discussionComment.getCreatedDate();
            Long totalOrder = discussionComment.getTotalOrder();
            ViewStatus viewStatus = discussionComment.getViewStatus();

            // DTO 객체 생성 후 리스트에 추가
            DiscussionCommentResponseDTO dto = DiscussionCommentResponseDTO.builder()
                .commentId(commentId)
                .discussionId(discussionId)
                .memberId(memberId)
                .profileImage(profileImage)
                .nickname(nickname)
                .honorific(honorific)
                .parentId(parentId)
                .groupOrder(order)
                .totalOrder(totalOrder)
                .isChild(isChild)
                .voteType(String.valueOf(voteType))
                .content(content)
                .like(likeCount)
                .dislike(dislikeCount)
                .isSelected(true)
                .createTime(createdDate)
                .viewStatus(viewStatus)
                .isReport(isReport)
                .build();

            responseDTOList.add(dto);
        }

        return responseDTOList;
    }


}
