package com.undefinedus.backend.controller;

import com.undefinedus.backend.dto.MemberDTO;
import com.undefinedus.backend.repository.MemberRepository;
import com.undefinedus.backend.service.MemberService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Log4j2
public class MemberController {

    private final MemberService memberService;
    private final MemberRepository memberRepository;

    @PostMapping("/api/member/register")
    public Map<String, String> memberRegister(@RequestBody MemberDTO memberDTO) {

        try {

            memberService.registerMember(memberDTO);

            return Map.of("result", "success");

        } catch (Exception e) {

            return Map.of("result", "fail");
        }
    }
}
