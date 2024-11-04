package com.undefinedus.backend.service;

import com.undefinedus.backend.domain.entity.Member;
import com.undefinedus.backend.domain.enums.MemberType;
import com.undefinedus.backend.domain.enums.PreferencesType;
import com.undefinedus.backend.dto.MemberDTO;
import com.undefinedus.backend.repository.MemberRepository;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Log4j2
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void registerMember(MemberDTO memberDTO) {

        Member member = Member.builder()
            .username(memberDTO.getUsername())
            .password(passwordEncoder.encode(memberDTO.getPassword()))
            .nickname(memberDTO.getNickname())
            .memberRoleList(Collections.singletonList(MemberType.USER))
            .preferences(convertToPreferencesType(memberDTO.getPreferences()))
            .build();

        memberRepository.save(member);
    }

    private Set<PreferencesType> convertToPreferencesType(Set<String> preferences) {
        return preferences.stream()
            .map(pref -> {
                try {
                    return PreferencesType.valueOf(pref.toUpperCase());
                } catch (IllegalArgumentException e) {
                    return null;
                }
            })
            .filter(pref -> pref != null)
            .collect(Collectors.toSet());
    }

}
