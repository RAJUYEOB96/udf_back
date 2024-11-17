package com.undefinedus.backend.dto.response;

import com.undefinedus.backend.dto.response.book.MyBookResponseDTO;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ScrollResponseDTO<E> {
    private List<E> content;          // 조회된 데이터 리스트
    private boolean hasNext;          // 추가 데이터 존재 여부
    private Long lastId;              // 마지막 항목의 ID
    private int numberOfElements;     // 현재 로드된 항목 수(현재 찾은 리스트 수)
    
    @Builder(builderMethodName = "withAll")
    public ScrollResponseDTO(List<E> content, boolean hasNext, Long lastId, int numberOfElements) {
        this.content = content;
        this.hasNext = hasNext;
        this.lastId = lastId;
        this.numberOfElements = numberOfElements;
    }
}
