package com.undefinedus.backend.service;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.undefinedus.backend.domain.entity.AladinBook;
import com.undefinedus.backend.domain.entity.Discussion;
import com.undefinedus.backend.domain.entity.DiscussionParticipant;
import com.undefinedus.backend.domain.entity.Member;
import com.undefinedus.backend.domain.entity.MyBook;
import com.undefinedus.backend.domain.enums.DiscussionStatus;
import com.undefinedus.backend.dto.request.discussionComment.DiscussionScrollRequestDTO;
import com.undefinedus.backend.dto.response.ScrollResponseDTO;
import com.undefinedus.backend.repository.AladinBookRepository;
import com.undefinedus.backend.repository.DiscussionParticipantRepository;
import com.undefinedus.backend.repository.DiscussionRepository;
import com.undefinedus.backend.repository.MemberRepository;
import com.undefinedus.backend.repository.MyBookRepository;
import com.undefinedus.backend.scheduler.config.QuartzConfig;
import com.undefinedus.backend.dto.request.discussion.DiscussionUpdateRequestDTO;
import com.undefinedus.backend.dto.request.discussion.DiscussionRegisterRequestDTO;
import com.undefinedus.backend.dto.response.discussion.DiscussionListResponseDTO;
import com.undefinedus.backend.scheduler.job.Scheduled;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.quartz.SchedulerException;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
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
    private Scheduled scheduled;

    @Mock
    private AladinBookRepository aladinBookRepository;

    @Mock
    private DiscussionParticipantRepository discussionParticipantRepository;

    @InjectMocks
    private DiscussionServiceImpl discussionServiceImpl; // 구현체를 주입

    private Long memberId;
    private String isbn13;
    private DiscussionRegisterRequestDTO discussionRegisterRequestDTO;

    // 토론 생성 관련 설정
    @BeforeEach
    void setUpDiscussionDetails() {
        memberId = 1L;
        isbn13 = "1234567890123";
        discussionRegisterRequestDTO = DiscussionRegisterRequestDTO.builder()
            .title("Test Discussion")
            .content("This is a test discussion content")
            .startDate(LocalDateTime.now().plusDays(1)) // 토론 시작 시간을 현재로부터 1일 뒤로 설정
            .agree(5)
            .disagree(3)
            .build();
    }

    @Test
    @DisplayName("토론 생성 성공 테스트")
    void register_shouldReturnDiscussionId() throws Exception {
        // Mocking
        Member member = new Member();
        MyBook myBook = new MyBook();
        Discussion discussion = Discussion.builder()
            .id(1L)
            .title(discussionRegisterRequestDTO.getTitle())
            .content(discussionRegisterRequestDTO.getContent())
            .status(DiscussionStatus.PROPOSED)
            .startDate(discussionRegisterRequestDTO.getStartDate())
            .closedAt(discussionRegisterRequestDTO.getStartDate().plusDays(1))
            .build();

        when(memberRepository.findById(memberId)).thenReturn(java.util.Optional.of(member));
        when(myBookRepository.findByMemberIdAndIsbn13(memberId, isbn13)).thenReturn(
            java.util.Optional.of(myBook));
        when(discussionRepository.save(any(Discussion.class))).thenReturn(discussion);

        // Quartz Config Mock
        doNothing().when(quartzConfig).scheduleDiscussionJobs(any(LocalDateTime.class), anyLong());

        // 메서드 호출
        Long discussionId = discussionServiceImpl.discussionRegister(memberId, isbn13,
            discussionRegisterRequestDTO);

        // Assertions
        assertNotNull(discussionId);  // 반환된 토론 ID가 null이 아님을 확인
        assertEquals(1L, discussionId);  // 반환된 토론 ID가 1L인지 확인

        // quartzConfig.scheduleJobs 호출 여부 검증
        verify(quartzConfig, times(1)).scheduleDiscussionJobs(any(LocalDateTime.class), anyLong());

        // discussionRepository.save 호출 여부 검증
        verify(discussionRepository, times(1)).save(any(Discussion.class));
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

        // AladinBook mock 설정 추가
        for (Discussion discussion : mockDiscussions) {
            AladinBook mockAladinBook = new AladinBook();
            mockAladinBook.changeIsbn13(discussion.getMyBook().getIsbn13());
            mockAladinBook.changeCover("cover" + discussion.getId());
            mockAladinBook.changeTitle("Book Title " + discussion.getId());
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

        // 결과 출력
        System.out.println("토론 리스트 조회 결과:");
        for (DiscussionListResponseDTO dto : result.getContent()) {
            System.out.println("ID: " + dto.getDiscussionId());
            System.out.println("이미지: " + dto.getCover());
            System.out.println("Isbn13: " + dto.getIsbn13());
            System.out.println("조회수: " + dto.getViews());
            System.out.println("닉네임: " + dto.getMemberName());
            System.out.println("생성일자: " + dto.getCreatedDate());
            System.out.println("제목: " + dto.getTitle());
            System.out.println("찬성: " + dto.getAgree());
            System.out.println("반대: " + dto.getDisagree());
            System.out.println("상태: " + dto.getStatus());
            System.out.println("--------------------");
        }

        // Verify that the repository method was called with the correct parameters
        ArgumentCaptor<DiscussionScrollRequestDTO> captor = ArgumentCaptor.forClass(DiscussionScrollRequestDTO.class);
        verify(discussionRepository, times(1)).findDiscussionsWithScroll(captor.capture());
        DiscussionScrollRequestDTO capturedDTO = captor.getValue();
        assertEquals(0L, capturedDTO.getLastId());
        assertEquals(10, capturedDTO.getSize());
        assertEquals("desc", capturedDTO.getSort());
        assertEquals("PROPOSED", capturedDTO.getStatus());
    }

    private Discussion createMockDiscussion(Long id, String title, int agreeCount, int disagreeCount) {
        Discussion discussion = new Discussion();
        discussion.changeId(id);
        discussion.changeTitle(title);
        discussion.changeMember(new Member());
        discussion.getMember().setNickname("User " + id);
        discussion.changeMyBook(new MyBook());
        discussion.getMyBook().changeIsbn13("ISBN" + id);
        discussion.setCreatedDate(LocalDateTime.now());
//        discussion.changeViews(10L);

        List<DiscussionParticipant> participants = new ArrayList<>();
        for (int i = 0; i < agreeCount; i++) {
            participants.add(DiscussionParticipant.builder().isAgree(true).discussion(discussion).build());
        }
        for (int i = 0; i < disagreeCount; i++) {
            participants.add(DiscussionParticipant.builder().isAgree(false).discussion(discussion).build());
        }
        discussion.changeParticipants(participants);

        return discussion;
    }

    @Test
    @DisplayName("토론 수정 성공 테스트")
    void discussionModify_shouldModifyDiscussion() throws Exception {
        // Mock 데이터 설정
        Long memberId = 1L;
        String isbn13 = "1234567890123";
        Long discussionId = 10L;

        Member mockMember = new Member();
        mockMember.setId(memberId);

        MyBook mockMyBook = new MyBook();
        mockMyBook.changeIsbn13(isbn13);
        mockMyBook.changeMember(mockMember);

        Discussion mockDiscussion = new Discussion();
        mockDiscussion.changeId(discussionId);
        mockDiscussion.changeMember(mockMember);
        mockDiscussion.changeStatus(DiscussionStatus.PROPOSED);

        DiscussionUpdateRequestDTO requestDTO = DiscussionUpdateRequestDTO.builder()
            .title("Updated Title")
            .content("Updated Content")
            .modifyStartTime(LocalDateTime.now().plusHours(2))
            .build();

        // Mock 동작 정의
        when(memberRepository.findById(memberId)).thenReturn(java.util.Optional.of(mockMember));
        when(myBookRepository.findByMemberIdAndIsbn13(memberId, isbn13)).thenReturn(java.util.Optional.of(mockMyBook));
        when(discussionRepository.findById(discussionId)).thenReturn(java.util.Optional.of(mockDiscussion));
        when(discussionRepository.save(any(Discussion.class))).thenReturn(mockDiscussion);
        doNothing().when(quartzConfig).scheduleDiscussionJobs(any(LocalDateTime.class), anyLong());

        // 메서드 호출
        Long modifiedDiscussionId = discussionServiceImpl.discussionUpdate(memberId, isbn13, discussionId, requestDTO);

        // 결과 검증
        assertNotNull(modifiedDiscussionId);
        assertEquals(discussionId, modifiedDiscussionId);

        // Mock 객체 호출 여부 검증
        verify(discussionRepository, times(1)).save(any(Discussion.class));
        verify(quartzConfig, times(1)).scheduleDiscussionJobs(any(LocalDateTime.class), anyLong());

        // 업데이트된 값 검증
        assertEquals("Updated Title", mockDiscussion.getTitle());
        assertEquals("Updated Content", mockDiscussion.getContent());
    }

    @Test
    @DisplayName("찬성 참여 성공 테스트")
    void joinAgree_shouldSaveDiscussionParticipant() {
        // Mock 데이터 설정
        Long memberId = 1L;
        Long discussionId = 5L;

        Member mockMember = new Member();
        mockMember.setId(memberId);

        Discussion mockDiscussion = new Discussion();
        mockDiscussion.changeId(discussionId);

        // Mock 동작 정의
        when(memberRepository.findById(memberId)).thenReturn(java.util.Optional.of(mockMember));
        when(discussionRepository.findById(discussionId)).thenReturn(java.util.Optional.of(mockDiscussion));

        // 메서드 호출
        discussionServiceImpl.joinAgree(memberId, discussionId);

        // Mock 객체 호출 여부 검증
        ArgumentCaptor<DiscussionParticipant> captor = ArgumentCaptor.forClass(DiscussionParticipant.class);
        verify(discussionParticipantRepository, times(1)).save(captor.capture());

        // 저장된 DiscussionParticipant 검증
        DiscussionParticipant savedParticipant = captor.getValue();
        assertEquals(mockDiscussion, savedParticipant.getDiscussion());
        assertEquals(mockMember, savedParticipant.getMember());
        assertTrue(savedParticipant.isAgree());
    }

    @Test
    @DisplayName("토론에 반대 참여 성공 테스트")
    void joinDisagree_shouldSaveDiscussionParticipant() {
        // Given: 테스트에 사용할 데이터 설정
        Long memberId = 1L;
        Long discussionId = 1L;

        // 멤버 객체 생성
        Member mockMember = new Member();
        mockMember.setId(memberId);

        // 토론 객체 생성
        Discussion mockDiscussion = new Discussion();
        mockDiscussion.changeId(discussionId);

        // 반대 참여를 위한 DiscussionParticipant 객체 생성
        DiscussionParticipant expectedParticipant = DiscussionParticipant.builder()
            .discussion(mockDiscussion)
            .member(mockMember)
            .isAgree(false)  // 반대 참여이므로 isAgree는 false
            .build();

        // Mocking: 의존성 있는 객체들이 리턴할 값을 설정
        when(discussionRepository.findById(discussionId)).thenReturn(java.util.Optional.of(mockDiscussion));
        when(memberRepository.findById(memberId)).thenReturn(java.util.Optional.of(mockMember));

        // ArgumentCaptor를 사용하여 저장된 객체를 캡처할 준비
        ArgumentCaptor<DiscussionParticipant> captor = ArgumentCaptor.forClass(DiscussionParticipant.class);

        // When: 실제 메서드 호출
        discussionServiceImpl.joinDisagree(memberId, discussionId);

        // Then: save() 메서드가 한 번 호출되었는지 검증하고, 저장된 객체를 캡처
        verify(discussionParticipantRepository, times(1)).save(captor.capture());

        // 캡처된 DiscussionParticipant 객체가 기대하는 값과 일치하는지 검증
        DiscussionParticipant savedParticipant = captor.getValue();
        assertEquals(mockDiscussion, savedParticipant.getDiscussion());  // 저장된 토론이 맞는지 확인
        assertEquals(mockMember, savedParticipant.getMember());  // 저장된 멤버가 맞는지 확인
        assertFalse(savedParticipant.isAgree());  // 반대 참여이므로 isAgree는 false여야 함
    }

    @Test
    @DisplayName("토론 삭제 성공 테스트")
    void deleteDiscussion_shouldDeleteDiscussion() throws SchedulerException {
        // Given: 테스트에 사용할 데이터 설정
        Long memberId = 1L;
        Long discussionId = 1L;

        // 멤버 객체 생성
        Member mockMember = new Member();
        mockMember.setId(memberId);

        // 토론 객체 생성
        Discussion mockDiscussion = new Discussion();
        mockDiscussion.changeId(discussionId);
        mockDiscussion.changeMember(mockMember);  // 멤버가 해당 토론의 소유자임을 설정

        // Mocking: 토론과 멤버 객체를 반환하도록 설정
        when(discussionRepository.findById(discussionId)).thenReturn(java.util.Optional.of(mockDiscussion));

        // ArgumentCaptor를 사용하여 삭제된 토론을 캡처할 준비
        ArgumentCaptor<Long> captor = ArgumentCaptor.forClass(Long.class);

        // When: 실제 메서드 호출
        discussionServiceImpl.deleteDiscussion(memberId, discussionId);

        // Then: deleteById 메서드가 한 번 호출되었는지 확인
        verify(discussionRepository, times(1)).deleteById(captor.capture());  // deleteById가 호출되었는지 확인

        // 삭제된 토론의 ID가 기대하는 값과 일치하는지 검증
        assertEquals(discussionId, captor.getValue());  // 삭제된 토론의 ID가 맞는지 확인
    }

}