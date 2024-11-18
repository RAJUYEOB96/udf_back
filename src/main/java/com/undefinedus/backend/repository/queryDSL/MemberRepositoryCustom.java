package com.undefinedus.backend.repository.queryDSL;

import com.undefinedus.backend.domain.entity.Member;
import com.undefinedus.backend.dto.request.ScrollRequestDTO;
import java.util.List;

public interface MemberRepositoryCustom {
    
    List<Member> findAllWithoutMemberId(Long memberId, ScrollRequestDTO requestDTO);
}
