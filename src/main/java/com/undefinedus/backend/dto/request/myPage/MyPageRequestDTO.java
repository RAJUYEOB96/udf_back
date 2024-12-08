package com.undefinedus.backend.dto.request.myPage;

import lombok.Builder;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class MyPageRequestDTO {
    private MultipartFile profileImage;

    private String nickname;

    private LocalDate birth;

    private String gender;

    private List<String> preferences;
}
