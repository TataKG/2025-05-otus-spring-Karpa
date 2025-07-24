package ru.otus.hw.repositories;

import jakarta.persistence.EntityNotFoundException;
import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Comment;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Репозиторий на основе Jpa для работы с комментариями ")
@DataJpaTest
@Import(JpaCommentRepository.class)
class JpaCommentRepositoryTest {

    private static final long FIRST_COMMENT_ID = 1L;
    private static final long BOOK_ID_WITH_COMMENTS = 1;
    private static final int EXPECTED_COMMENTS_COUNT_FOR_BOOK = 2;
    private static final long NON_EXIST_COMMENT_ID = 999L;
    private static final long BOOK_ID_WITHOUT_COMMENTS = 3L;


    @Autowired
    JpaCommentRepository repositoryJpa;

    @Autowired
    TestEntityManager em;

    @DisplayName("Должен загружать комментарий по id")
    @Test
    void shouldFindCommentById() {
        // given
        val expectedComment = em.find(Comment.class, FIRST_COMMENT_ID);
        em.detach(expectedComment);

        // when
        val optionalActualComment = repositoryJpa.findById(FIRST_COMMENT_ID);

        // then
        assertThat(optionalActualComment)
                .isPresent()
                .get()
                .extracting(
                        Comment::getId,
                        Comment::getText,
                        c -> c.getBook().getId()
                )
                .containsExactly(
                        expectedComment.getId(),
                        expectedComment.getText(),
                        expectedComment.getBook().getId()
                );
    }


    @DisplayName("Должен возвращать пустой Optional, если комментарий не найден")
    @Test
    void shouldReturnEmptyOptionalWhenCommentNotFound() {
        // when
        val optionalComment = repositoryJpa.findById(NON_EXIST_COMMENT_ID);

        // then
        assertThat(optionalComment).isEmpty();
    }

    @DisplayName("Должен загружать список комментариев по id книги")
    @Test
    void shouldFindCommentsByBookId() {
        // when
        val comments = repositoryJpa.findByBookId(BOOK_ID_WITH_COMMENTS);

        // then
        assertThat(comments)
                .isNotNull()
                .hasSize(EXPECTED_COMMENTS_COUNT_FOR_BOOK)
                .allSatisfy(comment -> {
                    assertThat(comment.getText()).isNotBlank();
                    assertThat(comment.getBook())
                            .isNotNull()
                            .extracting(Book::getId)
                            .isEqualTo(BOOK_ID_WITH_COMMENTS);
                });
    }

    @DisplayName("Должен возвращать пустой список, если у книги нет комментариев")
    @Test
    void shouldReturnEmptyListWhenBookHasNoComments() {
        // when
        val comments = repositoryJpa.findByBookId(BOOK_ID_WITHOUT_COMMENTS);

        // then
        assertThat(comments).isNotNull().isEmpty();
    }

    @DisplayName("Должен сохранять новый комментарий")
    @Test
    void shouldSaveNewComment() {
        // given
        Book book = em.find(Book.class, BOOK_ID_WITH_COMMENTS);
        Comment newComment = new Comment(0, "New Comment", book);

        // when
        Comment savedComment = repositoryJpa.save(newComment);
        em.flush();
        em.clear();

        // then
        assertThat(savedComment.getId()).isPositive();

        val foundComment = em.find(Comment.class, savedComment.getId());
        assertThat(foundComment)
                .isNotNull()
                .extracting(
                        Comment::getText,
                        c -> c.getBook().getId()
                )
                .containsExactly(
                        "New Comment",
                        BOOK_ID_WITH_COMMENTS
                );
    }

    @DisplayName("Должен обновлять существующий комментарий")
    @Test
    void shouldUpdateExistingComment() {
        // given
        Comment existingComment = em.find(Comment.class, FIRST_COMMENT_ID);
        String newText = "Updated Comment Text";
        existingComment.setText(newText);
        em.detach(existingComment);

        // when
        repositoryJpa.save(existingComment);
        em.flush();
        em.clear();

        // then
        val foundComment = em.find(Comment.class, FIRST_COMMENT_ID);
        assertThat(foundComment)
                .isNotNull()
                .extracting(Comment::getText)
                .isEqualTo(newText);
    }

    @DisplayName("Должен удалять комментарий по id")
    @Test
    void shouldDeleteCommentById() {
        // given
        Comment comment = em.find(Comment.class, FIRST_COMMENT_ID);
        assertThat(comment).isNotNull();
        em.detach(comment);

        // when
        repositoryJpa.deleteById(FIRST_COMMENT_ID);
        em.flush();
        em.clear();

        // then
        assertThat(em.find(Comment.class, FIRST_COMMENT_ID)).isNull();
    }
}