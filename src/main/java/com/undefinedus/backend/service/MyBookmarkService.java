package com.undefinedus.backend.service;

import com.undefinedus.backend.dto.request.ScrollRequestDTO;
import com.undefinedus.backend.dto.request.bookmark.BookmarkRequestDTO;
import com.undefinedus.backend.dto.request.bookmark.MyBookmarkUpdateRequestDTO;
import com.undefinedus.backend.dto.response.ScrollResponseDTO;
import com.undefinedus.backend.dto.response.bookmark.MyBookmarkResponseDTO;
import jakarta.validation.Valid;

public interface MyBookmarkService {
    
    void insertBookMark(Long id, @Valid BookmarkRequestDTO requestDTO);
    
    ScrollResponseDTO<MyBookmarkResponseDTO> getMyBookmarkList(Long memberId, ScrollRequestDTO requestDTO);
    
    void updateMyBookmark(Long id, Long bookmarkId, @Valid MyBookmarkUpdateRequestDTO requestDTO);
    
    void deleteMyBookmark(Long memberId, Long bookmarkId);
    
}
