package com.undefinedus.backend.repository;

import com.undefinedus.backend.domain.entity.AladinBook;
import com.undefinedus.backend.domain.entity.CommentLike;
import com.undefinedus.backend.domain.entity.Discussion;
import com.undefinedus.backend.domain.entity.DiscussionComment;
import com.undefinedus.backend.domain.entity.Member;
import com.undefinedus.backend.domain.entity.MyBook;
import com.undefinedus.backend.domain.enums.BookStatus;
import com.undefinedus.backend.domain.enums.DiscussionStatus;
import com.undefinedus.backend.domain.enums.VoteType;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hibernate.validator.internal.util.Contracts.assertNotNull;

@SpringBootTest
@Transactional
class DiscussionCommentRepositoryTest {

    @Autowired
    private DiscussionCommentRepository discussionCommentRepository;

    @Autowired
    private DiscussionRepository discussionRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MyBookRepository myBookRepository;
    
    @Autowired
    private AladinBookRepository aladinBookRepository; // 추가

    @Autowired
    private CommentLikeRepository commentLikeRepository;

    @Autowired
    private EntityManager entityManager;

    private Discussion discussion;
    private AladinBook aladinBook; // 추가
    
    Member member1;
    Member member2;
    Member member3;
    Member member4;
    Member member5;
    private DiscussionComment comment1;
    private DiscussionComment comment2;
    private DiscussionComment comment3;

    @BeforeEach
    void setUp() {

        // 댓글 테이블 초기화
        discussionCommentRepository.deleteAll();  // 모든 댓글 삭제

        // 여러 멤버 생성 및 저장
        member1 = Member.builder()
            .nickname("testuser1")
            .username("test1@example.com")
            .password("testpassword1")
            .build();
        memberRepository.save(member1);

        member2 = Member.builder()
            .nickname("testuser2")
            .username("test2@example.com")
            .password("testpassword2")
            .build();
        memberRepository.save(member2);

        member3 = Member.builder()
            .nickname("testuser3")
            .username("test3@example.com")
            .password("testpassword3")
            .build();
        memberRepository.save(member3);

        member4 = Member.builder()
            .nickname("testuser4")
            .username("test4@example.com")
            .password("testpassword4")
            .build();
        memberRepository.save(member4);

        member5 = Member.builder()
            .nickname("testuser5")
            .username("test5@example.com")
            .password("testpassword5")
            .build();
        memberRepository.save(member5);
        
        
        // AladinBook 생성 및 저장 (추가)
        aladinBook = AladinBook.builder()
                .isbn13("9780123456789")
                .title("Test Book")
                .author("Test Author")
                .link("test test")
                .cover("testet")
                .fullDescription("1111")
                .fullDescription2("2222")
                .publisher("3333")
                .categoryName("dsfasf")
                .customerReviewRank(1.1)
                .build();
        aladinBookRepository.save(aladinBook);

        // MyBook 생성 및 저장
        MyBook myBook = MyBook.builder()
            .member(member1)
            .status(BookStatus.READING)  // 적절한 BookStatus 설정
            .isbn13("9780123456789")  // 예시 ISBN
            .aladinBook(aladinBook) // 추가
            .build();
        myBookRepository.save(myBook);

        // Discussion 생성 및 저장
        discussion = Discussion.builder()
            .member(member1)
            .aladinBook(aladinBook) // 추가
            .title("Test Discussion")
            .content("Discussion Content")
            .status(DiscussionStatus.PROPOSED)
            .build();
        discussionRepository.save(discussion);

        // 댓글을 여러 개 추가
        comment1 = DiscussionComment.builder()
            .discussion(discussion)
            .member(member1)
            .voteType(VoteType.AGREE)
            .content("Best Comment with many likes")
            .build();
        discussionCommentRepository.save(comment1);

        comment2 = DiscussionComment.builder()
            .discussion(discussion)
            .member(member2)
            .voteType(VoteType.DISAGREE)
            .content("Comment with some likes")
            .build();
        discussionCommentRepository.save(comment2);

        comment3 = DiscussionComment.builder()
            .discussion(discussion)
            .member(member3)
            .voteType(VoteType.AGREE)
            .content("Comment with fewer likes")
            .build();
        discussionCommentRepository.save(comment3);

        // 좋아요 추가 - 멤버별로 댓글에 좋아요 추가
        CommentLike commentLike1 = CommentLike.builder()
            .comment(comment1)
            .isLike(true)
            .member(member1)
            .build();

        commentLikeRepository.save(commentLike1);

        CommentLike commentLike2 = CommentLike.builder()
            .comment(comment2)
            .isLike(true)
            .member(member2)
            .build();

        commentLikeRepository.save(commentLike2);

        CommentLike commentLike3 = CommentLike.builder()
            .comment(comment3)
            .isLike(true)
            .member(member3)
            .build();

        commentLikeRepository.save(commentLike3);

        CommentLike commentLike4 = CommentLike.builder()
            .comment(comment1)
            .isLike(true)
            .member(member4)
            .build();

        commentLikeRepository.save(commentLike4);

        CommentLike commentLike5 = CommentLike.builder()
            .comment(comment2)
            .isLike(true)
            .member(member5)
            .build();

        commentLikeRepository.save(commentLike5);
    }

    @Test
    @DisplayName("연결 확인")
    void testConnection() {
        assertNotNull(discussionCommentRepository);
    }

    @Test
    @DisplayName("findMaxGroupId 메서드 테스트")
    @Rollback(true)
    void testFindMaxGroupId() {
        // 댓글 그룹 ID가 0부터 시작하는지 확인
        Long maxGroupId = discussionCommentRepository.findMaxGroupId();
        assertThat(maxGroupId).isEqualTo(0L);

        // 새로운 댓글 추가
        DiscussionComment comment = DiscussionComment.builder()
            .discussion(discussion)
            .member(member1)
            .groupId(1L)
            .voteType(VoteType.AGREE)
            .content("New Comment with Group ID")
            .build();
        discussionCommentRepository.save(comment);

        // 데이터베이스에 반영되도록 강제 flush
        entityManager.flush();
        entityManager.clear();  // 영속성 컨텍스트 초기화

        // 그룹 ID가 1로 증가했는지 확인
        maxGroupId = discussionCommentRepository.findMaxGroupId();
        assertThat(maxGroupId).isEqualTo(1L);
    }

    @Test
    @DisplayName("findTopTotalOrder 메서드 테스트")
    void testFindTopTotalOrder() {
        // 댓글을 몇 개 추가
        DiscussionComment comment1 = DiscussionComment.builder()
            .discussion(discussion)
            .member(member1)
            .voteType(VoteType.AGREE)
            .totalOrder(1L)
            .content("Comment 1")
            .build();
        discussionCommentRepository.save(comment1);

        DiscussionComment comment2 = DiscussionComment.builder()
            .discussion(discussion)
            .member(member2)
            .voteType(VoteType.DISAGREE)
            .totalOrder(2L)
            .content("Comment 2")
            .build();
        discussionCommentRepository.save(comment2);

        // 가장 높은 totalOrder 값을 가져오는지 확인
        Long topTotalOrder = discussionCommentRepository.findTopTotalOrder(discussion.getId()).orElse(0L);
        assertThat(topTotalOrder).isEqualTo(2L);
    }

    @Test
    @DisplayName("findMaxTotalOrderFromChild 메서드 테스트")
    void testFindMaxTotalOrderFromChild() {
        // 자식 댓글을 추가
        DiscussionComment parentComment = DiscussionComment.builder()
            .discussion(discussion)
            .member(member1)
            .groupId(1L)
            .voteType(VoteType.AGREE)
            .content("Parent Comment")
            .build();
        discussionCommentRepository.save(parentComment);

        DiscussionComment childComment = DiscussionComment.builder()
            .discussion(discussion)
            .member(member2)
            .parentId(parentComment.getId())
            .groupId(1L)
            .totalOrder(5L)
            .voteType(VoteType.DISAGREE)
            .content("Child Comment")
            .build();
        discussionCommentRepository.save(childComment);

        // 특정 그룹 내에서 가장 높은 totalOrder 값을 가져오는지 확인
        Long maxTotalOrder = discussionCommentRepository.findMaxTotalOrderFromChild(1L);
        assertThat(maxTotalOrder).isEqualTo(5L);
    }

    @Test
    @DisplayName("incrementTotalOrderFrom 메서드 테스트")
    @Rollback(true) // 기본값은 true, 수동으로 설정해서 롤백이 되도록 보장
    void testIncrementTotalOrderFrom() {
        // 초기 댓글 추가
        DiscussionComment comment = DiscussionComment.builder()
            .discussion(discussion)
            .member(member1)
            .totalOrder(10L)
            .voteType(VoteType.AGREE)
            .content("Original Comment")
            .build();
        discussionCommentRepository.save(comment);

        // totalOrder 10 이상인 모든 댓글의 totalOrder를 증가
        discussionCommentRepository.incrementTotalOrderFrom(10L);

        // 데이터베이스에 반영되도록 강제로 flush 및 clear
        entityManager.flush();
        entityManager.clear();

        // 수정된 댓글 확인
        DiscussionComment updatedComment = discussionCommentRepository.findById(comment.getId()).orElseThrow();
        assertThat(updatedComment.getTotalOrder()).isEqualTo(11L);

        // 이 테스트 후 DB에 실제로 반영되지 않음
    }

//    @Test
//    @DisplayName("getBestCommentTop3List 메서드 테스트")
//    void testGetBestCommentTop3List() {
//        // 메서드 실행
//        Optional<List<DiscussionComment>> top3Comments = discussionCommentRepository.findBestCommentTop3List();
//
//        System.out.println("top3Comments = " + top3Comments);
//    }
}
