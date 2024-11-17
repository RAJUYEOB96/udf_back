package com.undefinedus.backend.repository.queryDSL;

import com.undefinedus.backend.domain.entity.MyBook;
import com.undefinedus.backend.dto.request.ScrollRequestDTO;
import java.util.List;

public interface MyBookRepositoryCustom {
    List<MyBook> findBooksWithScroll(Long memberId, ScrollRequestDTO requestDTO);
}
