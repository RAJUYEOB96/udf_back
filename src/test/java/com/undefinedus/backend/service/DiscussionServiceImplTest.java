package com.undefinedus.backend.service;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.undefinedus.backend.domain.entity.AladinBook;
import com.undefinedus.backend.domain.entity.Discussion;
import com.undefinedus.backend.domain.entity.DiscussionParticipant;
import com.undefinedus.backend.domain.entity.Member;
import com.undefinedus.backend.domain.entity.MyBook;
import com.undefinedus.backend.domain.enums.BookStatus;
import com.undefinedus.backend.domain.enums.DiscussionStatus;
import com.undefinedus.backend.domain.enums.MemberType;
import com.undefinedus.backend.domain.enums.PreferencesType;
import com.undefinedus.backend.dto.request.discussion.DiscussionRegisterRequestDTO;
import com.undefinedus.backend.dto.request.discussion.DiscussionUpdateRequestDTO;
import com.undefinedus.backend.dto.request.discussionComment.DiscussionScrollRequestDTO;
import com.undefinedus.backend.dto.response.ScrollResponseDTO;
import com.undefinedus.backend.dto.response.discussion.DiscussionListResponseDTO;
import com.undefinedus.backend.repository.AladinBookRepository;
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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.SchedulerException;

@ExtendWith(MockitoExtension.class)
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
    
    @InjectMocks
    private DiscussionServiceImpl discussionServiceImpl;
    
    private Long memberId;
    private String isbn13;
    
    @BeforeEach
    void setUp() {
        memberId = 1L;
        isbn13 = "1234567890123";
    }
    
    @Test
    @DisplayName("토론 생성 성공 테스트")
    void discussionRegister_shouldReturnDiscussionId() throws Exception {
        // Given
        LocalDateTime startDate = LocalDateTime.now().plusDays(1);
        
        Member mockMember = Member.builder()
                .id(memberId)
                .nickname("testuser")
                .build();
        
        MyBook mockMyBook = MyBook.builder()
                .id(1L)
                .isbn13(isbn13)
                .member(mockMember)
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
                .myBook(mockMyBook)
                .member(mockMember)
                .title(requestDTO.getTitle())
                .content(requestDTO.getContent())
                .status(DiscussionStatus.PROPOSED)
                .startDate(startDate)
                .closedAt(startDate.plusDays(1))
                .build();
        
        // Mocking
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(mockMember));
        when(myBookRepository.findByMemberIdAndIsbn13(memberId, isbn13)).thenReturn(Optional.of(mockMyBook));
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
    @DisplayName("토론 리스트 조회 테스트")
    void getDiscussionList_shouldReturnScrollResponseDTO() {
        // Given
        DiscussionScrollRequestDTO requestDTO = DiscussionScrollRequestDTO.builder()
                .lastId(0L)
                .size(10)
                .sort("desc")
                .status("PROPOSED")
                .build();
        
        List<Discussion> mockDiscussions = Arrays.asList(
                createMockDiscussion(1L, "Discussion 1", 5, 3),
                createMockDiscussion(2L, "Discussion 2", 4, 2)
        );
        
        for (Discussion discussion : mockDiscussions) {
            AladinBook mockAladinBook = AladinBook.builder()
                    .isbn13(discussion.getMyBook().getIsbn13())
                    .cover("cover" + discussion.getId())
                    .title("Book Title " + discussion.getId())
                    .build();
            when(aladinBookRepository.findByIsbn13(discussion.getMyBook().getIsbn13()))
                    .thenReturn(Optional.of(mockAladinBook));
        }
        
        when(discussionRepository.findDiscussionsWithScroll(any(DiscussionScrollRequestDTO.class)))
                .thenReturn(mockDiscussions);
        
        // When
        ScrollResponseDTO<DiscussionListResponseDTO> result = discussionServiceImpl.getDiscussionList(requestDTO);
        
        // Then
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(2L, result.getTotalElements());
        assertEquals(2, result.getNumberOfElements());
        assertEquals(2L, result.getLastId());
        assertFalse(result.isHasNext());
        
        DiscussionListResponseDTO firstDiscussion = result.getContent().get(0);
        assertEquals(1L, firstDiscussion.getDiscussionId());
        assertEquals("Discussion 1", firstDiscussion.getTitle());
        assertEquals(5L, firstDiscussion.getAgree());
        assertEquals(3L, firstDiscussion.getDisagree());
        
        ArgumentCaptor<DiscussionScrollRequestDTO> captor = ArgumentCaptor.forClass(DiscussionScrollRequestDTO.class);
        verify(discussionRepository).findDiscussionsWithScroll(captor.capture());
        DiscussionScrollRequestDTO capturedDTO = captor.getValue();
        assertEquals(0L, capturedDTO.getLastId());
        assertEquals(10, capturedDTO.getSize());
        assertEquals("desc", capturedDTO.getSort());
        assertEquals("PROPOSED", capturedDTO.getStatus());
    }
    
    private Discussion createMockDiscussion(Long id, String title, int agreeCount, int disagreeCount) {
        Member mockMember = Member.builder()
                .id(id)
                .nickname("User " + id)
                .build();
        
        MyBook mockMyBook = MyBook.builder()
                .id(id)
                .isbn13("ISBN" + id)
                .member(mockMember)
                .status(BookStatus.COMPLETED)
                .build();
        
        Discussion discussion = Discussion.builder()
                .id(id)
                .title(title)
                .member(mockMember)
                .myBook(mockMyBook)
                .status(DiscussionStatus.PROPOSED)
                .startDate(LocalDateTime.now())
                .closedAt(LocalDateTime.now().plusDays(1))
                .build();
        
        List<DiscussionParticipant> participants = new ArrayList<>();
        for (int i = 0; i < agreeCount; i++) {
            participants.add(DiscussionParticipant.builder()
                    .discussion(discussion)
                    .member(mockMember)
                    .isAgree(true)
                    .build());
        }
        for (int i = 0; i < disagreeCount; i++) {
            participants.add(DiscussionParticipant.builder()
                    .discussion(discussion)
                    .member(mockMember)
                    .isAgree(false)
                    .build());
        }
        discussion.changeParticipants(participants);
        
        return discussion;
    }
    
    @Test
    @DisplayName("토론 수정 성공 테스트")
    void discussionModify_shouldModifyDiscussion() throws Exception {
        // Given
        Long discussionId = 10L;
        
        Member mockMember = Member.builder()
                .id(memberId)
                .build();
        
        MyBook mockMyBook = MyBook.builder()
                .id(1L)
                .isbn13(isbn13)
                .member(mockMember)
                .status(BookStatus.COMPLETED)
                .build();
        
        Discussion mockDiscussion = Discussion.builder()
                .id(discussionId)
                .member(mockMember)
                .status(DiscussionStatus.PROPOSED)
                .build();
        
        DiscussionUpdateRequestDTO requestDTO = DiscussionUpdateRequestDTO.builder()
                .title("Updated Title")
                .content("Updated Content")
                .modifyStartTime(LocalDateTime.now().plusHours(2))
                .build();
        
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(mockMember));
        when(myBookRepository.findByMemberIdAndIsbn13(memberId, isbn13)).thenReturn(Optional.of(mockMyBook));
        when(discussionRepository.findById(discussionId)).thenReturn(Optional.of(mockDiscussion));
        when(discussionRepository.save(any(Discussion.class))).thenReturn(mockDiscussion);
        
        // When
        Long modifiedDiscussionId = discussionServiceImpl.discussionUpdate(memberId, isbn13, discussionId, requestDTO);
        
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
        
        Member mockMember = Member.builder()
                .id(memberId)
                .build();
        
        Discussion mockDiscussion = Discussion.builder()
                .id(discussionId)
                .status(DiscussionStatus.PROPOSED)
                .build();
        
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(mockMember));
        when(discussionRepository.findById(discussionId)).thenReturn(Optional.of(mockDiscussion));
        
        // When
        discussionServiceImpl.joinAgree(memberId, discussionId);
        
        // Then
        ArgumentCaptor<DiscussionParticipant> captor = ArgumentCaptor.forClass(DiscussionParticipant.class);
        verify(discussionParticipantRepository).save(captor.capture());
        
        DiscussionParticipant savedParticipant = captor.getValue();
        assertEquals(mockDiscussion, savedParticipant.getDiscussion());
        assertEquals(mockMember, savedParticipant.getMember());
        assertTrue(savedParticipant.isAgree());
    }
    
    @Test
    @DisplayName("반대 참여 성공 테스트")
    void joinDisagree_shouldSaveDiscussionParticipant() {
        // Given
        Long discussionId = 5L;
        
        Member mockMember = Member.builder()
                .id(memberId)
                .build();
        
        Discussion mockDiscussion = Discussion.builder()
                .id(discussionId)
                .status(DiscussionStatus.PROPOSED)
                .build();
        
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(mockMember));
        when(discussionRepository.findById(discussionId)).thenReturn(Optional.of(mockDiscussion));
        
        // When
        discussionServiceImpl.joinDisagree(memberId, discussionId);
        
        // Then
        ArgumentCaptor<DiscussionParticipant> captor = ArgumentCaptor.forClass(DiscussionParticipant.class);
        verify(discussionParticipantRepository).save(captor.capture());
        
        DiscussionParticipant savedParticipant = captor.getValue();
        assertEquals(mockDiscussion, savedParticipant.getDiscussion());
        assertEquals(mockMember, savedParticipant.getMember());
        assertFalse(savedParticipant.isAgree());
    }
    
    @Test
    @DisplayName("토론 삭제 성공 테스트")
    void deleteDiscussion_shouldDeleteDiscussion() throws SchedulerException {
        // Given
        Long discussionId = 5L;
        
        Member mockMember = Member.builder()
                .id(memberId)
                .build();
        
        Discussion mockDiscussion = Discussion.builder()
                .id(discussionId)
                .member(mockMember)
                .status(DiscussionStatus.PROPOSED)
                .views(0L)
                .isDeleted(false)
                .build();
        
        when(discussionRepository.findById(discussionId)).thenReturn(Optional.of(mockDiscussion));
        doNothing().when(scheduled).removeJob(anyLong(), any(DiscussionStatus.class));
        
        // When
        discussionServiceImpl.deleteDiscussion(memberId, discussionId);
        
        // Then
        verify(discussionRepository).findById(discussionId);
        verify(scheduled).removeJob(discussionId, mockDiscussion.getStatus());
        
        assertTrue(mockDiscussion.isDeleted());
        assertNotNull(mockDiscussion.getDeletedAt());
    }
}