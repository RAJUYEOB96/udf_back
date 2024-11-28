package com.undefinedus.backend.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.undefinedus.backend.domain.entity.AladinBook;
import com.undefinedus.backend.domain.entity.Discussion;
import com.undefinedus.backend.domain.entity.DiscussionComment;
import com.undefinedus.backend.domain.entity.Member;
import com.undefinedus.backend.domain.entity.MyBook;
import com.undefinedus.backend.domain.entity.Report;
import com.undefinedus.backend.domain.enums.BookStatus;
import com.undefinedus.backend.domain.enums.DiscussionCommentStatus;
import com.undefinedus.backend.domain.enums.DiscussionStatus;
import com.undefinedus.backend.domain.enums.ReportStatus;
import com.undefinedus.backend.domain.enums.ReportTargetType;
import com.undefinedus.backend.domain.enums.VoteType;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@Log4j2
class ReportRepositoryTest {
    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private DiscussionRepository discussionRepository;

    @Autowired
    private DiscussionCommentRepository discussionCommentRepository;

    @Autowired
    private AladinBookRepository aladinBookRepository;

    @Autowired
    private MyBookRepository myBookRepository;

    @Autowired
    private EntityManager entityManager;

    private Member reporter;
    private Member reporter2;
    private Member reported;
    private Discussion discussion;
    private MyBook myBook;
    private AladinBook aladinBook;
    private DiscussionComment comment1;
    private DiscussionComment comment2;
    private DiscussionComment comment3;

    @BeforeEach
    void setUp() {
        // Member 데이터 생성
        reporter = Member.builder()
            .username("test1@example.com")  // 아이디
            .password("hashedPassword123")  // 암호화된 비밀번호
            .nickname("테스트닉네임1")  // 닉네임 (2~10자)
            .profileImage("https://example.com/profile.jpg")  // 프로필 이미지 URL
            .introduction("안녕하세요! 저는 책을 좋아하는 회원입니다.")  // 자기소개
            .birth(LocalDate.of(1990, 1, 1))  // 생일
            .gender("남성")  // 성별
            .isPublic(true)  // 공개 여부
            .isMessageToKakao(false)  // 카톡 알림 여부
            .honorific("초보리더")  // 칭호
            .build();

        reporter2 = Member.builder()
            .username("test2@example.com")  // 아이디
            .password("hashedPassword123")  // 암호화된 비밀번호
            .nickname("테스트닉네임2")  // 닉네임 (2~10자)
            .profileImage("https://example.com/profile.jpg")  // 프로필 이미지 URL
            .introduction("안녕하세요! 저는 책을 좋아하는 회원입니다.")  // 자기소개
            .birth(LocalDate.of(1990, 1, 1))  // 생일
            .gender("남성")  // 성별
            .isPublic(true)  // 공개 여부
            .isMessageToKakao(false)  // 카톡 알림 여부
            .honorific("초보리더")  // 칭호
            .build();

        reported = Member.builder()
            .username("test3@example.com")  // 아이디
            .password("hashedPassword123")  // 암호화된 비밀번호
            .nickname("테스트닉네임3")  // 닉네임 (2~10자)
            .profileImage("https://example.com/profile.jpg")  // 프로필 이미지 URL
            .introduction("안녕하세요! 저는 책을 좋아하는 회원입니다.")  // 자기소개
            .birth(LocalDate.of(1990, 1, 1))  // 생일
            .gender("여성")  // 성별
            .isPublic(true)  // 공개 여부
            .isMessageToKakao(false)  // 카톡 알림 여부
            .honorific("초보리더")  // 칭호
            .build();

        memberRepository.save(reporter);
        memberRepository.save(reporter2);
        memberRepository.save(reported);

        // AladinBook 데이터 생성
        aladinBook = AladinBook.builder()
            .isbn13("9781234567890")
            .title("Sample Book")
            .author("Author Name")
            .link("https://www.aladin.com/book/1234567890")
            .cover("https://www.aladin.com/book/cover.jpg")
            .fullDescription("This is a sample book description.")
            .fullDescription2("Publisher's description here.")
            .publisher("Sample Publisher")
            .categoryName("Fiction")
            .customerReviewRank(4.5)
            .itemPage(300)
            .build();
        aladinBookRepository.save(aladinBook);

        // MyBook 데이터 생성
        myBook = MyBook.builder()
            .member(reported)
            .aladinBook(aladinBook)
            .isbn13("9781234567890")
            .status(BookStatus.READING)
            .myRating(4.0)
            .oneLineReview("Great book!")
            .currentPage(150)
            .updateCount(1)
            .startDate(LocalDate.of(2024, 11, 1))
            .endDate(LocalDate.of(2024, 11, 20))
            .build();
        myBookRepository.save(myBook);

        // Discussion 데이터 생성
        discussion = Discussion.builder()
            .myBook(myBook)  // MyBook 객체 (책 정보)
            .member(reported)  // 작성자 (Member 객체)
            .title("책에 대해 어떻게 생각하시나요?")  // 토론 제목
            .content("이 책은 정말 흥미롭고 생각할 거리를 많이 던져주는 작품입니다.")  // 토론 내용
            .status(DiscussionStatus.PROPOSED)  // 토론 상태 (기본값 PROPOSED)
            .startDate(LocalDateTime.now().plusDays(1))  // 최소 24시간 후로 설정
            .views(0L)  // 조회수 (기본값 0)
            .isDeleted(false)  // 삭제 여부 (기본값 false)
            .build();
        discussionRepository.save(discussion);

        // DiscussionComment 데이터 생성
        comment1 = DiscussionComment.builder()
            .discussion(discussion)
            .member(reported)
            .groupId(1L)
            .parentId(0L)
            .order(1L)
            .isChild(false)
            .totalOrder(1L)
            .voteType(VoteType.AGREE)
            .content("I agree with this point.")
            .isSelected(false)
            .discussionCommentStatus(DiscussionCommentStatus.ACTIVE)
            .isDeleted(false)
            .deletedAt(null)
            .build();

        discussionCommentRepository.save(comment1);

        comment2 = DiscussionComment.builder()
            .discussion(discussion)
            .member(reported)
            .groupId(1L)
            .parentId(0L)
            .order(2L)
            .isChild(false)
            .totalOrder(2L)
            .voteType(VoteType.DISAGREE)
            .content("I disagree with this point.")
            .isSelected(false)
            .discussionCommentStatus(DiscussionCommentStatus.ACTIVE)
            .isDeleted(false)
            .deletedAt(null)
            .build();

        discussionCommentRepository.save(comment2);

        comment3 = DiscussionComment.builder()
            .discussion(discussion)
            .member(reporter)
            .groupId(2L)
            .parentId(1L)
            .order(1L)
            .isChild(true)
            .totalOrder(3L)
            .voteType(VoteType.AGREE)
            .content("I support your view on this.")
            .isSelected(false)
            .discussionCommentStatus(DiscussionCommentStatus.ACTIVE)
            .isDeleted(false)
            .deletedAt(null)
            .build();

        discussionCommentRepository.save(comment3);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void testCountByDiscussionAndStatus() {
        // 1번 신고 (reporter가 discussion을 신고)
        Report report1 = Report.builder()
            .reporter(reporter)
            .reported(reported)
            .targetType(ReportTargetType.DISCUSSION)
            .reportReason("기타")
            .status(ReportStatus.PENDING)
            .discussion(discussion)
            .build();
        reportRepository.save(report1);

        // 2번 신고 (reporter2가 같은 discussion을 신고)
        Report report2 = Report.builder()
            .reporter(reporter2)
            .reported(reported)
            .targetType(ReportTargetType.DISCUSSION)
            .reportReason("불법정보")
            .status(ReportStatus.PENDING)
            .discussion(discussion)
            .build();
        reportRepository.save(report2);

        // 검증: discussion에 대한 신고 수는 2이어야 함
        long count = reportRepository.countByDiscussionAndStatus(discussion, ReportStatus.PENDING);
        assertThat(count).isEqualTo(2);
    }

    @Test
    void testCountByCommentAndStatus() {

        // 1번 신고 (reporter가 comment1을 신고)
        Report report1 = Report.builder()
            .reporter(reporter)
            .reported(reported)
            .targetType(ReportTargetType.DISCUSSION_COMMENT)
            .reportReason("불법정보")
            .comment(comment1)
            .build();
        reportRepository.save(report1);

        // 2번 신고 (reporter2가 같은 comment1을 신고)
        Report report2 = Report.builder()
            .reporter(reporter2)
            .reported(reported)
            .targetType(ReportTargetType.DISCUSSION_COMMENT)
            .reportReason("개인정보노출_유포_거래")
            .comment(comment1)
            .build();
        reportRepository.save(report2);

        // 검증: comment1에 대한 신고 수는 2이어야 함
        long count = reportRepository.countByCommentAndStatus(comment1, ReportStatus.PENDING);
        assertThat(count).isEqualTo(2);
    }

    @Test
    void testFindByDiscussionAndStatus() {
        // 신고 추가
        Report report1 = Report.builder()
            .reporter(reporter)
            .reported(reported)
            .targetType(ReportTargetType.DISCUSSION)
            .reportReason("기타")
            .status(ReportStatus.PENDING)
            .discussion(discussion)
            .build();
        reportRepository.save(report1);

        // 신고 리스트 조회
        List<Report> reports = reportRepository.findByDiscussionAndStatus(discussion, ReportStatus.PENDING);
        assertThat(reports).hasSize(1);  // 1개의 신고가 조회되어야 함
    }

    @Test
    void testFindByCommentAndStatus() {
        // 신고 추가
        Report report1 = Report.builder()
            .reporter(reporter)
            .reported(reported)
            .targetType(ReportTargetType.DISCUSSION_COMMENT)
            .reportReason("불법정보")
            .status(ReportStatus.PENDING)
            .comment(comment1)  // comment1 지정
            .build();
        reportRepository.save(report1);

        // 신고 리스트 조회
        List<Report> reports = reportRepository.findByCommentAndStatus(comment1, ReportStatus.PENDING);
        assertThat(reports).hasSize(1);  // 1개의 신고가 조회되어야 함
    }
}