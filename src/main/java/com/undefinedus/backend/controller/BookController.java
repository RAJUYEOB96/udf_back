package com.undefinedus.backend.controller;

import com.undefinedus.backend.dto.request.book.AladinBookRequestDTO;
import com.undefinedus.backend.dto.request.book.BookChoiceRequestDTO;
import com.undefinedus.backend.dto.request.book.BookStatusRequestDTO;
import com.undefinedus.backend.service.BookService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Log4j2
public class BookController {

    private final BookService bookService;

    @PostMapping("/api/book/choice")
    public Map<String, Long> choiceBook(@RequestBody BookChoiceRequestDTO bookChoiceRequestDTO) {

        Map<String, Long> result = bookService.choiceBook(bookChoiceRequestDTO);

        return result;
    }

}
