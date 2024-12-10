package com.undefinedus.backend.service;

import com.undefinedus.backend.domain.entity.AladinBook;
import com.undefinedus.backend.domain.entity.Discussion;
import com.undefinedus.backend.domain.entity.DiscussionParticipant;
import com.undefinedus.backend.domain.entity.Member;
import com.undefinedus.backend.domain.entity.MyBook;
import com.undefinedus.backend.domain.enums.DiscussionStatus;
import com.undefinedus.backend.dto.request.discussion.DiscussionRegisterRequestDTO;
import com.undefinedus.backend.dto.request.discussion.DiscussionUpdateRequestDTO;
import com.undefinedus.backend.dto.request.discussionComment.DiscussionScrollRequestDTO;
import com.undefinedus.backend.dto.response.ScrollResponseDTO;
import com.undefinedus.backend.dto.response.discussion.DiscussionDetailResponseDTO;
import com.undefinedus.backend.dto.response.discussion.DiscussionListResponseDTO;
import com.undefinedus.backend.exception.aladinBook.AladinBookNotFoundException;
import com.undefinedus.backend.exception.book.BookNotFoundException;
import com.undefinedus.backend.exception.discussion.DiscussionException;
import com.undefinedus.backend.exception.discussion.DiscussionNotFoundException;
import com.undefinedus.backend.exception.member.MemberNotFoundException;
import com.undefinedus.backend.repository.AladinBookRepository;
import com.undefinedus.backend.repository.DiscussionParticipantRepository;
import com.undefinedus.backend.repository.DiscussionRepository;
import com.undefinedus.backend.repository.MemberRepository;
import com.undefinedus.backend.repository.MyBookRepository;
import com.undefinedus.backend.scheduler.config.QuartzConfig;
import com.undefinedus.backend.scheduler.entity.QuartzTrigger;
import com.undefinedus.backend.scheduler.job.Scheduled;
import com.undefinedus.backend.scheduler.repository.QuartzTriggerRepository;
import java.sql.Date;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.quartz.SchedulerException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Log4j2
public class DiscussionServiceImpl implements DiscussionService {

    private final DiscussionRepository discussionRepository;
    private final MemberRepository memberRepository;
    private final MyBookRepository myBookRepository;
    private final QuartzConfig quartzConfig;
    private final AladinBookRepository aladinBookRepository;
    private final Scheduled scheduled;
    private final DiscussionParticipantRepository discussionParticipantRepository;
    private final QuartzTriggerRepository quartzTriggerRepository;

    @Override
    public Long discussionRegister(Long memberId, DiscussionRegisterRequestDTO discussionRegisterRequestDTO) {

        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new MemberNotFoundException("해당 사용자를 찾을 수 없습니다. : " + memberId));

        String isbn13 = discussionRegisterRequestDTO.getIsbn13();

        MyBook myBook = myBookRepository.findByMemberIdAndIsbn13(memberId, isbn13)
            .orElseThrow(() -> new BookNotFoundException("해당 책을 찾을 수 없습니다. : " + isbn13));

        Discussion discussion = Discussion.builder()
            .myBook(myBook)  // MyBook 객체
            .member(member)  // Member 객체
            .title(discussionRegisterRequestDTO.getTitle())
            .content(discussionRegisterRequestDTO.getContent())
            .status(DiscussionStatus.PROPOSED)
            .startDate(discussionRegisterRequestDTO.getStartDate())
            .closedAt(discussionRegisterRequestDTO.getStartDate().plusDays(1)) // 하루 뒤 토론 마감
            .build();

        Discussion savedDiscussion = discussionRepository.save(discussion);
        log.info(savedDiscussion.toString());

        // 상태 변경 작업 스케줄링
        try {
            quartzConfig.scheduleDiscussionJobs(savedDiscussion.getStartDate(),
                savedDiscussion.getId());
        } catch (SchedulerException e) {
            log.error(
                "토론 상태 변경 작업 스케줄링에 실패했습니다. 토론 ID: " + savedDiscussion.getId(),
                e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return savedDiscussion.getId();
    }

    @Override
    public Discussion changeStatus(Long discussionId, DiscussionStatus discussionStatus) {

        Discussion discussion = discussionRepository.findById(discussionId).orElseThrow();

        discussion.changeStatus(discussionStatus);

        Discussion discussionSaved = discussionRepository.save(discussion);

        return discussionSaved;
    }

    @Override
    public ScrollResponseDTO<DiscussionListResponseDTO> getDiscussionList(
        DiscussionScrollRequestDTO discussionScrollRequestDTO) {

        List<Discussion> discussionList = discussionRepository.findDiscussionsWithScroll(
            discussionScrollRequestDTO);

        boolean hasNext = false;
        if (discussionList.size() > discussionScrollRequestDTO.getSize()) { // 11 > 10 이면 있다는 뜻
            hasNext = true;
            discussionList.remove(discussionList.size() - 1); // 11개 가져온 걸 10개를 보내기 위해
        }

        // 결과를 담을 리스트
        List<DiscussionListResponseDTO> responseDTOList = new ArrayList<>();

        for (Discussion discussion : discussionList) {
            // 각 토론의 관련 정보를 추출
            String memberName = discussion.getMember().getNickname();
            String title = discussion.getTitle();
            Long agree = discussion.getParticipants().stream().filter(isAgree -> isAgree.isAgree())
                .count();  // 찬성 참여자 수
            Long disagree = discussion.getParticipants().stream()
                .filter(disAgree -> !disAgree.isAgree()).count();  // 반대 참여자 수
            LocalDateTime createdDate = discussion.getCreatedDate();
            Long views = discussion.getViews();
            Long discussionId = discussion.getId();
            String isbn13 = discussion.getMyBook().getIsbn13();
            LocalDateTime startDateTime = discussion.getStartDate();
            LocalDateTime closedAt = discussion.getClosedAt();

            AladinBook aladinBook = aladinBookRepository.findByIsbn13(isbn13).orElseThrow(
                () -> new AladinBookNotFoundException("없는 ISBN13 입니다. : " + isbn13)
            );

            String cover = aladinBook.getCover();

            // DTO 객체 생성 후 리스트에 추가
            DiscussionListResponseDTO dto = DiscussionListResponseDTO.builder()
                .discussionId(discussionId)
                .isbn13(isbn13)
                .bookTitle(aladinBook.getTitle())
                .memberName(memberName)
                .title(title)
                .agree(agree)
                .disagree(disagree)
                .createdDate(createdDate)
                .startDateTime(startDateTime)
                .closedAt(closedAt)
                .views(views)
                .cover(cover)
                .status(String.valueOf(discussion.getStatus()))
                .agreePercent(discussion.getAgreePercent())
                .disagreePercent(discussion.getDisagreePercent())
                .build();

            responseDTOList.add(dto);
        }

        // 마지막 항목의 ID 설정
        Long lastId = discussionList.isEmpty() ?
            discussionScrollRequestDTO.getLastId() :    // 조회된 목록이 비어있는 경우를 대비해 삼항 연산자 사용
            discussionList.get(discussionList.size() - 1)
                .getId(); // lastId를 요청 DTO의 값이 아닌, 실제 조회된 마지막 항목의 ID로 설정

        return ScrollResponseDTO.<DiscussionListResponseDTO>withAll()
            .content(responseDTOList)
            .hasNext(hasNext)
            .lastId(lastId) // 조회된 목록의 마지막 항목의 ID ? INDEX ?
            .numberOfElements(responseDTOList.size())
            .totalElements(discussionList.stream().count())
            .build();
    }

    @Override
    public DiscussionDetailResponseDTO getDiscussionDetail(Long discussionId) {

        Discussion discussion = discussionRepository.findById(discussionId)
            .orElseThrow(() -> new DiscussionException("해당 토론방을 찾을 수 없습니다. : " + discussionId));

        String isbn13 = discussion.getMyBook().getIsbn13();

        AladinBook discussionBook = aladinBookRepository.findByIsbn13(isbn13).orElseThrow();

        long agreeCount = discussion.getParticipants().stream().filter(agree -> agree.isAgree())
            .count();

        long disagreeCount = discussion.getParticipants().size() - agreeCount;

        discussion.increaseViews();

        Discussion savedDiscussion = discussionRepository.save(discussion);

        DiscussionDetailResponseDTO discussionDetailResponseDTO = DiscussionDetailResponseDTO.builder()
            .discussionId(discussionId)
            .bookTitle(discussionBook.getTitle())
            .memberName(discussion.getMember().getNickname())
            .title(savedDiscussion.getTitle())
            .content(savedDiscussion.getContent())
            .agree(agreeCount)
            .disagree(disagreeCount)
            .startDate(savedDiscussion.getStartDate())
            .closedAt(savedDiscussion.getStartDate().plusDays(1))
            .createdDate(savedDiscussion.getCreatedDate())
            .views(savedDiscussion.getViews())
            .commentCount(savedDiscussion.getComments().stream().count())
            .cover(discussionBook.getCover())
            .status(String.valueOf(savedDiscussion.getStatus()))
            .agreePercent(savedDiscussion.getAgreePercent())
            .disagreePercent(savedDiscussion.getDisagreePercent())
            .build();

        return discussionDetailResponseDTO;
    }

    @Override
    public Long discussionUpdate(Long memberId, String isbn13, Long discussionId,
        DiscussionUpdateRequestDTO discussionUpdateRequestDTO) throws Exception {

        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new MemberNotFoundException("해당 멤버를 찾을 수 없습니다. : " + memberId));

        MyBook myBook = myBookRepository.findByMemberIdAndIsbn13(memberId, isbn13)
            .orElseThrow(() -> new BookNotFoundException("해당 책을 찾을 수 없습니다. : " + isbn13));

        Discussion discussion = discussionRepository.findById(discussionId).orElseThrow(
            () -> new DiscussionNotFoundException("해당 토론을 찾을 수 없습니다. : " + discussionId));
        
        List<QuartzTrigger> quartzTriggers =
                quartzTriggerRepository.findAllBySchedName("discussion ID : " + discussionId);

        if (member == discussion.getMember()
            && discussion.getStatus() == DiscussionStatus.PROPOSED && discussion.getParticipants()
            .isEmpty()) {
            
            discussion.changeMyBook(myBook);
            discussion.changeTitle(discussionUpdateRequestDTO.getTitle());
            discussion.changeContent(discussionUpdateRequestDTO.getContent());
            discussion.changeStartDate(discussionUpdateRequestDTO.getModifyStartTime());

        }

        Discussion save = discussionRepository.save(discussion);
        
        for (QuartzTrigger quartzTrigger : quartzTriggers) {
            quartzTrigger.updateStartTimeEasy(Date.from(save.getStartDate().atZone(ZoneId.systemDefault()).toInstant()));
        }

        return save.getId();
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void joinAgree(Long memberId, Long discussionId) {

        Discussion discussion = discussionRepository.findById(discussionId).orElseThrow(
            () -> new DiscussionNotFoundException("해당 토론을 찾을 수 없습니다. : " + discussionId));

        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new MemberNotFoundException("해당 멤버를 찾을 수 없습니다. : " + memberId));

        DiscussionParticipant savedParticipant = discussionParticipantRepository.findByDiscussionAndMember(
            discussion, member).orElse(null);

        if (discussion.getStatus() == DiscussionStatus.PROPOSED) {

            if (savedParticipant == null) {
                DiscussionParticipant discussionParticipant = DiscussionParticipant.builder()
                    .discussion(discussion)
                    .member(member)
                    .isAgree(true)
                    .build();

                discussionParticipantRepository.save(discussionParticipant);
            } else {
                if (!savedParticipant.isAgree()) {

                    discussionParticipantRepository.deleteById(savedParticipant.getId());

                    DiscussionParticipant discussionParticipant = DiscussionParticipant.builder()
                        .discussion(discussion)
                        .member(member)
                        .isAgree(true)
                        .build();
                    discussionParticipantRepository.save(discussionParticipant);
                    return;
                }
                discussionParticipantRepository.delete(savedParticipant);
            }
        }
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void joinDisagree(Long memberId, Long discussionId) {

        Discussion discussion = discussionRepository.findById(discussionId).orElseThrow(
            () -> new DiscussionNotFoundException("해당 토론을 찾을 수 없습니다. : " + discussionId));

        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new MemberNotFoundException("해당 멤버를 찾을 수 없습니다. : " + memberId));

        DiscussionParticipant savedParticipant = discussionParticipantRepository.findByDiscussionAndMember(
            discussion, member).orElse(null);

        if (discussion.getStatus() == DiscussionStatus.PROPOSED) {
            if (savedParticipant == null) {
                DiscussionParticipant discussionParticipant = DiscussionParticipant.builder()
                    .discussion(discussion)
                    .member(member)
                    .isAgree(false)
                    .build();

                discussionParticipantRepository.save(discussionParticipant);
            } else {
                if (savedParticipant.isAgree()) {

                    discussionParticipantRepository.deleteById(savedParticipant.getId());

                    DiscussionParticipant discussionParticipant = DiscussionParticipant.builder()
                        .discussion(discussion)
                        .member(member)
                        .isAgree(false)
                        .build();

                    discussionParticipantRepository.save(discussionParticipant);
                    return;
                }
                discussionParticipantRepository.delete(savedParticipant);
            }
        }
    }

    @Override
    public void deleteDiscussion(Long memberId, Long discussionId) throws SchedulerException {

        Discussion discussion = discussionRepository.findById(discussionId).orElseThrow(
            () -> new DiscussionNotFoundException("해당 토론을 찾을 수 없습니다. : " + discussionId));

        if (Objects.equals(discussion.getMember().getId(), memberId)) {

            discussion.changeDeleted(true);
            discussion.changeDeletedAt(LocalDateTime.now());
            scheduled.removeJob(discussionId, discussion.getStatus());
        } else {
            throw new DiscussionException("해당 discussion을 만든 회원이 아닙니다. memberId : " + memberId);
        }
    }
}
