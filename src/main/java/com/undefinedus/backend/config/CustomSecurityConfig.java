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
        // - 모든 요청에 대해 JWT 토큰을 검증
        // - UsernamePasswordAuthenticationFilter 이전에 실행됨
        // - 즉, 사용자 인증 전에 먼저 JWT 토큰을 확인
        // - 유효한 JWT가 있다면 해당 사용자는 이미 인증된 것으로 처리
        http.addFilterBefore(new JWTCheckFilter(), UsernamePasswordAuthenticationFilter.class);
        
        // 익셉션 발생시 행동 설정
        // - 접근 거부(403 Forbidden) 발생 시 CustomAccessDeniedHandler가 처리
        // - 예: 권한이 없는 사용자가 관리자 페이지 접근 시도할 때
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
        // CORS는 "다른 출처(도메인)"에서 리소스를 요청할 수 있게 허용하는 보안 메커니즘입니다.
        // "다른 출처"란 프로토콜(http/https), 도메인(gongchaek.site), 포트(5173/8080)중 하나라도 다른 경우를 말합니다.
        
        CorsConfiguration configuration = new CorsConfiguration();
        
        // - "*" 패턴은 모든 출처(도메인)에서의 요청을 허용한다는 의미
        // - 보안상 위험할 수 있으므로 프로덕션 환경에서는
        // - 실제 클라이언트 도메인만 명시적으로 허용하는 것이 좋음
        // - 예: "https://gongchaek.site"
//        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        
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
        // - 인증 정보(쿠키, HTTP 인증)를 포함한 요청을 허용할지 설정
        // - true로 설정하면 클라이언트에서 credentials: 'include' 옵션으로
        //   쿠키나 인증 헤더를 포함한 요청을 보낼 수 있음
        // - JWT를 사용할 때는 일반적으로 true로 설정
        configuration.setAllowCredentials(true);
        
        // - 특정 도메인에서의 요청을 명시적으로 허용
        // - 개발 환경(localhost)과 프로덕션 환경(gongchaek.site)의
        //   클라이언트 도메인을 등록
        // - setAllowCredentials(true)와 함께 사용할 때는
        //   와일드카드(*) 대신 명시적인 도메인을 지정해야 함
        configuration.addAllowedOrigin("http://localhost:5173");  // 로컬 프론트엔드
        configuration.addAllowedOrigin("https://gongchaek.site"); // 배포된 프론트엔드
        
        // URL 기반 CORS 구성 소스 생성
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        
        // 모든 URL에 대한 CORS 구성 등록
        source.registerCorsConfiguration("/**", configuration);
        
        // CORS 구성 소스 Bean 반환
        return source;
    }
}
