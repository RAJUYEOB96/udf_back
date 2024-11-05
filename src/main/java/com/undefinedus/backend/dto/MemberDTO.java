package com.undefinedus.backend.dto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Getter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

@Getter
public class MemberDTO  extends User {

    private String username;
    private String password;
    private String nickname;
    private SocialLoginDTO socialLoginDTO;
    private List<String> memberRoleList;


    public MemberDTO(String username, String password, String nickname,
        SocialLoginDTO socialLoginDTO,
        List<String> memberRoleList) {
        super(username, password,
            memberRoleList.stream().map(str -> new SimpleGrantedAuthority("ROLE_"+str)).collect(
                Collectors.toList()));

        this.username = username;
        this.password = password;
        this.nickname = nickname;
        this.socialLoginDTO = socialLoginDTO;
        this.memberRoleList = memberRoleList;
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
