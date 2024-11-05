package com.undefinedus.backend.service;

import com.undefinedus.backend.dto.request.book.BookChoiceRequestDTO;
import java.util.Map;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface BookService {

    public Map<String, Long> choiceBook(BookChoiceRequestDTO bookChoiceRequestDTO);
}
