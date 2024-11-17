package com.undefinedus.backend.repository.queryDSL;

import com.undefinedus.backend.domain.entity.MyBookmark;
import com.undefinedus.backend.dto.request.ScrollRequestDTO;
import java.util.List;

public interface MyBookmarkRepositoryCustom {
    
    List<MyBookmark> findBookmarksWithScroll(Long memberId, ScrollRequestDTO requestDTO);
}
