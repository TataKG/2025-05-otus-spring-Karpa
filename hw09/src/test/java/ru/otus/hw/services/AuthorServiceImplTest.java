package ru.otus.hw.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.converters.AuthorDtoConverter;
import ru.otus.hw.models.Author;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Import({
        AuthorDtoConverter.class,
        AuthorServiceImpl.class
})
class AuthorServiceImplTest extends BaseMongoTest {

    private final AuthorService authorService;

    @Autowired
    AuthorServiceImplTest(AuthorService authorService) {
        this.authorService = authorService;
    }

    @Test
    @DisplayName("Должен возвращать всех авторов")
    void shouldFindAllAuthors() {
        // given
        Author savedAuthor1 = createAuthor("Author 1");
        Author savedAuthor2 = createAuthor("Author 2");

        //when
        List<AuthorDto> result = authorService.findAll();

        // then
        assertAll(
                () -> assertThat(result).hasSize(2),
                () -> assertThat(result)
                        .extracting(AuthorDto::fullName)
                        .containsExactlyInAnyOrder("Author 1", "Author 2"),
                () -> assertThat(result)
                        .extracting(AuthorDto::id)
                        .containsExactlyInAnyOrder(savedAuthor1.getId(), savedAuthor2.getId())
        );
    }

    @Test
    @DisplayName("Должен возвращать пустой список, когда авторов нет")
    void shouldReturnEmptyListWhenNoAuthors() {
        List<AuthorDto> result = authorService.findAll();
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Должен находить автора по существующему ID")
    void shouldFindAuthorById() {
        // given
        Author savedAuthor = createAuthor("Test Author");

        // when
        Optional<AuthorDto> result = authorService.findById(savedAuthor.getId());

        // then
        assertTrue(result.isPresent());
        assertAll(
                () -> assertThat(result).isPresent(),
                () -> assertThat(result.get())
                        .extracting(AuthorDto::id, AuthorDto::fullName)
                        .containsExactly(savedAuthor.getId(), "Test Author")
        );
    }

    @Test
    @DisplayName("Должен возвращать Optional.empty() для несуществующего ID")
    void shouldReturnEmptyForNonExistentId() {
        // given
        String nonExistentId = "507f1f77bcf86cd799439011"; // Валидный, но несуществующий ObjectId

        // when
        Optional<AuthorDto> result = authorService.findById(nonExistentId);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Должен возвращать Optional.empty() для некорректного ID")
    void shouldReturnEmptyForInvalidId() {
        // given
        String invalidId = "invalid_id";

        // when
        Optional<AuthorDto> result = authorService.findById(invalidId);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Должен возвращать Optional.empty() для null ID")
    void shouldReturnEmptyForNullId() {
        // when
        Optional<AuthorDto> result = authorService.findById(null);

        // then
        assertThat(result).isEmpty();
    }

}