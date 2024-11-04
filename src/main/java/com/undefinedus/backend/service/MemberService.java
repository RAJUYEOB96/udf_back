package com.undefinedus.backend.service;

import com.undefinedus.backend.domain.enums.PreferencesType;
import com.undefinedus.backend.dto.MemberDTO;
import java.util.Set;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface MemberService {

    void registerMember(MemberDTO memberDTO);

}
