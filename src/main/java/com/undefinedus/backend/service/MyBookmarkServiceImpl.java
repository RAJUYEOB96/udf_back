package com.undefinedus.backend.service;

import com.undefinedus.backend.domain.entity.AladinBook;
import com.undefinedus.backend.domain.entity.Member;
import com.undefinedus.backend.domain.entity.MyBookmark;
import com.undefinedus.backend.dto.request.ScrollRequestDTO;
import com.undefinedus.backend.dto.request.bookmark.BookmarkRequestDTO;
import com.undefinedus.backend.dto.request.bookmark.MyBookmarkUpdateRequestDTO;
import com.undefinedus.backend.dto.response.ScrollResponseDTO;
import com.undefinedus.backend.dto.response.bookmark.MyBookmarkResponseDTO;
import com.undefinedus.backend.exception.book.BookNotFoundException;
import com.undefinedus.backend.exception.bookmark.BookmarkNotFoundException;
import com.undefinedus.backend.exception.member.MemberNotFoundException;
import com.undefinedus.backend.repository.AladinBookRepository;
import com.undefinedus.backend.repository.MemberRepository;
import com.undefinedus.backend.repository.MyBookmarkRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Log4j2
@Transactional
public class MyBookmarkServiceImpl implements MyBookmarkService{
    
    // === 에러 메시지 상수 === //
    private static final String AladinBook_NOT_FOUND = "해당 책을 찾을 수 없습니다. : %s";
    private static final String USER_NOT_FOUND = "해당 유저를 찾을 수 없습니다. : %d";
    private static final String BOOKMARK_NOT_FOUND = "해당 기록된 북마크를 찾을 수 없습니다. : 멤버 id - %d, 책갈피 id - %d";
    
    // === Repository 주입 === //
    private final MyBookmarkRepository myBookmarkRepository;
    private final AladinBookRepository aladinBookRepository;
    private final MemberRepository memberRepository;
    
    
    @Override
    public void insertBookMark(Long memberId, BookmarkRequestDTO requestDTO) {
        
        AladinBook findAladinBook = aladinBookRepository.findByTitle(requestDTO.getTitle())
                .orElseThrow(() -> new BookNotFoundException(String.format(AladinBook_NOT_FOUND, requestDTO.getTitle())));
        
        Member findMember = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException(String.format(USER_NOT_FOUND, memberId)));
        
        MyBookmark myBookmark = MyBookmark.builder()
                .aladinBook(findAladinBook)
                .member(findMember)
                .phrase(requestDTO.getPhrase())
                .pageNumber(requestDTO.getPageNumber())
                .build();
        
        myBookmarkRepository.save(myBookmark);
    }
    
    @Override
    public ScrollResponseDTO<MyBookmarkResponseDTO> getMyBookmarkList(Long memberId, ScrollRequestDTO requestDTO) {
        // findBooksWithScroll안에서 size + 1개 데이터 조회해서 가져옴 (size가 10이면 11개 가져옴)
        
        List<MyBookmark> myBookmarks = myBookmarkRepository.findBookmarksWithScroll(memberId, requestDTO);
        
        boolean hasNext = false;
        if (myBookmarks.size() > requestDTO.getSize()) { // 11 > 10 이면 있다는 뜻
            hasNext = true;
            myBookmarks.remove(myBookmarks.size() - 1); // 11개 가져온 걸 10개를 보내기 위해
        }
        
        List<MyBookmarkResponseDTO> dtoList = myBookmarks.stream()
                .map(myBookmark -> {
                    return MyBookmarkResponseDTO.from(myBookmark);
                })
                .collect(Collectors.toList());
        
        // 마지막 항목의 ID 설정
        Long lastId = myBookmarks.isEmpty() ?
                requestDTO.getLastId() :    // 조회된 목록이 비어있는 경우를 대비해 삼항 연산자 사용
                myBookmarks.get(myBookmarks.size() - 1).getId(); // lastId를 요청 DTO의 값이 아닌, 실제 조회된 마지막 항목의 ID로 설정
        
        return ScrollResponseDTO.<MyBookmarkResponseDTO>withAll()
                .content(dtoList)
                .hasNext(hasNext)
                .lastId(lastId)
                .numberOfElements(dtoList.size())
                .build();
    }
    
    @Override
    public void updateMyBookmark(Long memberId, Long bookmarkId, MyBookmarkUpdateRequestDTO requestDTO) {
    
        MyBookmark findMyBookmark = myBookmarkRepository.findByIdAndMemberId(bookmarkId, memberId)
                .orElseThrow(() -> new BookmarkNotFoundException(String.format(BOOKMARK_NOT_FOUND, memberId, bookmarkId)));
        
        findMyBookmark.updateMyBookmark(requestDTO);
    
    }
}
