package com.undefinedus.backend.config;

import com.undefinedus.backend.handler.APILoginFailHandler;
import com.undefinedus.backend.handler.APILoginSuccessHandler;
import com.undefinedus.backend.handler.CustomAccessDeniedHandler;
import com.undefinedus.backend.security.filter.JWTCheckFilter;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;


@Configuration
@Log4j2
@RequiredArgsConstructor
@EnableMethodSecurity
public class CustomSecurityConfig {
    
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        
        log.info("..............security config...............");
        
        // CORS(Cross-Origin Resource Sharing) 설정
        // 같은 파일 아래쪽에 만들어 놓음
        http.cors(httpSecurityCorsConfigurer -> {
            httpSecurityCorsConfigurer.configurationSource(corsConfigurationSource());
        });
        
        // 리액트로 SPA를 구현하고 API 서버로 스프링 부트를 사용한다면,
        // CSRF 보호가 꼭 필요하지 않을 수 있습니다.
        // SPA에서는 주로 토큰 기반 인증(예: JWT)을 사용하기 때문입니다.
        http.csrf(httpSecurityCsrfConfigurer -> {
            httpSecurityCsrfConfigurer.disable();
        });
        
        // 로그인 설정, formLogin 이기 때문에 formData 형태로 와야함, Json 아님
        http.formLogin(config -> {
            config.loginPage("/api/member/login");
            config.successHandler(new APILoginSuccessHandler());
            config.failureHandler(new APILoginFailHandler());
        });
        
        // 세션 관련 설정 (여기서는 Session을 안만들게 설정)
        http.sessionManagement(httpSecuritySessionManagementConfigurer -> {
            httpSecuritySessionManagementConfigurer.sessionCreationPolicy(SessionCreationPolicy.NEVER);
        });
        
        // 실행전 JWT를 확인하는 설정, 로그인 말고도 다른걸 할때 체크하는지는 알아봐야함
        // TODO : 좀더 어떤 역할을 하는지 알아볼 필요!!
        http.addFilterBefore(new JWTCheckFilter(), UsernamePasswordAuthenticationFilter.class);
        
        // 익셉션 발생시 행동 설정
        http.exceptionHandling(config -> {
            config.accessDeniedHandler(new CustomAccessDeniedHandler());
        });
        
        return http.build();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        
        CorsConfiguration configuration = new CorsConfiguration();
        
        // TODO : 모든 출처 허용 -> 나중에 마지막에 바꾸어야 할듯
        // TODO : 이건 무엇?
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        
        // GET, POST, PUT, PATCH, DELETE, HEAD, OPTIONS 메소드 허용
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "HEAD", "OPTIONS"));
        
        // Authorization, Cache-Control, Content-Type 헤더 허용 // 나머지가 필요한가?
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Cache-Control",
                "Content-Type",
                "Accept",
                "Access-Control-Allow-Origin",
                "Access-Control-Allow-Headers",
                "Origin"
        ));
        
        // 자격 증명 허용
        // TODO : 이건 무엇?
        configuration.setAllowCredentials(true);
        
        // 아래는 어디에 필요한거지?
        configuration.addAllowedOrigin("http://localhost:5173");
        configuration.addAllowedOrigin("http://gongchaek.site");
        configuration.addAllowedOrigin("https://gongchaek.site");
        configuration.addAllowedOrigin("http://localhost:8080");
        
        // URL 기반 CORS 구성 소스 생성
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        
        // 모든 URL에 대한 CORS 구성 등록
        source.registerCorsConfiguration("/**", configuration);
        
        // CORS 구성 소스 Bean 반환
        return source;
    }
}
