package ru.otus.hw.security.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.catalina.security.SecurityConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import ru.otus.hw.controllers.handlers.GlobalExceptionHandler;
import ru.otus.hw.controllers.rest.AuthorController;
import ru.otus.hw.controllers.rest.BookController;
import ru.otus.hw.controllers.rest.CommentController;
import ru.otus.hw.controllers.rest.GenreController;
import ru.otus.hw.converters.AuthorDtoConverter;
import ru.otus.hw.converters.BookDtoConverter;
import ru.otus.hw.converters.GenreDtoConverter;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.BookFormDto;
import ru.otus.hw.dto.CommentDto;
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.services.AuthorService;
import ru.otus.hw.services.BookService;
import ru.otus.hw.services.CommentService;
import ru.otus.hw.services.GenreService;
import ru.otus.hw.services.UserDetailService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {
        AuthorController.class,
        GenreController.class,
        CommentController.class,
        BookController.class,
})
@Import({
        AuthorService.class,
        GenreService.class,
        CommentService.class,
        BookService.class,
        BookDtoConverter.class,
        AuthorDtoConverter.class,
        GenreDtoConverter.class,
        GlobalExceptionHandler.class,
        SecurityConfig.class,
        UserDetailService.class
})
public class AllControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthorService authorService;

    @MockBean
    private GenreService genreService;

    @MockBean
    private CommentService commentService;

    @MockBean
    private BookService bookService;

    @MockBean
    private UserDetailService userDetailService;

    @Autowired
    private ObjectMapper objectMapper;

    static Stream<Endpoint> getEndpoints() {
        return Stream.of(
                new Endpoint(HttpMethod.GET, "/api/v1/authors"),
                new Endpoint(HttpMethod.GET, "/api/v1/genres"),
                new Endpoint(HttpMethod.GET, "/api/v1/books/1/comments"),
                new Endpoint(HttpMethod.GET, "/api/v1/books"),
                new Endpoint(HttpMethod.GET, "/api/v1/books/1")
        );
    }

    static Stream<Endpoint> deleteBookEndpoints() {
        return Stream.of(
                new Endpoint(HttpMethod.DELETE, "/api/v1/books/1")
        );
    }

    static Stream<Endpoint> deleteCommentEndpoints() {
        return Stream.of(
                new Endpoint(HttpMethod.DELETE, "/api/v1/books/1/comments/1")
        );
    }

    static Stream<Endpoint> postEndpoints() {
        return Stream.of(
                new Endpoint(HttpMethod.POST, "/api/v1/books/1/comments"),
                new Endpoint(HttpMethod.POST, "/api/v1/books")
        );
    }

    static Stream<Endpoint> putEndpoints() {
        return Stream.of(
                new Endpoint(HttpMethod.PUT, "/api/v1/books/1")
        );
    }

    private record Endpoint(HttpMethod method, String uri) {
    }

    @BeforeEach
    void setUpData() {
        AuthorDto authorDto = new AuthorDto("1", "Test Author");
        GenreDto genreDto = new GenreDto("1", "Test Genre");
        CommentDto commentDto = new CommentDto("1", "Comment", "1");

        when(authorService.findAll()).thenReturn(List.of(authorDto));
        when(authorService.findById(any())).thenReturn(Optional.of(authorDto));
        when(genreService.findAll()).thenReturn(List.of(genreDto));
        when(commentService.findById(any())).thenReturn(Optional.of(commentDto));

        BookDto bookDto = new BookDto("1", "Book", authorDto, genreDto);
        when(bookService.findById("1")).thenReturn(Optional.of(bookDto));
        when(bookService.findAll()).thenReturn(List.of(bookDto));
    }

    @ParameterizedTest
    @MethodSource({"getEndpoints", "postEndpoints", "deleteCommentEndpoints", "deleteBookEndpoints","putEndpoints"})
    void shouldAllAnyEndpointWithUnauthorizedUserReturnRedirect(Endpoint endpoint) throws Exception {
        mockMvc.perform(request(endpoint.method(), endpoint.uri()).with(csrf()))
                .andExpect(status().is4xxClientError());
    }

    @ParameterizedTest
    @MethodSource("getEndpoints")
    @WithMockUser(username = "user")
    void shouldAllGetEndpointWithAuthorizedUserReturnOk(Endpoint endpoint) throws Exception {
        mockMvc.perform(request(endpoint.method(), endpoint.uri()))
                .andExpect(status().isOk());
    }

    @ParameterizedTest
    @MethodSource("deleteCommentEndpoints")
    @WithMockUser(username = "user")
    void shouldDeleteCommentEndpointWithAuthorizedUserReturnOk(Endpoint endpoint) throws Exception {
        mockMvc.perform(request(endpoint.method(), endpoint.uri()).with(csrf()))
                .andExpect(status().isNoContent());
    }

    @ParameterizedTest
    @MethodSource("deleteBookEndpoints")
    @WithMockUser(username = "user")
    void shouldDeleteBookEndpointWithAuthorizedUserReturnOk(Endpoint endpoint) throws Exception {
        mockMvc.perform(request(endpoint.method(), endpoint.uri()).with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser("user")
    void shouldCreateCommentWithAuthorizedUserReturnOk() throws Exception {
        CommentDto comment = new CommentDto(null, "Comment", "1");
        CommentDto insertedComment = new CommentDto("1", "Comment", "1");

        when(commentService.insert(comment)).thenReturn(insertedComment);

        mockMvc.perform(request(HttpMethod.POST, "/api/v1/books/1/comments")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(insertedComment)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser()
    void shouldCreateBookWithAuthorizedUserReturnOk() throws Exception {
        AuthorDto authorDto = new AuthorDto("1", "Test Author");
        GenreDto genreDto = new GenreDto("1", "Test Genre");

        BookFormDto bookFormDto = new BookFormDto(null, "Title", "1", "1");
        BookDto insertedBook = new BookDto("1", "Title", authorDto, genreDto);

        when(bookService.insert(bookFormDto)).thenReturn(insertedBook);

        mockMvc.perform(request(HttpMethod.POST, "/api/v1/books")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookFormDto)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser("user")
    void shouldUpdateBookWithAuthorizedUserReturnOk() throws Exception {
        AuthorDto authorDto = new AuthorDto("1", "Test Author");
        GenreDto genreDto = new GenreDto("1", "Test Genre");

        BookFormDto bookFormDto = new BookFormDto("1", "Title", "1", "1");
        BookDto updatedBook = new BookDto("1", "Title Updated", authorDto, genreDto);

        when(bookService.update(bookFormDto)).thenReturn(updatedBook);

        mockMvc.perform(request(HttpMethod.PUT, "/api/v1/books/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookFormDto)))
                .andExpect(status().isOk());
    }

}
