package com.undefinedus.backend.service;

import com.undefinedus.backend.domain.entity.CommentLike;
import com.undefinedus.backend.domain.entity.Discussion;
import com.undefinedus.backend.domain.entity.DiscussionComment;
import com.undefinedus.backend.domain.entity.DiscussionParticipant;
import com.undefinedus.backend.domain.entity.Member;
import com.undefinedus.backend.domain.entity.Report;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
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
    public Map<String, Object> writeComment(Long discussionId, Long memberId,
        DiscussionCommentRequestDTO discussionCommentRequestDTO) {

        Map<String, Object> result = new HashMap<>();

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

        DiscussionComment savedComment = discussionCommentRepository.save(discussionComment);

        DiscussionCommentResponseDTO commentDTO = getCommentDTO(savedComment, member);

        List<DiscussionComment> discussionCommentList = discussionCommentRepository.findByDiscussion(
            discussionComment.getDiscussion());

        long commentCount = discussionCommentList.stream().count();

        result.put("content", commentDTO);
        result.put("commentCount", commentCount);

        return result;
    }

    // 답글 달기
    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public Map<String, Object> writeReply(Long discussionId, Long discussionCommentId,
        Long memberId,
        DiscussionCommentRequestDTO discussionCommentRequestDTO) {

        Map<String, Object> result = new HashMap<>();

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

        DiscussionComment savedComment = discussionCommentRepository.save(childDiscussionComment);

        DiscussionCommentResponseDTO commentDTO = getCommentDTO(savedComment, member);

        List<DiscussionComment> discussionCommentList = discussionCommentRepository.findByDiscussion(
            childDiscussionComment.getDiscussion());

        long commentCount = discussionCommentList.stream().count();

        result.put("content", commentDTO);
        result.put("commentCount", commentCount);

        return result;
    }

    @Override
    public ScrollResponseDTO<DiscussionCommentResponseDTO> getCommentList(
        Long loginMemberId, DiscussionCommentsScrollRequestDTO discussionCommentsScrollRequestDTO,
        Long discussionId) {

        List<DiscussionComment> discussionCommentList = discussionCommentRepository.findDiscussionCommentListWithScroll(
            discussionCommentsScrollRequestDTO, discussionId);

        Discussion discussion = discussionRepository.findById(discussionId).orElseThrow(
            () -> new DiscussionNotFoundException("해당 토론을 찾지 못했습니다. : " + discussionId));

        long discussionCommentTotal = discussionCommentRepository.findByDiscussion(
            discussion).stream().count();

        Member member = memberRepository.findById(loginMemberId)
            .orElseThrow(() -> new MemberNotFoundException("해당 회원을 찾을 수 없습니다 : " + loginMemberId));

        boolean hasNext = false;
        if (discussionCommentList.size()
            > discussionCommentsScrollRequestDTO.getSize()) { // 11 > 10 이면 있다는 뜻
            hasNext = true;
            discussionCommentList.remove(discussionCommentList.size() - 1); // 11개 가져온 걸 10개를 보내기 위해
        }

        // 결과를 담을 리스트
        List<DiscussionCommentResponseDTO> responseDTOList = new ArrayList<>();

        for (DiscussionComment discussionComment : discussionCommentList) {

            DiscussionCommentResponseDTO commentResponseDTO = getCommentDTO(discussionComment,
                member);

            responseDTOList.add(commentResponseDTO);
        }

        // 마지막 항목의 ID 설정
        Long lastId = discussionCommentList.isEmpty() ?
            discussionCommentsScrollRequestDTO.getLastId() :    // 조회된 목록이 비어있는 경우를 대비해 삼항 연산자 사용
            discussionCommentList.get(discussionCommentList.size() - 1)
                .getTotalOrder(); // lastId를 요청 DTO의 값이 아닌, 실제 조회된 마지막 항목의 ID로 설정

        return ScrollResponseDTO.<DiscussionCommentResponseDTO>withAll()
            .content(responseDTOList)
            .hasNext(hasNext)
            .lastId(lastId) // 조회된 목록의 마지막 항목의 ID ? INDEX ?
            .numberOfElements(responseDTOList.size())
            .totalElements(discussionCommentTotal)
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

                return getCommentDTO(discussionComment, member);
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

        return getCommentDTO(discussionComment, member);
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

                return getCommentDTO(discussionComment, member);
            }
            commentLikeRepository.deleteById(existingLikeOpt.getId());
        }

        CommentLike commentLike = CommentLike.builder()
            .comment(discussionComment)
            .member(member)
            .isLike(false)
            .build();

        commentLikeRepository.save(commentLike);

        return getCommentDTO(discussionComment, member);
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

        Member member = memberRepository.findById(loginMemberId)
            .orElseThrow(() -> new MemberNotFoundException("해당 회원을 찾을 수 없습니다 : " + loginMemberId));

        // 결과를 담을 리스트
        List<DiscussionCommentResponseDTO> responseDTOList = new ArrayList<>();

        for (DiscussionComment discussionComment : bestCommentTop3List) {

            DiscussionCommentResponseDTO commentDTO = getCommentDTO(discussionComment, member);

            responseDTOList.add(commentDTO);
        }

        return responseDTOList;
    }

    @Override
    public DiscussionCommentResponseDTO getCommentDTO(DiscussionComment discussionComment,
        Member member) {

        String profileImage =
            discussionComment.getMember().isDeleted() ? "defaultProfileImage.jpg"
                : discussionComment.getMember().getProfileImage();
        String nickname =
            discussionComment.getMember().isDeleted() ? "탈퇴한 회원"
                : discussionComment.getMember().getNickname();
        String honorific =
            discussionComment.getMember().isDeleted() ? "탈퇴한 회원입니다"
                : discussionComment.getMember().getHonorific();

        Optional<Report> commentReported = reportRepository.findByReporterAndComment(
            member, discussionComment);

        Optional<CommentLike> commentIsLike = commentLikeRepository.findByCommentAndMember(
            discussionComment, member);

        if (commentIsLike.isPresent()) {
            log.info("isLike : " + commentIsLike.get().isLike());
        }

        String parentCommentNickname;

        Optional<DiscussionComment> parentComment = discussionCommentRepository.findById(
            discussionComment.getParentId() != null ? discussionComment.getParentId() : 0L);

        if (parentComment.isPresent()) {

            if (parentComment.get().getMember().isDeleted()) {

                parentCommentNickname = "탈퇴한 회원";
            } else {

                parentCommentNickname = parentComment.get().getMember().getNickname();
            }
        } else {

            parentCommentNickname = null;
        }

        List<CommentLike> commentLikeList = commentLikeRepository.findByComment(discussionComment);

        long likeCount = commentLikeList.stream()
            .filter(commentLike -> commentLike.isLike() == true).count();
        long dislikeCount = commentLikeList.size() - likeCount;

        DiscussionCommentResponseDTO discussionCommentResponseDTO = DiscussionCommentResponseDTO.builder()
            .commentId(discussionComment.getId())
            .discussionId(discussionComment.getDiscussion().getId())
            .memberId(discussionComment.getMember().getId())
            .profileImage(profileImage)
            .nickname(nickname)
            .parentNickname(parentCommentNickname)
            .honorific(honorific)
            .parentId(parentComment.map(DiscussionComment::getId).orElse(null))
            .groupId(discussionComment.getGroupId() != null ? discussionComment.getGroupId() : null)
            .groupOrder(
                discussionComment.getGroupOrder() != null ? discussionComment.getGroupOrder()
                    : null)
            .totalOrder(discussionComment.getTotalOrder())
            .isChild(discussionComment.isChild())
            .isLike(commentIsLike.isPresent() ? commentIsLike.get().isLike() : null)
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
}
