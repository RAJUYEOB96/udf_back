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
import com.undefinedus.backend.domain.enums.DiscussionStatus;
import com.undefinedus.backend.domain.enums.ReportStatus;
import com.undefinedus.backend.domain.enums.ReportTargetType;
import com.undefinedus.backend.domain.enums.ViewStatus;
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
import java.util.List;
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
    private DiscussionComment comment;
    private DiscussionComment comment2;
    private DiscussionComment comment3;
    private Report report;
    private Report report2;


    @BeforeEach
    void setUp() {
        // Member 데이터 생성
        reporter = Member.builder()
            .username("test51@example.com")  // 아이디
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
            .username("test52@example.com")  // 아이디
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
            .username("test53@example.com")  // 아이디
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
            .username("test54@example.com")  // 아이디
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
            .member(reported)  // 작성자 (Member 객체)
            .aladinBook(aladinBook)  // 추가
            .title("책에 대해 어떻게 생각하시나요?")  // 토론 제목
            .content("이 책은 정말 흥미롭고 생각할 거리를 많이 던져주는 작품입니다.")  // 토론 내용
            .status(DiscussionStatus.PROPOSED)  // 토론 상태 (기본값 PROPOSED)
            .startDate(LocalDateTime.now().plusDays(1))  // 최소 24시간 후로 설정
            .views(0L)  // 조회수 (기본값 0)
            .isDeleted(false)  // 삭제 여부 (기본값 false)
            .build();
        discussionRepository.save(discussion);

        // DiscussionComment 데이터 생성
        comment = DiscussionComment.builder()
            .discussion(discussion)
            .member(reporter)
            .groupId(1L)
            .parentId(0L)
            .groupOrder(1L)
            .isChild(false)
            .totalOrder(1L)
            .voteType(VoteType.AGREE)
            .content("I agree with this point.")
            .viewStatus(ViewStatus.ACTIVE)
            .isDeleted(false)
            .deletedAt(null)
            .build();

        discussionCommentRepository.save(comment);

        comment2 = DiscussionComment.builder()
            .discussion(discussion)
            .member(reporter2)
            .groupId(1L)
            .parentId(0L)
            .groupOrder(2L)
            .isChild(false)
            .totalOrder(2L)
            .voteType(VoteType.DISAGREE)
            .content("I disagree with this point.")
            .viewStatus(ViewStatus.ACTIVE)
            .isDeleted(false)
            .deletedAt(null)
            .build();

        discussionCommentRepository.save(comment2);

        comment3 = DiscussionComment.builder()
            .discussion(discussion)
            .member(reported2)
            .groupId(2L)
            .parentId(1L)
            .groupOrder(1L)
            .isChild(true)
            .totalOrder(3L)
            .voteType(VoteType.AGREE)
            .content("I support your view on this.")
            .viewStatus(ViewStatus.ACTIVE)
            .isDeleted(false)
            .deletedAt(null)
            .build();

        discussionCommentRepository.save(comment3);


        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("토론 신고 테스트")
    void testReportDiscussion() {
        // given
        ReportRequestDTO reportRequestDTO = ReportRequestDTO.builder()
            .reason("부적절한 내용")
            .build();

        // when
        reportService.reportDiscussion(reporter.getId(), discussion.getId(), reportRequestDTO);

        // then
        List<Report> reports = reportRepository.findAll();
        assertThat(reports)
            .hasSize(1)
            .allSatisfy(report -> {
                assertThat(report.getReporter().getId()).isEqualTo(reporter.getId());
                assertThat(report.getReported().getId()).isEqualTo(reported.getId());
                assertThat(report.getDiscussion().getId()).isEqualTo(discussion.getId());
                assertThat(report.getReportReason()).isEqualTo("부적절한 내용");
                assertThat(report.getStatus()).isEqualTo(ReportStatus.PENDING);
                assertThat(report.getTargetType()).isEqualTo(ReportTargetType.DISCUSSION);
            });
    }

    @Test
    @DisplayName("토론 신고 3회 이상 시 상태 변경 테스트")
    void testReportDiscussionMoreThanThreeTimes() {
        // given
        ReportRequestDTO reportRequestDTO = new ReportRequestDTO();
        reportRequestDTO.setReason("부적절한 내용");

        // when
        for (int i = 0; i < 3; i++) {
            Member newReporter = Member.builder()
                .username("reporter" + i + "@test.com")
                .password("password")
                .nickname("리포터" + i)
                .build();
            memberRepository.save(newReporter);
            reportService.reportDiscussion(newReporter.getId(), discussion.getId(),
                reportRequestDTO);
        }

        // then
        List<Report> reports = reportRepository.findAll();
        assertThat(reports).hasSize(3);
        for (Report report : reports) {
            assertThat(report.getStatus()).isEqualTo(ReportStatus.TEMPORARY_ACCEPTED);
        }

        Discussion blockedDiscussion = discussionRepository.findById(discussion.getId())
            .orElseThrow();
        assertThat(blockedDiscussion.getViewStatus()).isEqualTo(ViewStatus.BLOCKED);
    }

    @Test
    @DisplayName("댓글 신고 테스트")
    void testReportComment() {
        // given
        ReportRequestDTO reportRequestDTO = new ReportRequestDTO();
        reportRequestDTO.setReason("부적절한 댓글");

        // when
        reportService.reportComment(reporter.getId(), comment.getId(), reportRequestDTO);

        // 영속성 컨텍스트 동기화
        entityManager.flush();
        entityManager.clear();

        // given 객체 재조회
        Member updatedReporter = memberRepository.getOne(reporter.getId()); // getOne()을 사용해 직접 조회
        DiscussionComment updatedComment = discussionCommentRepository.getOne(comment.getId()); // getOne() 사용

        // then
        List<Report> reports = reportRepository.findAll();
        assertThat(reports).hasSize(1);

        Report report = reports.get(0);
        assertThat(report.getReporter().getId()).isEqualTo(updatedReporter.getId());
        assertThat(report.getComment().getId()).isEqualTo(updatedComment.getId());
        assertThat(report.getReportReason()).isEqualTo("부적절한 댓글");
        assertThat(report.getStatus()).isEqualTo(ReportStatus.PENDING);
        assertThat(report.getTargetType()).isEqualTo(ReportTargetType.DISCUSSION_COMMENT);
    }

    @Test
    @DisplayName("댓글 신고 3회 이상 시 상태 변경 테스트")
    void testReportCommentMoreThanThreeTimes() {
        // given
        ReportRequestDTO reportRequestDTO = new ReportRequestDTO();
        reportRequestDTO.setReason("부적절한 댓글");

        // when
        for (int i = 0; i < 3; i++) {
            Member newReporter = Member.builder()
                .username("reporter" + i + "@test.com")
                .password("password")
                .nickname("리포터" + i)
                .build();
            memberRepository.save(newReporter);
            reportService.reportComment(newReporter.getId(), comment.getId(), reportRequestDTO);
        }

        // then
        List<Report> reports = reportRepository.findAll();
        assertThat(reports).hasSize(3);
        for (Report report : reports) {
            assertThat(report.getStatus()).isEqualTo(ReportStatus.TEMPORARY_ACCEPTED);
        }

        DiscussionComment blockedComment = discussionCommentRepository.findById(comment.getId())
            .orElseThrow();
        assertThat(blockedComment.getViewStatus()).isEqualTo(
            ViewStatus.BLOCKED);
    }

    @Test
    @DisplayName("신고 목록 조회 테스트")
    void testGetReportList() {
        // given

        // Report 데이터 생성
        report = Report.builder()
            .reporter(reporter)  // 신고한 사람 (Member 객체)
            .reported(reported)  // 신고 당한 사람 (Member 객체)
            .targetType(ReportTargetType.DISCUSSION)  // 신고 대상 타입 (토론)
            .reportReason("불쾌감을 주는 내용")  // 신고 사유
            .status(ReportStatus.PENDING)  // 신고 처리 상태 (PENDING, TEMPORARY_ACCEPTED, ACCEPTED, REJECTED)
            .discussion(discussion)  // 신고 당한 토론 (Discussion 객체)
            .build();
        reportRepository.save(report);

        report2 = Report.builder()
            .reporter(reporter2)  // 신고한 사람 (Member 객체)
            .reported(reported2)  // 신고 당한 사람 (Member 객체)
            .targetType(ReportTargetType.DISCUSSION_COMMENT)  // 신고 대상 타입 (댓글)
            .reportReason("부적절한 언행")  // 신고 사유
            .status(ReportStatus.PENDING)  // 신고 처리 상태 (PENDING, TEMPORARY_ACCEPTED, ACCEPTED, REJECTED)
            .comment(comment2)  // 신고 당한 댓글 (DiscussionComment 객체)
            .build();
        reportRepository.save(report2);

        ScrollRequestDTO requestDTO = new ScrollRequestDTO();
        requestDTO.setSize(10);
        requestDTO.setLastId(0L);
        requestDTO.setTabCondition("미처리");
        requestDTO.setSort("desc");

        // when
        ScrollResponseDTO<ReportResponseDTO> result = reportService.getReportList(requestDTO);

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);

        // isHasNext() 검증 전에 현재 상태 출력
        System.out.println("Content size: " + result.getContent().size());
        System.out.println("Total elements: " + result.getTotalElements());
        System.out.println("Has next: " + result.isHasNext());

        assertThat(result.isHasNext()).isFalse();

        // 추가적인 검증
        assertThat(result.getContent())
            .extracting("targetType")
            .containsExactlyInAnyOrder(ReportTargetType.DISCUSSION, ReportTargetType.DISCUSSION_COMMENT);
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
        assertThat(responseDTO.getDiscussionTitle()).isEqualTo(
            savedReport.getDiscussion().getTitle());
        assertThat(responseDTO.getDiscussionContent()).isEqualTo(
            savedReport.getDiscussion().getContent());
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
            .comment(comment)
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
        assertThat(
            responseDTO.getDiscussionContent()).isNull();  // Comment 신고인 경우 discussionId는 null
        assertThat(responseDTO.getCommentContent()).isEqualTo(comment.getContent());
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
        discussion.changeViewStatus(ViewStatus.BLOCKED);  // 토론 상태를 BLOCKED로 변경

        entityManager.flush();
        entityManager.clear();

        // when
        reportService.rejectReport(savedReport.getId());

        // then
        Report rejectedReport = reportRepository.findById(savedReport.getId()).orElseThrow();
        Discussion updatedDiscussion = discussionRepository.findById(discussion.getId())
            .orElseThrow();

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
        discussion.changeViewStatus(ViewStatus.BLOCKED);  // 토론 상태를 BLOCKED로 변경

        entityManager.flush();
        entityManager.clear();

        // when
        reportService.rejectReport(savedReport.getId());

        // then
        Report rejectedReport = reportRepository.findById(savedReport.getId()).orElseThrow();
        Discussion updatedDiscussion = discussionRepository.findById(discussion.getId())
            .orElseThrow();

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
            .comment(comment)
            .build();

        Report savedReport = reportRepository.save(report);
        comment.changeViewStatus(ViewStatus.BLOCKED);

        entityManager.flush();
        entityManager.clear();

        // when
        reportService.rejectReport(savedReport.getId());

        // then
        Report rejectedReport = reportRepository.findById(savedReport.getId()).orElseThrow();
        DiscussionComment updatedComment = discussionCommentRepository.findById(comment.getId())
            .orElseThrow();

        assertThat(rejectedReport.getStatus()).isEqualTo(ReportStatus.REJECTED);
        assertThat(updatedComment.getViewStatus()).isEqualTo(
            ViewStatus.ACTIVE);
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

    @Test
    @DisplayName("TEMPORARY_ACCEPTED 상태의 신고를 승인하면 ACCEPTED 상태가 되고 관련 콘텐츠가 차단된다")
    void testApproveTemporaryAcceptedReport() {
        // given
        Report report = Report.builder()
            .reporter(reporter)
            .reported(reported)
            .targetType(ReportTargetType.DISCUSSION)
            .reportReason("불법정보")
            .status(ReportStatus.TEMPORARY_ACCEPTED)
            .discussion(discussion)
            .build();

        Report savedReport = reportRepository.save(report);

        entityManager.flush();
        entityManager.clear();

        // when
        reportService.approvalReport(savedReport.getId());

        // then
        Report approvedReport = reportRepository.findById(savedReport.getId()).orElseThrow();
        Discussion blockedDiscussion = discussionRepository.findById(discussion.getId())
            .orElseThrow();

        assertThat(approvedReport.getStatus()).isEqualTo(ReportStatus.ACCEPTED);
        assertThat(blockedDiscussion.getViewStatus()).isEqualTo(ViewStatus.BLOCKED);
    }

    @Test
    @DisplayName("PENDING 상태의 신고를 승인하면 ACCEPTED 상태가 되고 관련 콘텐츠가 차단된다")
    void testApprovePendingReport() {
        // given
        Report report = Report.builder()
            .reporter(reporter)
            .reported(reported)
            .targetType(ReportTargetType.DISCUSSION_COMMENT)
            .reportReason("불법정보")
            .status(ReportStatus.PENDING)
            .comment(comment)
            .build();

        Report savedReport = reportRepository.save(report);

        entityManager.flush();
        entityManager.clear();

        // when
        reportService.approvalReport(savedReport.getId());

        // then
        Report approvedReport = reportRepository.findById(savedReport.getId()).orElseThrow();
        DiscussionComment blockedComment = discussionCommentRepository.findById(comment.getId())
            .orElseThrow();

        assertThat(approvedReport.getStatus()).isEqualTo(ReportStatus.ACCEPTED);
        assertThat(blockedComment.getViewStatus()).isEqualTo(
            ViewStatus.BLOCKED);
    }

    @Test
    @DisplayName("이미 ACCEPTED나 REJECTED 상태인 신고는 승인할 수 없다")
    void testCannotApproveFinalizedReport() {
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

        Report savedAcceptedReport = reportRepository.save(acceptedReport);
        Report savedRejectedReport = reportRepository.save(rejectedReport);

        entityManager.flush();
        entityManager.clear();

        // when & then
        assertThatThrownBy(() -> reportService.approvalReport(savedAcceptedReport.getId()))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("확정되지 않은 신고만 승인할 수 있습니다.");

        assertThatThrownBy(() -> reportService.approvalReport(savedRejectedReport.getId()))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("확정되지 않은 신고만 승인할 수 있습니다.");
    }
}