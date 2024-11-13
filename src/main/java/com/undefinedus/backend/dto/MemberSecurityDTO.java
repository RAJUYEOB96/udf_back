package com.undefinedus.backend.dto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Getter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

@Getter
public class MemberSecurityDTO extends User { // User 때문에 Builder 패턴 적용하기 까다로워 안썼음

    // id, username, nickname, socialLogin memberRoleList 만 보여주면 될듯
    private final String nickname;
    private final List<String> roles;
    private String socialProvider;


    public MemberSecurityDTO(String username, String password, String nickname, List<String> roles, String socialProvider) {
        super(username, password,
            roles.stream().map(str -> new SimpleGrantedAuthority("ROLE_"+str)).collect(
                Collectors.toList()));

        this.nickname = nickname;
        this.roles = roles;
        this.socialProvider = socialProvider; // CustomUserDetailsService에서 확인하고 소셜이면 바꿔줄 예정
    }

    public Map<String, Object> getClaims() {

        Map<String, Object> dataMap = new HashMap<>();

        dataMap.put("username", getUsername());
        dataMap.put("nickname", nickname);
        dataMap.put("roles", roles);
        dataMap.put("socialProvider", socialProvider);
        
        return dataMap;
    }
    
    // 소셜이 있으면 넣어주기 위해
    public void setSocialProvider(String socialProvider) {
        this.socialProvider = socialProvider;
    }
    
}
