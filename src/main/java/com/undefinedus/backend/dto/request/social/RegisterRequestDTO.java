package com.undefinedus.backend.dto.request.social;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequestDTO {
    
    private String username;
    
    private String password;
    
    private String nickname;
    
    private LocalDate birth;
    
    private String gender;
    
    @Builder.Default
    private List<String> preferences = new ArrayList<>();

}
