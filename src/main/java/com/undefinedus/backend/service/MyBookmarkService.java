package com.undefinedus.backend.service;

import com.undefinedus.backend.dto.request.bookmark.BookmarkRequestDTO;
import jakarta.validation.Valid;

public interface MyBookmarkService {
    
    void insertBookMark(Long id, @Valid BookmarkRequestDTO requestDTO);
}
