package ru.otus.hw.services;

import jakarta.validation.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.converters.AuthorDtoConverter;
import ru.otus.hw.converters.CommentDtoConverter;
import ru.otus.hw.dto.CommentDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.repositories.JpaBookRepository;
import ru.otus.hw.repositories.JpaCommentRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@DisplayName("Интеграционный тест для сервиса комментариев")
@DataJpaTest
@Import({CommentServiceImpl.class,
        CommentDtoConverter.class,
        AuthorDtoConverter.class,
        JpaBookRepository.class,
        JpaCommentRepository.class})
@Transactional(propagation = Propagation.NEVER)
class CommentServiceImplTest {

    private final CommentService commentService;

    @Autowired
    CommentServiceImplTest(CommentService commentService) {
        this.commentService = commentService;
    }

    @Test
    @DisplayName("Должен находить комментарий по id без LazyInitializationException")
    void findById_shouldNotThrowLazyException() {
        Optional<CommentDto> result = commentService.findById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().text()).isNotBlank();
        assertThat(result.get().bookId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Должен возвращать пустой Optional при поиске несуществующего комментария")
    void findById_shouldReturnEmptyForNonExistingId() {
        Optional<CommentDto> result = commentService.findById(999L);
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Должен удалять комментарий по id")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void deleteById_shouldDeleteComment() {
        long commentId = 1L;
        assertThat(commentService.findById(commentId)).isPresent();

        commentService.deleteById(commentId);

        assertThat(commentService.findById(commentId)).isEmpty();
    }

    @Test
    @DisplayName("Должен создавать новый комментарий")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void insert_shouldCreateNewComment() {
        String commentText = "New Test Comment";
        long bookId = 1L;

        CommentDto result = commentService.insert(commentText, bookId);

        assertThat(result.id()).isPositive();
        assertThat(result.text()).isEqualTo(commentText);
        assertThat(result.bookId()).isEqualTo(bookId);

        Optional<CommentDto> savedComment = commentService.findById(result.id());
        assertThat(savedComment).isPresent();
        assertThat(savedComment.get().text()).isEqualTo(commentText);
        assertThat(savedComment.get().bookId()).isEqualTo(bookId);
    }

    @Test
    @DisplayName("Должен бросать исключение при создании комментария для несуществующей книги")
    void insert_shouldThrowForNonExistingBook() {
        assertThatThrownBy(() -> commentService.insert("Test", 999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Book with id 999 not found");
    }


    @Test
    @DisplayName("Должен обновлять существующий комментарий")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void update_shouldUpdateExistingComment() {
        long bookId = 1;
        long commentId = 1L;
        String updatedText = "Updated Text";

        CommentDto result = commentService.update(commentId, updatedText,bookId);

        assertThat(result.id()).isEqualTo(commentId);
        assertThat(result.text()).isEqualTo(updatedText);

        Optional<CommentDto> updatedComment = commentService.findById(commentId);
        assertThat(updatedComment).isPresent();
        assertThat(updatedComment.get().text()).isEqualTo(updatedText);
    }
}