package com.undefinedus.backend.service;

import com.undefinedus.backend.domain.entity.AladinBook;
import com.undefinedus.backend.domain.entity.Member;
import com.undefinedus.backend.domain.entity.MyBookmark;
import com.undefinedus.backend.dto.request.bookmark.BookmarkRequestDTO;
import com.undefinedus.backend.exception.book.BookNotFoundException;
import com.undefinedus.backend.exception.member.MemberNotFoundException;
import com.undefinedus.backend.repository.AladinBookRepository;
import com.undefinedus.backend.repository.MemberRepository;
import com.undefinedus.backend.repository.MyBookmarkRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Log4j2
@Transactional
public class MyBookmarkServiceImpl implements MyBookmarkService{
    
    private static final String AladinBook_NOT_FOUND = "해당 책을 찾을 수 없습니다. : %s";
    private static final String USER_NOT_FOUND = "해당 유저를 찾을 수 없습니다. : %d";
    
    
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
                .pageNumber(requestDTO.getBookmarkPage())
                .build();
        
        myBookmarkRepository.save(myBookmark);
    }
}
