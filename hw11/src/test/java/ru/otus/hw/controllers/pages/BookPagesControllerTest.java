package ru.otus.hw.controllers.pages;

import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import ru.otus.hw.converters.AuthorDtoConverter;
import ru.otus.hw.converters.BookDtoConverter;
import ru.otus.hw.converters.CommentDtoConverter;
import ru.otus.hw.converters.GenreDtoConverter;

@WebMvcTest(BookPagesController.class)
@Import({BookDtoConverter.class, AuthorDtoConverter.class, GenreDtoConverter.class, CommentDtoConverter.class})
class BookPagesControllerTest {

}


