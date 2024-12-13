package com.undefinedus.backend.global.validation.annotation;

import com.undefinedus.backend.global.validation.validator.NoProfanityValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// 비속어 필터링을 위한 커스텀 어노테이션
// DTO의 필드에 이 어노테이션을 붙이면 자동으로 비속어 검사가 진행됨
@Target({ElementType.FIELD})  // 필드 레벨에만 어노테이션 적용
@Retention(RetentionPolicy.RUNTIME)  // 런타임에도 어노테이션 정보 유지
@Constraint(validatedBy = NoProfanityValidator.class)  // 실제 검증을 수행할 Validator 클래스 지정
public @interface NoProfanity {
    // 유효성 검사 실패시 반환할 메시지
    String message() default "부적절한 표현이 포함되어 있습니다";

    // groups와 payload는 Bean Validation 스펙에서 요구하는 필수 속성
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

