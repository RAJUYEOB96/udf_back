package com.undefinedus.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.undefinedus.backend.domain.entity.Member;
import com.undefinedus.backend.domain.enums.PreferencesType;
import com.undefinedus.backend.dto.response.aladinAPI.AladinApiResponseDTO;
import com.undefinedus.backend.repository.MemberRepository;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Log4j2
class ChatGPTServiceImplTests {
    
    @MockBean  // Spring Bean을 Mock으로 대체
    private ChatClient chatClient;

    @Autowired
    private ChatGPTService chatGPTService;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("테스트")
    @Transactional
    public void preference() {

        Long memberId = 2L;

        Optional<Member> opt = memberRepository.findById(memberId);
        Member member = opt.get();
        Set<PreferencesType> ps = member.getPreferences();
        Iterator<PreferencesType> iter = ps.iterator();

        System.out.println(ps);
        while(iter.hasNext()) {
            PreferencesType pt = iter.next();
            System.out.println(pt.getCategoryId());
        }
    }
}