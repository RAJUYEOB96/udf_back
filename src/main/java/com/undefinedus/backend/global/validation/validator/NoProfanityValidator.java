package com.undefinedus.backend.global.validation.validator;

import com.undefinedus.backend.global.validation.annotation.NoProfanity;
import com.vane.badwordfiltering.BadWordFiltering;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;

@Component  // 스프링 빈으로 등록
public class NoProfanityValidator implements ConstraintValidator<NoProfanity, String> {
    // korean-bad-words 라이브러리의 핵심 클래스
    private final BadWordFiltering badWords = new BadWordFiltering();

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // null이거나 빈 문자열인 경우는 비속어 검사를 하지 않음
        // 필수값 체크는 @NotNull, @NotEmpty 등의 다른 어노테이션으로 처리
        if (value == null || value.trim().isEmpty()) {
            return true;
        }

        // 비속어가 포함되어 있으면 false, 아니면 true 반환
        return !badWords.check(value);
    }
}

