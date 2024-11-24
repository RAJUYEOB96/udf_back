package com.undefinedus.backend.dto.response;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
public class ScrollResponseDTO<E> {
    private List<E> content;          // 조회된 데이터 리스트
    private boolean hasNext;          // 추가 데이터 존재 여부
    private Long lastId;              // 마지막 항목의 ID
    private int numberOfElements;     // 현재 로드된 항목 수(현재 찾은 리스트 수)
    private Long totalElements;        // 해당 리스트의 전체 수

    // === 아래 social 에서 필요 === //
    // TODO : 여유가 될 때 파일 각각의 상황에 맞게 쪼개놓기
    private String lastNickname;    // // 마지막으로 본 닉네임 추가

    @Builder(builderMethodName = "withAll")
    public ScrollResponseDTO(List<E> content, boolean hasNext, Long lastId, String lastNickname, int numberOfElements
            , Long totalElements) {
        this.content = content;
        this.hasNext = hasNext;
        this.lastId = lastId;
        this.lastNickname = lastNickname;
        this.numberOfElements = numberOfElements;
        this.totalElements = totalElements;
    }
}
