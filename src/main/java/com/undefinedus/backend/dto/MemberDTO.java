package com.undefinedus.backend.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

// 김용
@Getter
@Setter
public class MemberDTO extends User {

    private String username;
    private String password;
    private String nickname;
    private SocialLoginDTO socialLoginDTO;
    private List<String> memberRoleList = new ArrayList<>();
    private Set<String> preferences;


    public MemberDTO(String username, String password, String nickname,
        SocialLoginDTO socialLoginDTO,
        List<String> memberRoleList,
        Set<String> preferences) {
        super(username, password, new ArrayList<>());

        this.username = username;
        this.password = password;
        this.nickname = nickname;
        this.socialLoginDTO = socialLoginDTO;
        this.memberRoleList = memberRoleList;
        this.preferences = preferences;
    }

    public Map<String, Object> getClaims() {

        Map<String, Object> dataMap = new HashMap<>();

        dataMap.put("username", username);
        dataMap.put("password", password);
        dataMap.put("nickname", nickname);
        dataMap.put("socialLoginDTO", socialLoginDTO);
        dataMap.put("memberRoleList", memberRoleList);

        return dataMap;
    }
}
