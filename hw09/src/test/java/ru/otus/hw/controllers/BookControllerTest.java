package ru.otus.hw.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Import;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.otus.hw.converters.AuthorDtoConverter;
import ru.otus.hw.converters.BookDtoConverter;
import ru.otus.hw.converters.CommentDtoConverter;
import ru.otus.hw.converters.GenreDtoConverter;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.CommentDto;
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.CommentRepository;
import ru.otus.hw.repositories.GenreRepository;
import ru.otus.hw.services.AuthorService;
import ru.otus.hw.services.BookService;
import ru.otus.hw.services.CommentService;
import ru.otus.hw.services.GenreService;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(BookController.class)
@Import({BookDtoConverter.class, AuthorDtoConverter.class, GenreDtoConverter.class, CommentDtoConverter.class})
public class BookControllerTest {

    private static final String VIEW_CUSTOM_ERROR = "customError";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private BookDtoConverter bookConverter;

    @MockBean
    private BookService bookService;

    @MockBean
    private BookRepository bookRepository;

    @MockBean
    private AuthorService authorService;

    @MockBean
    private AuthorRepository authorRepository;

    @MockBean
    private CommentService commentService;

    @MockBean
    private CommentRepository commentRepository;

    @MockBean
    private GenreService genreService;

    @MockBean
    private GenreRepository genreRepository;

    @MockBean
    @Autowired
    private MessageSource messageSource;

    @Test
    @DisplayName("GET /books - должен отобразить список книг")
    void shouldReturnListPage() throws Exception {
        AuthorDto author = new AuthorDto("1", "Author 1");
        GenreDto genre = new GenreDto("1", "Genre1");
        List<BookDto> books = List.of(
                new BookDto("1", "Book 1", author, genre),
                new BookDto("2", "Book 2", author, genre)
        );
        when(bookService.findAll()).thenReturn(books);

        mvc.perform(get("/books"))
                .andExpect(status().isOk())
                .andExpect(view().name("book-list"))
                .andExpect(model().attribute("books", books));

    }

    @Test
    @DisplayName("GET /books/view/{id} - должен показать книгу с комментариями")
    void getBook_ShouldReturnBookView() throws Exception {
        // given

        AuthorDto author = new AuthorDto("1", "Author 1");
        GenreDto genre = new GenreDto("1", "Genre1");

        String bookId = "1";

        BookDto bookDto = new BookDto(bookId, "Книга",
                author, genre);
        List<CommentDto> comments = List.of(
                new CommentDto("1", "Great book", bookId),
                new CommentDto("2", "Good", bookId)
        );

        when(bookService.findById(bookId)).thenReturn(Optional.of(bookDto));
        when(bookService.findById(bookId)).thenReturn(Optional.of(bookDto));
        when(commentService.findByBookId(bookId)).thenReturn(comments);

        // when & then
        mvc.perform(get("/books/view/{id}", bookId))
                .andExpect(status().isOk())
                .andExpect(view().name("book-view"))
                .andExpect(model().attributeExists("book"))
                .andExpect(model().attribute("book", bookDto));
    }

    @Test
    void viewPage_WhenBookNotFound_ShouldReturn404() throws Exception {
        // given
        String nonExistentId = "99";
        when(bookService.findById(nonExistentId)).thenReturn(Optional.empty());

        // when & then
        mvc.perform(get("/books/view/{id}", nonExistentId))
                .andExpect(view().name(VIEW_CUSTOM_ERROR));
    }
}