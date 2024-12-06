package com.undefinedus.backend.dto.request.myPage;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordRequestDTO {

    @JsonProperty("prevPassword")
    private String prevPassword;

    @JsonProperty("newPassword")
    private String newPassword;
}
