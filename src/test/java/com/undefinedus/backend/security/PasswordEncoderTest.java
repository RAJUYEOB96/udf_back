package com.undefinedus.backend.security;

import static org.assertj.core.api.Assertions.assertThat;

import lombok.extern.log4j.Log4j2;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@Log4j2
class PasswordEncoderTest {
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Test
    @DisplayName("패스워드 인코딩 테스트")
    void testPasswordEncoding() {
        // given
        String rawPassword = "testPassword123";
        
        // when
        String encodedPassword = passwordEncoder.encode(rawPassword);
        
        // then
        assertThat(encodedPassword).isNotEqualTo(rawPassword);
        assertThat(passwordEncoder.matches(rawPassword, encodedPassword)).isTrue();
    }
    
    @Test
    @DisplayName("서로 다른 해시값 생성 테스트")
    void testDifferentHash() {
        // given
        String password = "testPassword123";
        
        // when
        String firstHash = passwordEncoder.encode(password);
        String secondHash = passwordEncoder.encode(password);
        
        // then
        assertThat(firstHash).isNotEqualTo(secondHash);
        assertThat(passwordEncoder.matches(password, firstHash)).isTrue();
        assertThat(passwordEncoder.matches(password, secondHash)).isTrue();
    }
}
