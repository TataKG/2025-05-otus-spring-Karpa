package ru.otus.hw.controllers.pages;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import ru.otus.hw.converters.AuthorDtoConverter;
import ru.otus.hw.converters.BookDtoConverter;
import ru.otus.hw.converters.CommentDtoConverter;
import ru.otus.hw.converters.GenreDtoConverter;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.BookFormDto;
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.services.AuthorService;
import ru.otus.hw.services.BookService;
import ru.otus.hw.services.CommentService;
import ru.otus.hw.services.GenreService;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookPagesController.class)
@Import({BookDtoConverter.class, AuthorDtoConverter.class, GenreDtoConverter.class, CommentDtoConverter.class})
class BookPagesControllerTest {

//    @Autowired
//    private MockMvc mockMvc;
//
//    @MockBean
//    private BookService bookService;
//
//    @MockBean
//    private AuthorService authorService;
//
//    @MockBean
//    private CommentService commentService;
//
//    @MockBean
//    private GenreService genreService;

//    @Test
//    @DisplayName("Должен вывести список книг")
//    void ShouldReturnViewWithBooks() throws Exception {
//
//        AuthorDto author = new AuthorDto("1", "Author 1");
//        GenreDto genre = new GenreDto("1", "Genre1");
//        List<BookDto> books = List.of(
//                new BookDto("1", "Book 1", author, genre),
//                new BookDto("2", "Book 2", author, genre)
//        );
//
//        // Given
//        when(bookService.findAll()).thenReturn(books);
//
//        // When & Then
//        mockMvc.perform(get("/"))
//                .andExpect(status().isOk())
//                .andExpect(view().name("book-list"))
//                .andExpect(model().attribute("books", hasSize(2)));
//
//        verify(bookService, times(1)).findAll();
//        verifyNoMoreInteractions(bookService);
//    }
//
//    @Test
//    @DisplayName("Должен вернуть форму для редактирования существующей книги")
//    void shouldReturnEditPageForExistingBook() throws Exception {
//        String bookId = "1";
//        BookDto bookDto = new BookDto(bookId, "Existing Book", null, null);
//        BookFormDto bookFormDto = new BookFormDto(bookId, "Existing Book", null, null);
//
//        when(bookService.findById(bookId)).thenReturn(Optional.of(bookDto));
//        when(authorService.findAll()).thenReturn(List.of());
//        when(genreService.findAll()).thenReturn(List.of());
//        when(commentService.findByBookId(bookId)).thenReturn(List.of());
//
//        mockMvc.perform(get("/books/edit/{id}", bookId))
//                .andExpect(status().isOk())
//                .andExpect(view().name("book-edit"))
//        ;
//    }
}


