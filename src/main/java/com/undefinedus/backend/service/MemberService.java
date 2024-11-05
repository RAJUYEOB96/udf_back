package com.undefinedus.backend.service;

import com.undefinedus.backend.domain.entity.Member;
import com.undefinedus.backend.domain.entity.SocialLogin;
import com.undefinedus.backend.dto.MemberDTO;
import com.undefinedus.backend.dto.SocialLoginDTO;
import com.undefinedus.backend.dto.request.social.RegisterRequestDTO;
import java.util.Map;
import java.util.stream.Collectors;

public interface MemberService {
    
    Map<String, Object> getKakaoInfo(String accessToken);
    
    MemberDTO socialRegister(RegisterRequestDTO requestDTO);
    
    MemberDTO regularRegister(RegisterRequestDTO requestDTO);
    
    void usernameDuplicateCheck(String username);
    
    void nicknameDuplicateCheck(String nickname);
    
    default MemberDTO entityToDTOWithSocial(Member member) {
        
        SocialLogin socialLogin = member.getSocialLogin();
        
        SocialLoginDTO socialLoginDTO = SocialLoginDTO.builder()
                .id(socialLogin.getId())
                .memberId(socialLogin.getMember().getId())
                .provider(socialLogin.getProvider())
                .providerId(socialLogin.getProviderId())
                .build();
        
        MemberDTO memberDTO = new MemberDTO(
                member.getUsername(),
                member.getPassword(),
                member.getNickname(),
                socialLoginDTO,
                member.getMemberRoleList().stream().map(memberRole -> memberRole.name()).collect(Collectors.toList()));
        
        return memberDTO;
    }
    
    default MemberDTO entityToDTOWithRegular(Member member) {
        
        MemberDTO memberDTO = new MemberDTO(
                member.getUsername(),
                member.getPassword(),
                member.getNickname(),
                null,
                member.getMemberRoleList().stream().map(memberRole -> memberRole.name()).collect(Collectors.toList()));
        
        return memberDTO;
    }
   
}
