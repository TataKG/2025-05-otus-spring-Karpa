package ru.otus.hw.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.otus.hw.dto.CommentDto;
import ru.otus.hw.repositories.CommentRepository;
import ru.otus.hw.services.CommentService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = {CommentController.class})
public class CommentControllerTest {
    @Autowired
    private MockMvc mvc;

    @MockBean
    private CommentService commentService;

    @MockBean
    private CommentRepository commentRepository;

    @Test
    @DisplayName("POST /books/{bookId}/comments/{commentId}/delete - удалить комментарий у книги")
    void shouldDeleteCommentAndRedirectToBookPage() throws Exception {
        String bookId = "1";
        String commentId = "10";

        mvc.perform(post("/books/{bookId}/comments/{commentId}/delete", bookId, commentId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books/view/" + bookId));

        Mockito.verify(commentService).deleteById(commentId);
        Mockito.verifyNoMoreInteractions(commentService);
    }

    @Test
    @DisplayName("POST /book/{bookId}/comments/add - добавить комментарий к книге")
    void shouldAddCommentAndRedirectToBookPage() throws Exception {
        String bookId = "1";
        CommentDto commentDto = new CommentDto(null, "New comment", bookId);

        mvc.perform(post("/books/{bookId}/comments/add", bookId)
                        .param("text", "New comment")
                        .param("bookId", bookId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books/view/" + bookId));

        Mockito.verify(commentService).insert(Mockito.argThat(comment ->
                comment.text().equals("New comment") &&
                        comment.bookId().equals(bookId)
        ));
        Mockito.verifyNoMoreInteractions(commentService);
    }

}