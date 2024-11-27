package com.undefinedus.backend.repository.queryDSL;

import com.undefinedus.backend.domain.entity.Discussion;
import com.undefinedus.backend.dto.request.DiscussionScrollRequestDTO;
import java.util.List;

public interface DiscussionRepositoryCustom {

    List<Discussion> findDiscussionsWithScroll(DiscussionScrollRequestDTO requestDTO);
}
