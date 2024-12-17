package com.undefinedus.backend.service;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.undefinedus.backend.domain.entity.CommentLike;
import com.undefinedus.backend.domain.entity.Discussion;
import com.undefinedus.backend.domain.entity.DiscussionComment;
import com.undefinedus.backend.domain.entity.DiscussionParticipant;
import com.undefinedus.backend.domain.entity.Member;
import com.undefinedus.backend.domain.enums.VoteType;
import com.undefinedus.backend.dto.request.discussionComment.DiscussionCommentsScrollRequestDTO;
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
import com.undefinedus.backend.dto.response.discussionComment.DiscussionCommentResponseDTO;
import com.undefinedus.backend.repository.ReportRepository;
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
    @Mock
    private ReportRepository reportRepository;

    @InjectMocks
    private DiscussionCommentServiceImpl discussionCommentService;

    Member member1;
    Member member2;
    Discussion discussion;
    DiscussionComment comment1;
    DiscussionComment comment2;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Member 객체 생성
        member1 = Member.builder()
            .id(1L)
            .username("user1@example.com")
            .nickname("User1")
            .build();
        member2 = Member.builder()
            .id(2L)
            .username("user2@example.com")
            .nickname("User2")
            .build();

        // Discussion 객체 생성
        discussion = Discussion.builder()
            .id(1L)
            .title("Test Discussion")
            .content("This is a test discussion")
            .member(member1)
            .build();

        // DiscussionComment 객체 생성
        comment1 = DiscussionComment.builder()
            .id(1L)
            .discussion(discussion)
            .member(member1)
            .content("Comment 1")
            .voteType(VoteType.AGREE)
            .build();
        comment2 = DiscussionComment.builder()
            .id(2L)
            .discussion(discussion)
            .member(member2)
            .isChild(true)
            .parentId(1L)
            .content("Comment 2")
            .voteType(VoteType.DISAGREE)
            .build();

        // CommentLike 객체 생성
        CommentLike like1 = CommentLike.builder()
            .id(1L)
            .comment(comment1)
            .member(member2)
            .isLike(true)
            .build();
        CommentLike like2 = CommentLike.builder()
            .id(2L)
            .comment(comment2)
            .member(member1)
            .isLike(false)
            .build();

        // Repository mocking
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member1));
        when(memberRepository.findById(2L)).thenReturn(Optional.of(member2));
        when(discussionRepository.findById(1L)).thenReturn(Optional.of(discussion));
        when(discussionCommentRepository.findById(1L)).thenReturn(Optional.of(comment1));
        when(discussionCommentRepository.findById(2L)).thenReturn(Optional.of(comment2));
        when(commentLikeRepository.findByCommentAndMember(comment1, member2)).thenReturn(Optional.of(like1));
        when(commentLikeRepository.findByCommentAndMember(comment2, member1)).thenReturn(Optional.of(like2));
    }

    @Test
    @DisplayName("댓글 작성 테스트")
    void testWriteComment() {
        // Given
        Long discussionId = 1L;
        Long memberId = 1L;
        DiscussionCommentRequestDTO requestDTO = DiscussionCommentRequestDTO.builder()
            .voteType(String.valueOf(VoteType.AGREE))
            .content("테스트 댓글")
            .build();

        Discussion discussion = Discussion.builder()
            .id(discussionId)
            .build();
        Member member = Member.builder()
            .id(memberId)
            .build();
        DiscussionComment savedComment = DiscussionComment.builder()
            .id(1L)
            .discussion(discussion)
            .member(member)
            .voteType(VoteType.AGREE)
            .content("테스트 댓글")
            .build();

        when(discussionRepository.findById(discussionId)).thenReturn(Optional.of(discussion));
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(discussionCommentRepository.findTopTotalOrder(discussionId)).thenReturn(Optional.of(0L));
        when(discussionCommentRepository.save(any(DiscussionComment.class))).thenReturn(savedComment);

        // When
        discussionCommentService.writeComment(discussionId, memberId, requestDTO);

        // Then
        verify(discussionRepository).findById(discussionId);
        verify(memberRepository).findById(memberId);
        verify(discussionCommentRepository).findTopTotalOrder(discussionId);
        verify(discussionCommentRepository, times(1)).save(any(DiscussionComment.class));
        verify(discussionParticipantRepository).findByDiscussionAndMember(discussion, member);
        verify(discussionParticipantRepository).save(any(DiscussionParticipant.class));
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
        // Given
        DiscussionCommentsScrollRequestDTO requestDTO = new DiscussionCommentsScrollRequestDTO();
        requestDTO.setSize(10);
        requestDTO.setLastId(0L);
        
        Long discussionId = 42L;
        
        Member member = new Member();
        member.setId(1L);
        
        Discussion discussion = new Discussion();
        discussion.changeId(1L);

        List<DiscussionComment> commentList = Arrays.asList(comment2);
        
        // When
        when(discussionCommentRepository.findDiscussionCommentListWithScroll(any(DiscussionCommentsScrollRequestDTO.class), eq(discussionId)))
                .thenReturn(commentList);
        when(memberRepository.findById(eq(1L))).thenReturn(Optional.of(member));
        
        ScrollResponseDTO<DiscussionCommentResponseDTO> result =
                discussionCommentService.getCommentList(member.getId(), requestDTO, discussionId);

        System.out.println("result = " + result);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertFalse(result.isHasNext());
    }

    @Test
    @DisplayName("댓글에 좋아요를 추가하는 테스트")
    void testAddLike() {
        // given
        Long memberId = member1.getId(); // @BeforeEach에서 생성된 member1 사용
        Long discussionCommentId = comment2.getId(); // @BeforeEach에서 생성된 comment2 사용

        // Mocking 중복 확인 (새로운 좋아요는 없다고 가정)
        when(commentLikeRepository.findByCommentAndMember(comment2, member1)).thenReturn(Optional.empty());

        // when
        discussionCommentService.addLike(memberId, discussionCommentId);

        // then
        verify(commentLikeRepository, times(1)).save(any(CommentLike.class)); // 새로운 좋아요가 저장되는지 확인
    }

    @Test
    @DisplayName("댓글에 싫어요를 추가하는 테스트")
    void testAddDislike() {
        // given
        Long memberId = member1.getId(); // @BeforeEach에서 생성된 member1 사용
        Long discussionCommentId = comment2.getId(); // @BeforeEach에서 생성된 comment2 사용

        // Mocking 중복 확인 (새로운 좋아요는 없다고 가정)
        when(commentLikeRepository.findByCommentAndMember(comment2, member1)).thenReturn(Optional.empty());

        // when
        discussionCommentService.addDislike(memberId, discussionCommentId);

        // then
        verify(commentLikeRepository, times(1)).save(any(CommentLike.class)); // 새로운 좋아요가 저장되는지 확인
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

    @Test
    @DisplayName("댓글 좋아요 수가 많은 상위 3개의 댓글을 조회하는 테스트")
    void testGetTop3CommentByLikes() {
        // Given
        DiscussionComment comment1 = DiscussionComment.builder()
            .id(1L)
            .discussion(discussion) // 적절한 토론 객체를 설정
            .member(member1) // 적절한 멤버 객체를 설정
            .content("댓글 333")
            .likes(Arrays.asList(
                CommentLike.builder().isLike(true).build(),  // 좋아요
                CommentLike.builder().isLike(true).build(),  // 좋아요
                CommentLike.builder().isLike(false).build()  // 싫어요
            ))
            .build();

        DiscussionComment comment2 = DiscussionComment.builder()
            .id(2L)
            .discussion(discussion) // 적절한 토론 객체를 설정
            .member(member1) // 적절한 멤버 객체를 설정
            .content("댓글 2")
            .likes(Arrays.asList(
                CommentLike.builder().isLike(true).build(),  // 좋아요
                CommentLike.builder().isLike(false).build(), // 싫어요
                CommentLike.builder().isLike(false).build()  // 싫어요
            ))
            .build();

        DiscussionComment comment3 = DiscussionComment.builder()
            .id(3L)
            .discussion(discussion) // 적절한 토론 객체를 설정
            .member(member2) // 적절한 멤버 객체를 설정
            .content("댓글 3")
            .likes(Arrays.asList(
                CommentLike.builder().isLike(true).build(),  // 좋아요
                CommentLike.builder().isLike(true).build(),   // 좋아요
                CommentLike.builder().isLike(true).build(),   // 좋아요
                CommentLike.builder().isLike(true).build()    // 좋아요
            ))
            .build();
        
        Long loginMemberId = 2L;

        List<DiscussionComment> bestCommentTop3List = Arrays.asList(comment3, comment1, comment2);

        when(discussionCommentRepository.findBest3CommentList(discussion.getId())).thenReturn(
            Optional.of(bestCommentTop3List));

        List<DiscussionCommentResponseDTO> results =
                discussionCommentService.getBest3CommentByCommentLikes(loginMemberId, discussion.getId());
        for (DiscussionCommentResponseDTO dto : results) {
            System.out.println("dto = " + dto);
        }
    }
}
