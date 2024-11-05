package com.undefinedus.backend.config;

import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("공책(gong-chaek) 프로젝트 API 문서")  // 프로젝트 이름으로 변경
                        .description(
                                "API 명세서\n\n개발자:\n- 김재원 (wodnjs3580@naver.com)\n- 김용 (97rladyd@gmail.com)")   // 프로젝트 설명 추가
                        .version("v1.0.0")         // 버전 정보 수정
                        .contact(new Contact()
                                .name("김재원, 김용")  // 실제 개발자 이름으로 변경
                                .email("wodnjs3580@naver.com, 97rladyd@gmail.com"))    // 실제 이메일 주소로 변경
                )
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local Server"),      // 로컬 서버 URL
                        new Server().url("http://52.78.190.92:8080").description("Production Server")   // 실제 운영 서버 URL
                ));
    }
}
