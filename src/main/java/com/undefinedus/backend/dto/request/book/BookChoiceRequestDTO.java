package com.undefinedus.backend.dto.request.book;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BookChoiceRequestDTO {

    AladinBookRequestDTO aladinBookRequestDTO;
    BookStatusRequestDTO bookStatusRequestDTO;
}
