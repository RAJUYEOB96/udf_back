package com.undefinedus.backend.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

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
import com.undefinedus.backend.dto.request.ScrollRequestDTO;
import com.undefinedus.backend.dto.request.report.ReportRequestDTO;
import com.undefinedus.backend.dto.response.ScrollResponseDTO;
import com.undefinedus.backend.dto.response.report.ReportResponseDTO;
import com.undefinedus.backend.exception.report.ReportNotFoundException;
import com.undefinedus.backend.repository.AladinBookRepository;
import com.undefinedus.backend.repository.DiscussionCommentRepository;
import com.undefinedus.backend.repository.DiscussionRepository;
import com.undefinedus.backend.repository.MemberRepository;
import com.undefinedus.backend.repository.MyBookRepository;
import com.undefinedus.backend.repository.ReportRepository;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class ReportServiceImplTest {
    @Autowired
    private ReportServiceImpl reportService;
    
    @Autowired
    private MemberRepository memberRepository;
    
    @Autowired
    private DiscussionRepository discussionRepository;
    
    @Autowired
    private DiscussionCommentRepository discussionCommentRepository;
    
    @Autowired
    private ReportRepository reportRepository;
    
    @Autowired
    private EntityManager entityManager;
    
    @Autowired
    private AladinBookRepository aladinBookRepository;
    
    @Autowired
    private MyBookRepository myBookRepository;
    
    private Member reporter;
    private Member reporter2;
    private Member reported;
    private Member reported2;
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
        
        reported2 = Member.builder()
                .username("test4@example.com")  // 아이디
                .password("hashedPassword123")  // 암호화된 비밀번호
                .nickname("테스트닉네임4")  // 닉네임 (2~10자)
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
        memberRepository.save(reported2);
        
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
                .member(reporter)
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
                .member(reporter2)
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
                .member(reported2)
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
    @DisplayName("토론 게시글 신고")
    void reportDiscussion() {
        // ReportRequestDTO 생성
        ReportRequestDTO requestDTO = new ReportRequestDTO();
        requestDTO.setDiscussionId(discussion.getId());  // 동일한 토론에 대해 신고
        requestDTO.setReportedId(reported.getId()); // 신고된 사람 설정
        requestDTO.setReason("기타");
        requestDTO.setTargetType(String.valueOf(ReportTargetType.DISCUSSION));
        
        System.out.println("discussion = " + discussion.getStatus());
        
        // 1번 신고 실행
        reportService.report(reporter.getId(), requestDTO); // 신고한 사람 설정
        
        // 2번 신고 실행 (동일한 discussion에 대해 다시 신고)
        reportService.report(reporter2.getId(), requestDTO);
        
        // 3번 신고 실행 (동일한 discussion에 대해 다시 신고)
        reportService.report(reported2.getId(), requestDTO);
        
        List<Report> all = reportRepository.findAll();
        
        System.out.println("all = " + all);
        
        entityManager.flush();
        entityManager.clear();
        
        Discussion updatedDiscussion = discussionRepository.findById(discussion.getId()).orElseThrow();
        
        // 상태 확인
        assertThat(updatedDiscussion.getStatus()).isEqualTo(DiscussionStatus.BLOCKED);  // 3회 신고 시 상태가 BLOCKED로 변경됨
        
        System.out.println("updatedDiscussion = " + updatedDiscussion.getStatus());
    }
    
    @Test
    @DisplayName("댓글 신고")
    void reportComment() {
        // ReportRequestDTO 생성
        ReportRequestDTO requestDTO = new ReportRequestDTO();
        requestDTO.setCommentId(comment1.getId());
        requestDTO.setReportedId(reported.getId()); // 신고된 사람 설정
        requestDTO.setReason("불법정보");
        requestDTO.setTargetType(ReportTargetType.DISCUSSION_COMMENT.name());
        
        // 1번 신고 실행
        reportService.report(reporter.getId(), requestDTO); // 신고한 사람 설정
        
        // 2번 신고 실행 (동일한 댓글에 대해 다시 신고)
        reportService.report(reporter2.getId(), requestDTO);
        
        // 3번 신고 실행 (동일한 댓글에 대해 다시 신고)
        reportService.report(reported2.getId(), requestDTO);
        
        entityManager.flush();
        entityManager.clear();
        
        DiscussionComment discussionComment = discussionCommentRepository.findById(comment1.getId())
                .orElseThrow();
        
        // 상태 확인
        assertThat(discussionComment.getDiscussionCommentStatus()).isEqualTo(DiscussionCommentStatus.BLOCKED);  // 3회 신고 시 상태가 BLOCKED로 변경됨
        
        System.out.println("discussionComment = " + discussionComment.getDiscussionCommentStatus());
    }
    
    @Test
    @DisplayName("미처리 신고 목록을 페이징하여 조회한다")
    void testGetPendingReportList() {
        // given
        // 신고 데이터 생성 (토론글 신고)
        List<Discussion> discussions = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            Discussion newDiscussion = Discussion.builder()
                    .myBook(myBook)
                    .member(reported)
                    .title("토론 제목 " + i)
                    .content("토론 내용 " + i)
                    .status(DiscussionStatus.PROPOSED)
                    .startDate(LocalDateTime.now().plusDays(1))
                    .views(0L)
                    .isDeleted(false)
                    .build();
            discussionRepository.save(newDiscussion);
            discussions.add(newDiscussion);
        }
        
        // 각 토론글에 대한 신고 생성
        for (Discussion disc : discussions) {
            ReportRequestDTO requestDTO = new ReportRequestDTO();
            requestDTO.setDiscussionId(disc.getId());
            requestDTO.setReportedId(reported.getId());
            requestDTO.setReason("불법정보");
            requestDTO.setTargetType(ReportTargetType.DISCUSSION.name());
            
            reportService.report(reporter.getId(), requestDTO);
        }
        
        entityManager.flush();
        entityManager.clear();
        
        // when
        ScrollRequestDTO scrollRequestDTO = new ScrollRequestDTO();
        scrollRequestDTO.setTabCondition("미처리");
        scrollRequestDTO.setSort("desc");
        scrollRequestDTO.setSize(2);
        
        ScrollResponseDTO<?> response = reportService.getReportList(scrollRequestDTO);
        
        // then
        assertThat(response.getContent()).hasSize(2); // size만큼만 조회되는지 확인
        assertThat(response.isHasNext()).isTrue(); // 다음 페이지 존재 여부 확인
        assertThat(response.getTotalElements()).isEqualTo(3L); // 전체 개수 확인
    }
    
    @Test
    @DisplayName("처리완료된 신고 목록을 페이징하여 조회한다")
    void testGetCompletedReportList() {
        // given
        // 토론글 3개 생성
        List<Discussion> discussions = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            Discussion newDiscussion = Discussion.builder()
                    .myBook(myBook)
                    .member(reported)
                    .title("토론 제목 " + i)
                    .content("토론 내용 " + i)
                    .status(DiscussionStatus.PROPOSED)
                    .startDate(LocalDateTime.now().plusDays(1))
                    .views(0L)
                    .isDeleted(false)
                    .build();
            discussionRepository.save(newDiscussion);
            discussions.add(newDiscussion);
        }
        
        // 각 토론글에 대해 3번씩 신고하여 BLOCKED 상태로 만듦
        for (Discussion disc : discussions) {
            ReportRequestDTO requestDTO = new ReportRequestDTO();
            requestDTO.setDiscussionId(disc.getId());
            requestDTO.setReportedId(reported.getId());
            requestDTO.setReason("불법정보");
            requestDTO.setTargetType(ReportTargetType.DISCUSSION.name());
            
            reportService.report(reporter.getId(), requestDTO);
            reportService.report(reporter2.getId(), requestDTO);
            reportService.report(reported2.getId(), requestDTO);
        }
        
        entityManager.flush();
        entityManager.clear();
        
        // when
        ScrollRequestDTO scrollRequestDTO = new ScrollRequestDTO();
        scrollRequestDTO.setTabCondition("미처리");
        scrollRequestDTO.setSort("desc");
        scrollRequestDTO.setSize(2);
        
        ScrollResponseDTO<?> response = reportService.getReportList(scrollRequestDTO);
        
        // then
        assertThat(response.getContent()).hasSize(2);
        assertThat(response.isHasNext()).isTrue();
        assertThat(response.getTotalElements()).isEqualTo(9L); // 3개의 토론글 * 3번의 신고 = 9
    }
    
    @Test
    @DisplayName("빈 목록을 조회한다")
    void testGetEmptyList() {
        // given
        ScrollRequestDTO scrollRequestDTO = new ScrollRequestDTO();
        scrollRequestDTO.setTabCondition("미처리");
        scrollRequestDTO.setSort("desc");
        scrollRequestDTO.setSize(10);
        
        // when
        ScrollResponseDTO<?> response = reportService.getReportList(scrollRequestDTO);
        
        // then
        assertThat(response.getContent()).isEmpty();
        assertThat(response.isHasNext()).isFalse();
        assertThat(response.getTotalElements()).isEqualTo(0L);
    }
    
    @Test
    @DisplayName("신고 상세 정보를 조회한다")
    void testGetReportDetail() {
        // given
        Report report = Report.builder()
                .reporter(reporter)
                .reported(reported)
                .targetType(ReportTargetType.DISCUSSION)
                .reportReason("불법정보")
                .status(ReportStatus.PENDING)
                .discussion(discussion)
                .build();
        
        Report savedReport = reportRepository.save(report);
        Long reportId = savedReport.getId();
        
        entityManager.flush();
        entityManager.clear();
        
        // when
        ReportResponseDTO responseDTO = reportService.getReportDetail(reportId);
        
        // then
        assertThat(responseDTO).isNotNull();
        assertThat(responseDTO.getId()).isEqualTo(reportId);
        assertThat(responseDTO.getReporterNickname()).isEqualTo(reporter.getNickname());
        assertThat(responseDTO.getReportedNickname()).isEqualTo(reported.getNickname());
        assertThat(responseDTO.getReportReason()).isEqualTo("불법정보");
        assertThat(responseDTO.getTargetType()).isEqualTo(ReportTargetType.DISCUSSION);
        assertThat(responseDTO.getDiscussionTitle()).isEqualTo(savedReport.getDiscussion().getTitle());
        assertThat(responseDTO.getDiscussionContent()).isEqualTo(savedReport.getDiscussion().getContent());
        assertThat(responseDTO.getCommentContent()).isNull();  // Discussion 신고인 경우 commentId는 null
    }
    
    @Test
    @DisplayName("댓글 신고 상세 정보를 조회한다")
    void testGetCommentReportDetail() {
        // given
        Report report = Report.builder()
                .reporter(reporter)
                .reported(reported)
                .targetType(ReportTargetType.DISCUSSION_COMMENT)
                .reportReason("불법정보")
                .status(ReportStatus.PENDING)
                .comment(comment1)
                .build();
        
        Report savedReport = reportRepository.save(report);
        Long reportId = savedReport.getId();
        
        entityManager.flush();
        entityManager.clear();
        
        // when
        ReportResponseDTO responseDTO = reportService.getReportDetail(reportId);
        
        // then
        assertThat(responseDTO).isNotNull();
        assertThat(responseDTO.getId()).isEqualTo(reportId);
        assertThat(responseDTO.getReporterNickname()).isEqualTo(reporter.getNickname());
        assertThat(responseDTO.getReportedNickname()).isEqualTo(reported.getNickname());
        assertThat(responseDTO.getReportReason()).isEqualTo("불법정보");
        assertThat(responseDTO.getTargetType()).isEqualTo(ReportTargetType.DISCUSSION_COMMENT);
        assertThat(responseDTO.getDiscussionTitle()).isNull();  // Comment 신고인 경우 discussionId는 null
        assertThat(responseDTO.getDiscussionContent()).isNull();  // Comment 신고인 경우 discussionId는 null
        assertThat(responseDTO.getCommentContent()).isEqualTo(comment1.getContent());
    }
    
    @Test
    @DisplayName("존재하지 않는 신고 ID로 조회시 예외가 발생한다")
    void testGetReportDetail_NotFound() {
        // given
        Long nonExistentReportId = 999L;
        
        // when & then
        assertThatThrownBy(() -> reportService.getReportDetail(nonExistentReportId))
                .isInstanceOf(ReportNotFoundException.class)
                .hasMessageContaining("해당 report를 찾을 수 없습니다.");
    }
    
    @Test
    @DisplayName("PENDING 상태의 토론 신고를 거절하면 토론이 이전 상태로 복구된다")
    void testRejectPendingDiscussionReport() {
        // given
        Report report = Report.builder()
                .reporter(reporter)
                .reported(reported)
                .targetType(ReportTargetType.DISCUSSION)
                .reportReason("불법정보")
                .status(ReportStatus.PENDING)
                .discussion(discussion)
                .previousDiscussionStatus(DiscussionStatus.PROPOSED)  // 이전 상태 저장
                .build();
        
        Report savedReport = reportRepository.save(report);
        discussion.changeStatus(DiscussionStatus.BLOCKED);  // 토론 상태를 BLOCKED로 변경
        
        entityManager.flush();
        entityManager.clear();
        
        // when
        reportService.rejectReport(savedReport.getId());
        
        // then
        Report rejectedReport = reportRepository.findById(savedReport.getId()).orElseThrow();
        Discussion updatedDiscussion = discussionRepository.findById(discussion.getId()).orElseThrow();
        
        assertThat(rejectedReport.getStatus()).isEqualTo(ReportStatus.REJECTED);
        assertThat(updatedDiscussion.getStatus()).isEqualTo(DiscussionStatus.PROPOSED);
    }
    
    @Test
    @DisplayName("TEMPORARY_ACCEPTED 상태의 토론 신고를 거절하면 토론이 이전 상태로 복구된다")
    void testRejectTemporaryAcceptedDiscussionReport() {
        // given
        Report report = Report.builder()
                .reporter(reporter)
                .reported(reported)
                .targetType(ReportTargetType.DISCUSSION)
                .reportReason("불법정보")
                .status(ReportStatus.TEMPORARY_ACCEPTED)
                .discussion(discussion)
                .previousDiscussionStatus(DiscussionStatus.IN_PROGRESS)  // 이전 상태 저장
                .build();
        
        Report savedReport = reportRepository.save(report);
        discussion.changeStatus(DiscussionStatus.BLOCKED);  // 토론 상태를 BLOCKED로 변경
        
        entityManager.flush();
        entityManager.clear();
        
        // when
        reportService.rejectReport(savedReport.getId());
        
        // then
        Report rejectedReport = reportRepository.findById(savedReport.getId()).orElseThrow();
        Discussion updatedDiscussion = discussionRepository.findById(discussion.getId()).orElseThrow();
        
        assertThat(rejectedReport.getStatus()).isEqualTo(ReportStatus.REJECTED);
        assertThat(updatedDiscussion.getStatus()).isEqualTo(DiscussionStatus.IN_PROGRESS);
    }
    
    @Test
    @DisplayName("댓글 신고를 거절하면 댓글이 ACTIVE 상태로 복구된다")
    void testRejectCommentReport() {
        // given
        Report report = Report.builder()
                .reporter(reporter)
                .reported(reported)
                .targetType(ReportTargetType.DISCUSSION_COMMENT)
                .reportReason("불법정보")
                .status(ReportStatus.TEMPORARY_ACCEPTED)
                .comment(comment1)
                .build();
        
        Report savedReport = reportRepository.save(report);
        comment1.changeDiscussionCommentStatus(DiscussionCommentStatus.BLOCKED);
        
        entityManager.flush();
        entityManager.clear();
        
        // when
        reportService.rejectReport(savedReport.getId());
        
        // then
        Report rejectedReport = reportRepository.findById(savedReport.getId()).orElseThrow();
        DiscussionComment updatedComment = discussionCommentRepository.findById(comment1.getId()).orElseThrow();
        
        assertThat(rejectedReport.getStatus()).isEqualTo(ReportStatus.REJECTED);
        assertThat(updatedComment.getDiscussionCommentStatus()).isEqualTo(DiscussionCommentStatus.ACTIVE);
    }
    
    @Test
    @DisplayName("이미 ACCEPTED나 REJECTED 상태인 신고는 거절할 수 없다")
    void testCannotRejectFinalizedReport() {
        // given
        Report acceptedReport = Report.builder()
                .reporter(reporter)
                .reported(reported)
                .targetType(ReportTargetType.DISCUSSION)
                .reportReason("불법정보")
                .status(ReportStatus.ACCEPTED)
                .discussion(discussion)
                .build();
        
        Report rejectedReport = Report.builder()
                .reporter(reporter2)
                .reported(reported)
                .targetType(ReportTargetType.DISCUSSION)
                .reportReason("불법정보")
                .status(ReportStatus.REJECTED)
                .discussion(discussion)
                .build();
        
        // 변수를 재할당하지 않고 새로운 변수에 저장
        Report savedAcceptedReport = reportRepository.save(acceptedReport);
        Report savedRejectedReport = reportRepository.save(rejectedReport);
        
        entityManager.flush();
        entityManager.clear();
        
        // when & then
        assertThatThrownBy(() -> reportService.rejectReport(acceptedReport.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("확정되지 않은 신고만 거절할 수 있습니다.");
        
        assertThatThrownBy(() -> reportService.rejectReport(rejectedReport.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("확정되지 않은 신고만 거절할 수 있습니다.");
    }
}