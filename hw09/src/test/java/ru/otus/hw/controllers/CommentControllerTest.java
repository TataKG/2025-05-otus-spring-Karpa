package ru.otus.hw.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.otus.hw.dto.CommentDto;
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.repositories.CommentRepository;
import ru.otus.hw.repositories.GenreRepository;
import ru.otus.hw.services.CommentService;
import ru.otus.hw.services.GenreService;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = {CommentController.class})
class CommentControllerTest {
    @Autowired
    private MockMvc mvc;

    @MockBean
    private CommentService commentService;

    @MockBean
    private CommentRepository commentRepository;

    @Test
    @DisplayName("POST /books/{bookId}/comments/{commentId}/deleteComment - удалить комментарий у книги")
    void shouldDeleteCommentAndRedirectToBookPage() throws Exception {
        String bookId = "1";
        String commentId = "10";

        mvc.perform(post("/books/{bookId}/comments/{commentId}/deleteComment", bookId, commentId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/book/view/" + bookId));

        Mockito.verify(commentService).deleteById(commentId);
        Mockito.verifyNoMoreInteractions(commentService);
    }

    @Test
    @DisplayName("POST /book/{bookId}/comments/addComment - добавить комментарий к книге")
    void shouldAddCommentAndRedirectToBookPage() throws Exception {
        String bookId = "1";
        CommentDto commentDto = new CommentDto(null, "New comment", bookId);

        mvc.perform(post("/books/{bookId}/comments/addComment", bookId)
                        .param("text", "New comment")
                        .param("bookId", bookId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/book/view/" + bookId));

        Mockito.verify(commentService).insert(Mockito.argThat(comment ->
                comment.text().equals("New comment") &&
                        comment.bookId().equals(bookId)
        ));
        Mockito.verifyNoMoreInteractions(commentService);
    }

}