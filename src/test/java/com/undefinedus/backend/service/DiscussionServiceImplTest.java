package com.undefinedus.backend.service;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.undefinedus.backend.domain.entity.AladinBook;
import com.undefinedus.backend.domain.entity.Discussion;
import com.undefinedus.backend.domain.entity.DiscussionComment;
import com.undefinedus.backend.domain.entity.DiscussionParticipant;
import com.undefinedus.backend.domain.entity.Member;
import com.undefinedus.backend.domain.entity.MyBook;
import com.undefinedus.backend.domain.enums.BookStatus;
import com.undefinedus.backend.domain.enums.DiscussionStatus;
import com.undefinedus.backend.domain.enums.ViewStatus;
import com.undefinedus.backend.domain.enums.VoteType;
import com.undefinedus.backend.dto.request.discussion.DiscussionRegisterRequestDTO;
import com.undefinedus.backend.dto.request.discussion.DiscussionUpdateRequestDTO;
import com.undefinedus.backend.dto.request.discussionComment.DiscussionScrollRequestDTO;
import com.undefinedus.backend.dto.response.ScrollResponseDTO;
import com.undefinedus.backend.dto.response.discussion.DiscussionListResponseDTO;
import com.undefinedus.backend.repository.AladinBookRepository;
import com.undefinedus.backend.repository.DiscussionCommentRepository;
import com.undefinedus.backend.repository.DiscussionParticipantRepository;
import com.undefinedus.backend.repository.DiscussionRepository;
import com.undefinedus.backend.repository.MemberRepository;
import com.undefinedus.backend.repository.MyBookRepository;
import com.undefinedus.backend.scheduler.config.QuartzConfig;
import com.undefinedus.backend.scheduler.job.Scheduled;
import com.undefinedus.backend.scheduler.repository.QuartzTriggerRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import java.util.Random;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ExtendWith(MockitoExtension.class)
@Log4j2
class DiscussionServiceImplTest {

    @Mock
    private MemberRepository memberRepository;
    @Mock
    private MyBookRepository myBookRepository;
    @Mock
    private DiscussionRepository discussionRepository;
    @Mock
    private QuartzConfig quartzConfig;
    @Mock
    private QuartzTriggerRepository quartzTriggerRepository;
    @Mock
    private AladinBookRepository aladinBookRepository;
    @Mock
    private DiscussionParticipantRepository discussionParticipantRepository;
    @Mock
    private Scheduled scheduled;
    @Mock
    private DiscussionCommentRepository discussionCommentRepository;

    @InjectMocks
    private DiscussionServiceImpl discussionServiceImpl;

    private Long memberId;
    private String isbn13;
    private AladinBook mockAladinBook; // 추가
    private Member member; // 추가
    private MyBook mockMyBook;
    private Discussion discussion;

    @BeforeEach
    void setUp() {

        // 공통으로 사용할 Mock 객체들 생성
        memberId = 1L;
        isbn13 = "1234567890123";

        // Mock Member
        member = Member.builder()
            .id(memberId)
            .nickname("testuser")
            .honorific("테스터")
            .build();

        // Mock AladinBook
        mockAladinBook = AladinBook.builder()
            .isbn13(isbn13)
            .title("Test Book")
            .author("Test Author")
            .cover("test-cover-url")
            .build();
    }

    // createMockDiscussion 메서드 생성
    private Discussion createMockDiscussion(Long id, String title, int agreeCount,
        int disagreeCount, Member member, AladinBook aladinBook) {
        // MyBook mock 데이터 생성
        mockMyBook = MyBook.builder()
            .id(id)
            .isbn13(aladinBook.getIsbn13())
            .member(member)
            .aladinBook(aladinBook)
            .status(BookStatus.COMPLETED)
            .build();

        // Discussion 객체 생성
        discussion = Discussion.builder()
            .id(id)
            .title(title)
            .member(member)
            .aladinBook(aladinBook)
            .content("Test content")
            .status(DiscussionStatus.PROPOSED)
            .startDate(LocalDateTime.now().plusDays(1))
            .closedAt(LocalDateTime.now().plusDays(2))
            .views(0L)
            .isDeleted(false)
            .build();

        // agreeCount와 disagreeCount만큼의 참여자들 추가
        if (agreeCount > 0 || disagreeCount > 0) {
            List<DiscussionParticipant> participants = new ArrayList<>();
            for (int i = 0; i < agreeCount; i++) {
                participants.add(DiscussionParticipant.builder()
                    .discussion(discussion)
                    .member(member)
                    .isAgree(true)
                    .build());
            }
            for (int i = 0; i < disagreeCount; i++) {
                participants.add(DiscussionParticipant.builder()
                    .discussion(discussion)
                    .member(member)
                    .isAgree(false)
                    .build());
            }
            discussion.changeParticipants(participants);
        }

        return discussion;
    }

    @Test
    @DisplayName("토론 생성 성공 테스트")
    void discussionRegister_shouldReturnDiscussionId() throws Exception {
        // Given
        LocalDateTime startDate = LocalDateTime.now().plusDays(1);

        MyBook mockMyBook = MyBook.builder()
            .id(1L)
            .isbn13(isbn13)
            .member(member)
            .aladinBook(mockAladinBook)
            .status(BookStatus.COMPLETED)
            .build();

        DiscussionRegisterRequestDTO requestDTO = DiscussionRegisterRequestDTO.builder()
            .isbn13(isbn13)
            .title("Test Discussion Title")
            .content("This is a test content for discussion.")
            .startDate(startDate)
            .build();

        Discussion mockDiscussion = Discussion.builder()
            .id(1L)
            .member(member)
            .aladinBook(mockAladinBook)
            .title(requestDTO.getTitle())
            .content(requestDTO.getContent())
            .status(DiscussionStatus.PROPOSED)
            .startDate(startDate)
            .closedAt(startDate.plusDays(1))
            .build();

        // Mocking
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(myBookRepository.findByMemberIdAndIsbn13(memberId, isbn13)).thenReturn(
            Optional.of(mockMyBook));
        when(discussionRepository.save(any(Discussion.class))).thenReturn(mockDiscussion);
        doNothing().when(quartzConfig).scheduleDiscussionJobs(any(LocalDateTime.class), anyLong());

        // When
        Long discussionId = discussionServiceImpl.discussionRegister(memberId, requestDTO);

        // Then
        assertNotNull(discussionId);
        assertEquals(1L, discussionId);

        verify(memberRepository).findById(memberId);
        verify(myBookRepository).findByMemberIdAndIsbn13(memberId, isbn13);
        verify(discussionRepository).save(any(Discussion.class));
        verify(quartzConfig).scheduleDiscussionJobs(any(LocalDateTime.class), anyLong());
    }

    @Test
    @DisplayName("토론 목록 조회 성공 테스트")
    void getDiscussionList_shouldReturnScrollResponse() {
        // Given
        int size = 30;
        List<Discussion> mockDiscussions = new ArrayList<>();
        for (int i = 1; i <= size + 1; i++) { // hasNext를 위한 +1
            Discussion discussion = createMockDiscussion((long) i, "Test Discussion " + i, 5, 3, member, mockAladinBook);
            mockDiscussions.add(discussion);
        }

        when(discussionRepository.findDiscussionsWithScroll(any(DiscussionScrollRequestDTO.class)))
            .thenReturn(mockDiscussions);

        when(discussionRepository.findAllByStatus(any(DiscussionStatus.class)))
            .thenReturn(mockDiscussions);

        // When
        DiscussionScrollRequestDTO requestDTO = DiscussionScrollRequestDTO.builder()
            .size(10)
            .status("PROPOSED")
            .lastId(0L)
            .build();

        ScrollResponseDTO<DiscussionListResponseDTO> response = discussionServiceImpl.getDiscussionList(requestDTO);

        // Then
        verify(discussionRepository).findDiscussionsWithScroll(any(DiscussionScrollRequestDTO.class));
        verify(discussionRepository).findAllByStatus(any(DiscussionStatus.class));

        assertEquals(size, response.getContent().size()); // 실제 반환된 개수 확인
        assertEquals(size, response.getNumberOfElements()); // 요소의 총 개수
        assertTrue(response.isHasNext()); // hasNext 확인
    }

    @Test
    @DisplayName("토론 수정 성공 테스트")
    void discussionModify_shouldModifyDiscussion() throws Exception {
        // Given
        Long discussionId = 10L;

        Discussion mockDiscussion = Discussion.builder()
            .id(discussionId)
            .member(member)
            .status(DiscussionStatus.PROPOSED)
            .build();

        DiscussionUpdateRequestDTO requestDTO = DiscussionUpdateRequestDTO.builder()
            .discussionId(discussionId)
            .title("Updated Title")
            .content("Updated Content")
            .modifyStartTime(LocalDateTime.now().plusHours(2))
            .build();

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(discussionRepository.findById(discussionId)).thenReturn(Optional.of(mockDiscussion));
        when(discussionRepository.save(any(Discussion.class))).thenReturn(mockDiscussion);

        // When
        Long modifiedDiscussionId = discussionServiceImpl.discussionUpdate(memberId, requestDTO);

        // Then
        assertNotNull(modifiedDiscussionId);
        assertEquals(discussionId, modifiedDiscussionId);
        assertEquals("Updated Title", mockDiscussion.getTitle());
        assertEquals("Updated Content", mockDiscussion.getContent());

        verify(discussionRepository).save(any(Discussion.class));
    }

    @Test
    @DisplayName("찬성 참여 성공 테스트")
    void joinAgree_shouldSaveDiscussionParticipant() {
        // Given
        Long discussionId = 5L;

        Discussion mockDiscussion = Discussion.builder()
            .id(discussionId)
            .member(member)
            .aladinBook(mockAladinBook)
            .status(DiscussionStatus.PROPOSED)
            .build();

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(discussionRepository.findById(discussionId)).thenReturn(Optional.of(mockDiscussion));

        // When
        discussionServiceImpl.joinAgree(memberId, discussionId);

        // Then
        ArgumentCaptor<DiscussionParticipant> captor = ArgumentCaptor.forClass(
            DiscussionParticipant.class);
        verify(discussionParticipantRepository).save(captor.capture());

        DiscussionParticipant savedParticipant = captor.getValue();
        assertEquals(mockDiscussion, savedParticipant.getDiscussion());
        assertEquals(member, savedParticipant.getMember());
        assertTrue(savedParticipant.isAgree());
    }

    @Test
    @DisplayName("반대 참여 성공 테스트")
    void joinDisagree_shouldSaveDiscussionParticipant() {
        // Given
        Long discussionId = 5L;

        Discussion mockDiscussion = Discussion.builder()
            .id(discussionId)
            .member(member)
            .aladinBook(mockAladinBook)
            .status(DiscussionStatus.PROPOSED)
            .build();

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(discussionRepository.findById(discussionId)).thenReturn(Optional.of(mockDiscussion));

        // When
        discussionServiceImpl.joinDisagree(memberId, discussionId);

        // Then
        ArgumentCaptor<DiscussionParticipant> captor = ArgumentCaptor.forClass(
            DiscussionParticipant.class);
        verify(discussionParticipantRepository).save(captor.capture());

        DiscussionParticipant savedParticipant = captor.getValue();
        assertEquals(mockDiscussion, savedParticipant.getDiscussion());
        assertEquals(member, savedParticipant.getMember());
        assertFalse(savedParticipant.isAgree());
    }

    @Test
    @DisplayName("토론 삭제 성공 테스트")
    void deleteDiscussion_shouldDeleteDiscussion() throws SchedulerException {
        // Given
        Long discussionId = 5L;

        Discussion mockDiscussion = Discussion.builder()
            .id(discussionId)
            .member(member)
            .aladinBook(mockAladinBook)
            .status(DiscussionStatus.PROPOSED)
            .views(0L)
            .isDeleted(false)
            .build();

        when(discussionRepository.findById(discussionId)).thenReturn(Optional.of(mockDiscussion));
        doNothing().when(quartzConfig).removeJob(anyLong());

        // When
        discussionServiceImpl.deleteDiscussion(memberId, discussionId);

        // Then
        verify(discussionRepository).findById(discussionId);
        verify(quartzConfig).removeJob(discussionId);

        assertTrue(mockDiscussion.isDeleted());
        assertNotNull(mockDiscussion.getDeletedAt());
    }
}