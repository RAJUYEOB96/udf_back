package com.undefinedus.backend.service;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Log4j2
class ChatGPTServiceImplTests {

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

    @Test
    @DisplayName("gpt 추천 도서 테스트")
    public void getGPTRecommendedBookLIst() {

        Long memberId = 2L;

        List<AladinApiResponseDTO> booksByIsbn = chatGPTService.getGPTRecommendedBookLIst(
            memberId);

        System.out.println(booksByIsbn);
        System.out.println(booksByIsbn.size());
    }

}