package com.undefinedus.backend.config;


import com.undefinedus.backend.repository.MemberRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

@Configuration
@Log4j2
@RequiredArgsConstructor
@Transactional
public class InitialDataConfig {
    
    private final MemberRepository memberRepository;
    
    @PostConstruct
    public void initMembers() {
    
    }
}
