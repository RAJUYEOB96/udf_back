package com.undefinedus.backend.service;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.undefinedus.backend.domain.entity.CommentLike;
import com.undefinedus.backend.domain.entity.Discussion;
import com.undefinedus.backend.domain.entity.DiscussionComment;
import com.undefinedus.backend.domain.entity.DiscussionParticipant;
import com.undefinedus.backend.domain.entity.Member;
import com.undefinedus.backend.domain.enums.VoteType;
import com.undefinedus.backend.dto.request.DiscussionCommentsScrollRequestDTO;
import com.undefinedus.backend.dto.response.ScrollResponseDTO;
import com.undefinedus.backend.exception.discussion.DiscussionNotFoundException;
import com.undefinedus.backend.exception.discussionComment.DiscussionCommentNotFoundException;
import com.undefinedus.backend.exception.member.MemberNotFoundException;
import com.undefinedus.backend.repository.CommentLikeRepository;
import com.undefinedus.backend.repository.DiscussionCommentRepository;
import com.undefinedus.backend.repository.DiscussionParticipantRepository;
import com.undefinedus.backend.repository.DiscussionRepository;
import com.undefinedus.backend.repository.MemberRepository;
import com.undefinedus.backend.dto.request.discussionComment.DiscussionCommentRequestDTO;
import com.undefinedus.backend.dto.response.discussionComment.DiscussionCommentListResponseDTO;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class DiscussionCommentServiceImplTest {

    @Mock
    private DiscussionRepository discussionRepository;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private DiscussionCommentRepository discussionCommentRepository;
    @Mock
    private CommentLikeRepository commentLikeRepository;
    @Mock
    private DiscussionParticipantRepository discussionParticipantRepository;

    @InjectMocks
    private DiscussionCommentServiceImpl discussionCommentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("토론에 댓글을 작성하는 테스트")
    void testWriteComment() {
        Long discussionId = 1L;
        Long memberId = 1L;

        DiscussionCommentRequestDTO requestDTO = DiscussionCommentRequestDTO.builder()
            .voteType(String.valueOf(VoteType.DISAGREE))
            .content("Test Comment")
            .build();

        Discussion discussion = new Discussion();
        Member member = new Member();

        // Mocking the repository methods
        when(discussionRepository.findById(discussionId)).thenReturn(Optional.of(discussion));
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(discussionCommentRepository.findMaxGroupId()).thenReturn(1L);
        when(discussionCommentRepository.findTopTotalOrder(discussionId)).thenReturn(Optional.of(1L));
        when(discussionParticipantRepository.findByDiscussionAndMember(discussion, member)) // Mocking the call to discussionParticipantRepository
            .thenReturn(Optional.of(new DiscussionParticipant())); // 필요한 리턴값 설정

        discussionCommentService.writeComment(discussionId, memberId, requestDTO);

        verify(discussionCommentRepository, times(1)).save(any(DiscussionComment.class));
    }

    @Test
    @DisplayName("댓글에 대한 답글을 작성하는 테스트")
    void testWriteReply() {
        Long discussionId = 1L;
        Long discussionCommentId = 2L;
        Long memberId = 3L;

        DiscussionCommentRequestDTO requestDTO = DiscussionCommentRequestDTO.builder()
            .voteType(String.valueOf(VoteType.AGREE))
            .content("Test Reply")
            .build();

        Discussion discussion = new Discussion();
        discussion.changeId(discussionId);

        DiscussionComment parentComment = new DiscussionComment();
        parentComment.changeId(discussionCommentId);
        parentComment.changeGroupId(1L);
        parentComment.changeDiscussion(discussion);

        Member member = new Member();

        when(discussionRepository.findById(discussionId)).thenReturn(Optional.of(discussion));
        when(discussionCommentRepository.findById(discussionCommentId)).thenReturn(Optional.of(parentComment));
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(discussionCommentRepository.findTopOrder(anyLong(), anyLong())).thenReturn(Optional.of(1L));
        when(discussionCommentRepository.findMaxTotalOrderFromChild(anyLong())).thenReturn(1L);

        discussionCommentService.writeReply(discussionId, discussionCommentId, memberId, requestDTO);

        verify(discussionCommentRepository, times(1)).save(any(DiscussionComment.class));
        verify(discussionCommentRepository, times(1)).incrementTotalOrderFrom(anyLong());
    }

    @Test
    @DisplayName("댓글 목록을 스크롤 방식으로 조회하는 테스트")
    void testGetCommentList() {
        DiscussionCommentsScrollRequestDTO requestDTO = new DiscussionCommentsScrollRequestDTO();
        requestDTO.setSize(10);
        requestDTO.setLastId(0L);

        Member member = new Member();
        member.setId(1L); // 멤버 ID 설정

        Discussion discussion = new Discussion();
        discussion.changeId(1L); // 토론 ID 설정

        DiscussionComment comment1 = new DiscussionComment();
        comment1.changeId(1L);
        comment1.changeDiscussion(discussion);
        comment1.changeMember(member); // 멤버 설정
        comment1.changeVoteType(VoteType.AGREE);
        comment1.changeContent("Test Comment");
        comment1.setCreatedDate(LocalDateTime.now());

        List<DiscussionComment> commentList = Arrays.asList(comment1);

        when(discussionCommentRepository.findDiscussionCommentListWithScroll(any())).thenReturn(commentList);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member)); // 정확한 멤버 ID로 설정

        ScrollResponseDTO<DiscussionCommentListResponseDTO> result = discussionCommentService.getCommentList(requestDTO);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertFalse(result.isHasNext());
    }

    @Test
    @DisplayName("댓글에 좋아요를 추가하는 테스트")
    void testAddLike() {
        Long memberId = 1L;
        Long discussionCommentId = 2L;

        DiscussionComment comment = new DiscussionComment();
        Member member = new Member();

        when(discussionCommentRepository.findById(discussionCommentId)).thenReturn(Optional.of(comment));
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));

        discussionCommentService.addLike(memberId, discussionCommentId);

        verify(commentLikeRepository, times(1)).save(any(CommentLike.class));
    }

    @Test
    @DisplayName("댓글에 싫어요를 추가하는 테스트")
    void testAddDislike() {
        Long memberId = 1L;
        Long discussionCommentId = 2L;

        DiscussionComment comment = new DiscussionComment();
        Member member = new Member();

        when(discussionCommentRepository.findById(discussionCommentId)).thenReturn(Optional.of(comment));
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));

        discussionCommentService.addDislike(memberId, discussionCommentId);

        verify(commentLikeRepository, times(1)).save(any(CommentLike.class));
    }

    @Test
    @DisplayName("잘못된 토론 ID로 답글 작성 시 예외가 발생하는 테스트")
    void testWriteReplyWithInvalidDiscussionId() {
        Long invalidDiscussionId = 999L;
        when(discussionRepository.findById(invalidDiscussionId)).thenReturn(Optional.empty());

        DiscussionCommentRequestDTO requestDTO = DiscussionCommentRequestDTO.builder()
            .voteType(String.valueOf(VoteType.AGREE))
            .content("Test Content")
            .build();

        assertThrows(DiscussionNotFoundException.class, () ->
            discussionCommentService.writeReply(invalidDiscussionId, 1L, 1L, requestDTO));
    }

    @Test
    @DisplayName("잘못된 댓글 ID로 답글 작성 시 예외가 발생하는 테스트")
    void testWriteReplyWithInvalidCommentId() {
        Long invalidCommentId = 999L;
        when(discussionRepository.findById(anyLong())).thenReturn(Optional.of(new Discussion()));
        when(discussionCommentRepository.findById(invalidCommentId)).thenReturn(Optional.empty());

        DiscussionCommentRequestDTO requestDTO = DiscussionCommentRequestDTO.builder()
            .voteType(String.valueOf(VoteType.AGREE))
            .content("Test Content")
            .build();

        assertThrows(DiscussionCommentNotFoundException.class, () ->
            discussionCommentService.writeReply(1L, invalidCommentId, 1L, requestDTO));
    }

    @Test
    @DisplayName("잘못된 멤버 ID로 답글 작성 시 예외가 발생하는 테스트")
    void testWriteReplyWithInvalidMemberId() {
        Long invalidMemberId = 999L;
        when(discussionRepository.findById(anyLong())).thenReturn(Optional.of(new Discussion()));
        when(discussionCommentRepository.findById(anyLong())).thenReturn(Optional.of(new DiscussionComment()));
        when(memberRepository.findById(invalidMemberId)).thenReturn(Optional.empty());

        DiscussionCommentRequestDTO requestDTO = DiscussionCommentRequestDTO.builder()
            .voteType(String.valueOf(VoteType.AGREE))
            .content("Test Content")
            .build();

        assertThrows(MemberNotFoundException.class, () ->
            discussionCommentService.writeReply(1L, 1L, invalidMemberId, requestDTO));
    }

    @Test
    @DisplayName("댓글 삭제 테스트")
    void testDeleteComment() {
        Long memberId = 1L;
        Long commentId = 2L;

        // 테스트용 멤버와 댓글 객체 생성
        Member member = Member.builder()
            .id(memberId)
            .build();

        DiscussionComment discussionComment = DiscussionComment.builder()
            .id(commentId)
            .member(member) // 해당 댓글의 작성자를 멤버로 설정
            .build();

        // Mocking
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(discussionCommentRepository.findById(commentId)).thenReturn(Optional.of(discussionComment));

        // 메서드 실행
        discussionCommentService.deleteComment(memberId, commentId);

        // 검증
        verify(discussionCommentRepository, times(1)).deleteById(commentId);
    }


    @Test
    @DisplayName("마지막 반대 댓글 삭제 시 참여자 삭제 테스트")
    void testDeleteDisagreeCommentWhenLastDisagree() {
        Long memberId = 1L;
        Long commentId = 3L;

        // 테스트용 멤버와 댓글 객체 생성
        Member member = Member.builder()
            .id(memberId)
            .build();

        Discussion discussion = new Discussion();
        discussion.changeId(1L); // 토론 ID 설정

        DiscussionComment discussionComment = DiscussionComment.builder()
            .id(commentId)
            .member(member) // 해당 댓글의 작성자를 멤버로 설정
            .discussion(discussion) // 댓글이 속한 토론 설정
            .voteType(VoteType.DISAGREE) // 반대 댓글로 설정
            .build();

        // 댓글 삭제 후 마지막 반대 댓글인지 확인하는 로직을 위해 카운트도 mock
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(discussionCommentRepository.findById(commentId)).thenReturn(Optional.of(discussionComment));
        when(discussionCommentRepository.countCommentsForDiscussionAndVoteType(discussion.getId(), VoteType.DISAGREE)).thenReturn(0L); // 마지막 반대 댓글

        // 빌더 패턴을 사용하여 참여자 객체 생성
        DiscussionParticipant participant = DiscussionParticipant.builder()
            .discussion(discussion)
            .member(member)
            .isAgree(false) // 반대 설정
            .build();

        when(discussionParticipantRepository.findByDiscussionAndMember(discussion, member))
            .thenReturn(Optional.of(participant));

        // 댓글 삭제 메서드 실행
        discussionCommentService.deleteComment(memberId, commentId);

        // 댓글 삭제 확인
        verify(discussionCommentRepository, times(1)).deleteById(commentId);

        // 반대 상태인 참여자 삭제 확인
        verify(discussionParticipantRepository, times(1)).delete(participant);
    }
}